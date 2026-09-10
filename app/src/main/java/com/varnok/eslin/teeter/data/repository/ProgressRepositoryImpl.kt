package com.varnok.eslin.teeter.data.repository

import com.varnok.eslin.teeter.data.catalog.LevelCatalog
import com.varnok.eslin.teeter.data.local.TeeterPrefs
import com.varnok.eslin.teeter.domain.model.LevelProgress
import com.varnok.eslin.teeter.domain.model.RunStats
import com.varnok.eslin.teeter.domain.repository.ProgressRepository
import kotlin.math.max

class ProgressRepositoryImpl(private val prefs: TeeterPrefs) : ProgressRepository {

    override fun progress(index: Int): LevelProgress {
        val stars = prefs.int("lvl_${index}_stars", 0)
        val best = prefs.int("lvl_${index}_pulled", 0)
        val unlocked = index == 0 || prefs.int("lvl_${index - 1}_stars", 0) > 0
        return LevelProgress(stars, best, unlocked)
    }

    override fun record(index: Int, stars: Int, pulled: Int) {
        if (stars > prefs.int("lvl_${index}_stars", 0)) prefs.putInt("lvl_${index}_stars", stars)
        if (pulled > prefs.int("lvl_${index}_pulled", 0)) prefs.putInt("lvl_${index}_pulled", pulled)
    }

    override fun totalStars(): Int {
        var sum = 0
        for (i in LevelCatalog.levels.indices) sum += prefs.int("lvl_${i}_stars", 0)
        return sum
    }

    override fun clearedCount(): Int {
        var n = 0
        for (i in LevelCatalog.levels.indices) if (prefs.int("lvl_${i}_stars", 0) > 0) n++
        return n
    }

    override fun stats(): RunStats = RunStats(
        runs = prefs.int("stat_runs", 0),
        pulled = prefs.int("stat_pulled", 0),
        collapses = prefs.int("stat_collapses", 0),
        cleared = clearedCount(),
        stars = totalStars(),
        bestEndless = bestEndless(),
        tallest = prefs.int("stat_tallest", 0)
    )

    override fun addRun(pulled: Int, collapsed: Boolean, cleared: Boolean, tallest: Int) {
        prefs.putInt("stat_runs", prefs.int("stat_runs", 0) + 1)
        prefs.putInt("stat_pulled", prefs.int("stat_pulled", 0) + pulled)
        if (collapsed) prefs.putInt("stat_collapses", prefs.int("stat_collapses", 0) + 1)
        prefs.putInt("stat_tallest", max(prefs.int("stat_tallest", 0), tallest))
    }

    override fun bestEndless(): Int = prefs.int("stat_best_endless", 0)

    override fun setBestEndless(value: Int) {
        if (value > bestEndless()) prefs.putInt("stat_best_endless", value)
    }

    override fun awardIds(): Set<String> = prefs.strings("awards")

    override fun unlockAward(id: String): Boolean {
        val cur = awardIds()
        if (cur.contains(id)) return false
        prefs.putStrings("awards", cur + id)
        return true
    }

    override fun tutorialSeen(): Boolean = prefs.bool("tutorial_seen", false)

    override fun markTutorialSeen() = prefs.putBool("tutorial_seen", true)

    override fun resetAll() = prefs.wipe()
}
