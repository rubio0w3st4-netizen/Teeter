package com.varnok.eslin.teeter.data.catalog

import com.varnok.eslin.teeter.domain.model.Award
import com.varnok.eslin.teeter.domain.model.Chapter
import com.varnok.eslin.teeter.domain.model.Goal
import com.varnok.eslin.teeter.domain.model.LevelSpec

object LevelCatalog {

    const val PER_CHAPTER = 9

    val chapters = listOf(
        Chapter(0, "Workbench", "Dry pine, still air, nothing to blame but your hands.", "bg_workbench"),
        Chapter(1, "Attic Loft", "Lacquered courses that let go without warning.", "bg_attic"),
        Chapter(2, "Draught Hall", "A long hall breathes against the tower all night.", "bg_hall"),
        Chapter(3, "Foundry Floor", "Granite courses over a floor that will not hold still.", "bg_foundry")
    )

    private val titles = listOf(
        "Sawdust", "First Draw", "Middle Ground", "Steady Hand", "Two Fingers",
        "Shim Stack", "Long Reach", "Slow Pull", "Bench Test",
        "Wax Seam", "Dust Sheet", "Rafter Line", "Gable End", "Slick Course",
        "Loft Ladder", "Beam Split", "Attic Draft", "Roof Peak",
        "Open Door", "Side Wind", "Hall Column", "Cross Breeze", "Gale Watch",
        "Tall Order", "Whistle Gap", "Window Pane", "Storm Front",
        "Cold Iron", "Press Rhythm", "Granite Row", "Hammer Fall", "Floor Shake",
        "Heavy Course", "Slag Line", "Furnace Hum", "Last Column"
    )

    private val goalPattern = listOf(
        Goal.PULL, Goal.PULL, Goal.HEIGHT, Goal.STEADY, Goal.PULL,
        Goal.RUSH, Goal.HEIGHT, Goal.STEADY, Goal.PULL
    )

    val levels: List<LevelSpec> = buildList {
        for (i in titles.indices) {
            val chapter = i / PER_CHAPTER
            val step = i % PER_CHAPTER
            val ramp = step / (PER_CHAPTER - 1f)
            val goal = goalPattern[step]
            val startLayers = 8 + chapter + (ramp * 2.4f).toInt()
            val pullTarget = 4 + chapter * 2 + (ramp * 6f).toInt()
            val target = when (goal) {
                Goal.PULL -> pullTarget
                Goal.STEADY -> (pullTarget * 0.7f).toInt().coerceAtLeast(3)
                Goal.RUSH -> (pullTarget * 0.8f).toInt().coerceAtLeast(4)
                Goal.HEIGHT -> startLayers + 2 + (ramp * 2f).toInt()
            }
            val timeLimit = when (goal) {
                Goal.RUSH -> 62f - chapter * 4f - ramp * 8f
                else -> 190f - chapter * 10f - ramp * 20f
            }
            val blockCount = startLayers * 3
            val waxed = when (chapter) {
                0 -> 0
                1 -> 3 + (ramp * 4f).toInt()
                2 -> 2 + (ramp * 2f).toInt()
                else -> 2
            }
            val cracked = when (chapter) {
                0 -> if (step >= 4) 1 + (ramp * 2f).toInt() else 0
                1 -> 2 + (ramp * 2f).toInt()
                2 -> 3 + (ramp * 3f).toInt()
                else -> 3
            }
            val stone = when (chapter) {
                0 -> 0
                1 -> 0
                2 -> if (step >= 5) 2 else 0
                else -> 3 + (ramp * 4f).toInt()
            }
            add(
                LevelSpec(
                    index = i,
                    chapter = chapter,
                    title = titles[i],
                    goal = goal,
                    target = target,
                    startLayers = startLayers,
                    timeLimit = timeLimit,
                    gripScale = 0.80f + chapter * 0.14f + ramp * 0.10f,
                    gustStrength = if (chapter == 2) 0.100f + ramp * 0.180f else if (chapter == 3) 0.070f else 0f,
                    gustPeriod = 5.2f - ramp * 1.4f,
                    tapPeriod = if (chapter == 3) 7.0f - ramp * 3.0f else 0f,
                    tapForce = if (chapter == 3) 0.240f + ramp * 0.320f else 0f,
                    stoneCount = stone.coerceAtMost(blockCount / 4),
                    crackedCount = cracked.coerceAtMost(blockCount / 4),
                    waxedCount = waxed.coerceAtMost(blockCount / 4),
                    steadyCap = 0.115f - chapter * 0.008f - ramp * 0.012f
                )
            )
        }
    }

    fun endlessSpec(): LevelSpec = LevelSpec(
        index = -1,
        chapter = -1,
        title = "Long Night",
        goal = Goal.PULL,
        target = 9999,
        startLayers = 10,
        timeLimit = 0f,
        gripScale = 1.05f,
        gustStrength = 0.095f,
        gustPeriod = 4.6f,
        tapPeriod = 9.0f,
        tapForce = 0.260f,
        stoneCount = 3,
        crackedCount = 4,
        waxedCount = 4,
        steadyCap = 0.10f
    )

    val awards = listOf(
        Award("first_pull", "Sawdust", "Pull your first block clear of the tower."),
        Award("first_clear", "Bench Test", "Clear any level on the workbench."),
        Award("ten_levels", "Journeyman", "Clear ten levels."),
        Award("all_levels", "Master Joiner", "Clear every level in every hall."),
        Award("three_star", "Dead Level", "Earn three stars on a level."),
        Award("thirty_stars", "Plumb Line", "Bank thirty stars in total."),
        Award("no_wobble", "Still Air", "Clear a level without the gauge leaving the green."),
        Award("stone_pull", "Quarryman", "Pull a granite block without dropping the tower."),
        Award("tall_tower", "Eighteen Courses", "Grow a tower to eighteen courses."),
        Award("endless_20", "Long Night", "Pull twenty blocks in one endless run."),
        Award("endless_35", "Dawn Shift", "Pull thirty-five blocks in one endless run."),
        Award("hundred", "Hundredweight", "Pull one hundred blocks across all runs.")
    )
}
