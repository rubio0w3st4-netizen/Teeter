package com.varnok.eslin.teeter.domain.model

enum class BlockKind(val mass: Float, val grip: Float, val asset: String) {
    PINE(1.0f, 1.0f, "models/block_pine.glb"),
    WAXED(0.95f, 0.42f, "models/block_waxed.glb"),
    CRACKED(0.8f, 1.35f, "models/block_cracked.glb"),
    STONE(2.4f, 1.1f, "models/block_stone.glb")
}

enum class BlockState { SEATED, SLIDING, CARRIED, FALLING }

class TowerBlock(
    val id: Int,
    val kind: BlockKind,
    var layer: Int,
    var slot: Int
) {
    var state: BlockState = BlockState.SEATED
    var slide: Float = 0f
    var slideDir: Float = 1f
    var carryT: Float = 0f
    var carryFromX: Float = 0f
    var carryFromY: Float = 0f
    var carryFromZ: Float = 0f
    var px: Float = 0f
    var py: Float = 0f
    var pz: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var vz: Float = 0f
    var yaw: Float = 0f
    var pitch: Float = 0f
    var roll: Float = 0f
    var spinX: Float = 0f
    var spinY: Float = 0f
    var spinZ: Float = 0f
}
