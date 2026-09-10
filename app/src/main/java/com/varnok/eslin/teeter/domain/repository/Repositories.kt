package com.varnok.eslin.teeter.domain.repository

import com.varnok.eslin.teeter.domain.model.Award
import com.varnok.eslin.teeter.domain.model.Chapter
import com.varnok.eslin.teeter.domain.model.LevelProgress
import com.varnok.eslin.teeter.domain.model.LevelSpec
import com.varnok.eslin.teeter.domain.model.RunStats

interface LevelRepository {
    fun levels(): List<LevelSpec>
    fun chapters(): List<Chapter>
    fun awards(): List<Award>
    fun endless(): LevelSpec
    fun level(index: Int): LevelSpec
}

interface ProgressRepository {
    fun progress(index: Int): LevelProgress
    fun record(index: Int, stars: Int, pulled: Int)
    fun totalStars(): Int
    fun clearedCount(): Int
    fun stats(): RunStats
    fun addRun(pulled: Int, collapsed: Boolean, cleared: Boolean, tallest: Int)
    fun bestEndless(): Int
    fun setBestEndless(value: Int)
    fun awardIds(): Set<String>
    fun unlockAward(id: String): Boolean
    fun tutorialSeen(): Boolean
    fun markTutorialSeen()
    fun resetAll()
}

interface SettingsRepository {
    fun sound(): Boolean
    fun setSound(value: Boolean)
    fun vibration(): Boolean
    fun setVibration(value: Boolean)
    fun backdrop(): Int
    fun setBackdrop(value: Int)
    fun guides(): Boolean
    fun setGuides(value: Boolean)
}
