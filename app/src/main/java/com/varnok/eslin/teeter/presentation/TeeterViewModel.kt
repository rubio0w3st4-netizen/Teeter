package com.varnok.eslin.teeter.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.varnok.eslin.teeter.audio.Haptics
import com.varnok.eslin.teeter.audio.Sfx
import com.varnok.eslin.teeter.audio.SoundManager
import com.varnok.eslin.teeter.domain.model.RunStats
import com.varnok.eslin.teeter.domain.repository.ProgressRepository
import com.varnok.eslin.teeter.domain.repository.SettingsRepository
import com.varnok.eslin.teeter.domain.tower.TowerEngine
import com.varnok.eslin.teeter.domain.usecase.AwardBoardUseCase
import com.varnok.eslin.teeter.domain.usecase.AwardCard
import com.varnok.eslin.teeter.domain.usecase.BuildBoardUseCase
import com.varnok.eslin.teeter.domain.usecase.ChapterBoard
import com.varnok.eslin.teeter.domain.usecase.FinishRunUseCase
import com.varnok.eslin.teeter.domain.usecase.NextLevelUseCase
import com.varnok.eslin.teeter.domain.usecase.RunOutcome
import com.varnok.eslin.teeter.domain.usecase.StartRunUseCase
import com.varnok.eslin.teeter.domain.usecase.StatsUseCase

class TeeterViewModel(
    private val startRun: StartRunUseCase,
    private val finishRun: FinishRunUseCase,
    private val buildBoard: BuildBoardUseCase,
    private val nextLevel: NextLevelUseCase,
    private val awardBoard: AwardBoardUseCase,
    private val statsOf: StatsUseCase,
    private val progress: ProgressRepository,
    private val settings: SettingsRepository,
    val sound: SoundManager,
    val haptics: Haptics
) : ViewModel() {

    val stack = mutableStateListOf<Route>(Route.Menu)

    var engine: TowerEngine? by mutableStateOf(null)
        private set
    var outcome: RunOutcome? by mutableStateOf(null)
        private set
    var boards: List<ChapterBoard> by mutableStateOf(emptyList())
        private set
    var awards: List<AwardCard> by mutableStateOf(emptyList())
        private set
    var stats: RunStats by mutableStateOf(RunStats(0, 0, 0, 0, 0, 0, 0))
        private set
    var totalStars: Int by mutableStateOf(0)
        private set
    var soundOn: Boolean by mutableStateOf(true)
        private set
    var vibrationOn: Boolean by mutableStateOf(true)
        private set
    var backdrop: Int by mutableStateOf(0)
        private set
    var guidesOn: Boolean by mutableStateOf(true)
        private set
    var levelIndex: Int by mutableStateOf(0)
        private set
    var endless: Boolean by mutableStateOf(false)
        private set
    var paused: Boolean by mutableStateOf(false)
        private set

    private var booted = false

    fun boot() {
        if (booted) return
        booted = true
        refresh()
        if (!progress.tutorialSeen()) {
            stack.clear()
            stack.add(Route.Tutorial)
        }
    }

    private fun refresh() {
        boards = buildBoard()
        awards = awardBoard()
        stats = statsOf()
        totalStars = progress.totalStars()
        soundOn = settings.sound()
        vibrationOn = settings.vibration()
        backdrop = settings.backdrop()
        guidesOn = settings.guides()
    }

    val route: Route get() = stack.last()

    fun push(route: Route) {
        sound.play(Sfx.TAP)
        haptics.tick()
        refresh()
        stack.add(route)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex) else return
        refresh()
    }

    fun goMenu() {
        stack.clear()
        stack.add(Route.Menu)
        engine = null
        refresh()
    }

    fun finishTutorial() {
        progress.markTutorialSeen()
        stack.clear()
        stack.add(Route.Menu)
        refresh()
    }

    fun startLevel(index: Int) {
        levelIndex = index
        endless = false
        paused = false
        outcome = null
        engine = startRun(index, false)
        sound.play(Sfx.TAP)
        haptics.tick()
        if (route != Route.Play) stack.add(Route.Play)
    }

    fun startEndless() {
        endless = true
        paused = false
        outcome = null
        engine = startRun(0, true)
        sound.play(Sfx.TAP)
        haptics.tick()
        if (route != Route.Play) stack.add(Route.Play)
    }

    fun continueRun() {
        startLevel(nextLevel())
    }

    fun settleRun() {
        val e = engine ?: return
        if (outcome != null) return
        val result = finishRun(e, levelIndex, endless)
        outcome = result
        refresh()
        if (result.newAwards.isNotEmpty()) sound.play(Sfx.UNLOCK)
        stack.removeAll { it == Route.Play }
        stack.add(Route.Result)
    }

    fun retry() {
        outcome = null
        stack.removeAll { it == Route.Result }
        if (endless) startEndless() else startLevel(levelIndex)
    }

    fun advance() {
        outcome = null
        stack.removeAll { it == Route.Result }
        val next = (levelIndex + 1).coerceAtMost(boards.sumOf { it.cards.size } - 1)
        startLevel(next)
    }

    fun togglePause() {
        paused = !paused
        engine?.paused = paused
        sound.play(Sfx.TAP)
    }

    fun quitRun() {
        engine = null
        paused = false
        outcome = null
        goMenu()
    }

    fun setSound(value: Boolean) {
        settings.setSound(value); soundOn = value
        if (value) sound.play(Sfx.TAP)
    }

    fun setVibration(value: Boolean) {
        settings.setVibration(value); vibrationOn = value
        if (value) haptics.nudge()
    }

    fun chooseBackdrop(value: Int) {
        settings.setBackdrop(value); backdrop = value
        sound.play(Sfx.TAP)
    }

    fun setGuides(value: Boolean) {
        settings.setGuides(value); guidesOn = value
        sound.play(Sfx.TAP)
    }

    fun wipeProgress() {
        progress.resetAll()
        refresh()
        haptics.error()
    }
}
