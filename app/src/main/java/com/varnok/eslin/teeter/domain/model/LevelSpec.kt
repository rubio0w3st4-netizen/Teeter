package com.varnok.eslin.teeter.domain.model

enum class Goal { PULL, HEIGHT, STEADY, RUSH }

data class LevelSpec(
    val index: Int,
    val chapter: Int,
    val title: String,
    val goal: Goal,
    val target: Int,
    val startLayers: Int,
    val timeLimit: Float,
    val gripScale: Float,
    val gustStrength: Float,
    val gustPeriod: Float,
    val tapPeriod: Float,
    val tapForce: Float,
    val stoneCount: Int,
    val crackedCount: Int,
    val waxedCount: Int,
    val steadyCap: Float
) {
    val blockCount: Int get() = startLayers * 3
}

data class Chapter(
    val index: Int,
    val title: String,
    val blurb: String,
    val backdrop: String
)

data class Award(
    val id: String,
    val title: String,
    val detail: String
)

data class LevelProgress(
    val stars: Int,
    val bestPulled: Int,
    val unlocked: Boolean
)

data class RunStats(
    val runs: Int,
    val pulled: Int,
    val collapses: Int,
    val cleared: Int,
    val stars: Int,
    val bestEndless: Int,
    val tallest: Int
)
