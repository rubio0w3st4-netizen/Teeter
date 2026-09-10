package com.varnok.eslin.teeter.domain.usecase

import com.varnok.eslin.teeter.domain.model.Award
import com.varnok.eslin.teeter.domain.model.Chapter
import com.varnok.eslin.teeter.domain.model.LevelProgress
import com.varnok.eslin.teeter.domain.model.LevelSpec
import com.varnok.eslin.teeter.domain.model.RunStats
import com.varnok.eslin.teeter.domain.repository.LevelRepository
import com.varnok.eslin.teeter.domain.repository.ProgressRepository
import com.varnok.eslin.teeter.domain.tower.RunState
import com.varnok.eslin.teeter.domain.tower.TowerEngine

data class LevelCard(val spec: LevelSpec, val progress: LevelProgress)

data class ChapterBoard(val chapter: Chapter, val cards: List<LevelCard>, val stars: Int, val maxStars: Int)

data class AwardCard(val award: Award, val unlocked: Boolean)

data class RunOutcome(
    val cleared: Boolean,
    val stars: Int,
    val pulled: Int,
    val courses: Int,
    val newAwards: List<Award>
)

class StartRunUseCase(private val levels: LevelRepository) {
    operator fun invoke(index: Int, endless: Boolean): TowerEngine {
        val spec = if (endless) levels.endless() else levels.level(index)
        return TowerEngine(spec, endless, System.nanoTime())
    }
}

class BuildBoardUseCase(
    private val levels: LevelRepository,
    private val progress: ProgressRepository
) {
    operator fun invoke(): List<ChapterBoard> {
        val all = levels.levels()
        return levels.chapters().map { ch ->
            val cards = all.filter { it.chapter == ch.index }
                .map { LevelCard(it, progress.progress(it.index)) }
            ChapterBoard(ch, cards, cards.sumOf { it.progress.stars }, cards.size * 3)
        }
    }
}

class NextLevelUseCase(
    private val levels: LevelRepository,
    private val progress: ProgressRepository
) {
    operator fun invoke(): Int {
        val all = levels.levels()
        for (spec in all) if (progress.progress(spec.index).stars == 0) return spec.index
        return all.lastIndex
    }
}

class AwardBoardUseCase(
    private val levels: LevelRepository,
    private val progress: ProgressRepository
) {
    operator fun invoke(): List<AwardCard> {
        val unlocked = progress.awardIds()
        return levels.awards().map { AwardCard(it, unlocked.contains(it.id)) }
    }
}

class StatsUseCase(private val progress: ProgressRepository) {
    operator fun invoke(): RunStats = progress.stats()
}

class FinishRunUseCase(
    private val levels: LevelRepository,
    private val progress: ProgressRepository
) {
    operator fun invoke(engine: TowerEngine, index: Int, endless: Boolean): RunOutcome {
        val cleared = engine.state == RunState.WON
        val stars = engine.stars()
        val courses = engine.layerCount
        progress.addRun(engine.pulled, engine.state != RunState.WON, cleared, courses)
        if (!endless && cleared) progress.record(index, stars, engine.pulled)
        if (endless) progress.setBestEndless(engine.pulled)

        val granted = ArrayList<Award>()
        val byId = levels.awards().associateBy { it.id }
        fun grant(id: String) {
            if (progress.unlockAward(id)) byId[id]?.let { granted.add(it) }
        }
        if (engine.pulled > 0) grant("first_pull")
        if (cleared && !endless) grant("first_clear")
        if (progress.clearedCount() >= 10) grant("ten_levels")
        if (progress.clearedCount() >= levels.levels().size) grant("all_levels")
        if (stars >= 3) grant("three_star")
        if (progress.totalStars() >= 30) grant("thirty_stars")
        if (cleared && engine.maxLean <= engine.spec.steadyCap * 0.6f) grant("no_wobble")
        if (engine.stonePulled && cleared) grant("stone_pull")
        if (courses >= 18) grant("tall_tower")
        if (endless && engine.pulled >= 20) grant("endless_20")
        if (endless && engine.pulled >= 35) grant("endless_35")
        if (progress.stats().pulled >= 100) grant("hundred")
        return RunOutcome(cleared, stars, engine.pulled, courses, granted)
    }
}
