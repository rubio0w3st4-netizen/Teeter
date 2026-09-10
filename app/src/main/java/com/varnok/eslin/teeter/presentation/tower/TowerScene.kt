package com.varnok.eslin.teeter.presentation.tower

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.varnok.eslin.teeter.domain.model.BlockKind
import com.varnok.eslin.teeter.domain.model.BlockState
import com.varnok.eslin.teeter.domain.tower.RunState
import com.varnok.eslin.teeter.domain.tower.Tower
import com.varnok.eslin.teeter.domain.tower.TowerEngine
import io.github.sceneview.SceneScope
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView

private const val LENS_FOCAL_MM = 26.0
private const val NEAR_PLANE = 0.12
private const val FAR_PLANE = 300.0
private const val FLOOR_Y = -3.0f

private val POOL = mapOf(
    BlockKind.PINE to 42,
    BlockKind.WAXED to 14,
    BlockKind.CRACKED to 14,
    BlockKind.STONE to 14
)

private val BLOCK_SOURCE_DIMS = mapOf(
    BlockKind.PINE to floatArrayOf(1.9029f, 1.5548f, 1.4966f),
    BlockKind.WAXED to floatArrayOf(1.9030f, 0.5858f, 0.8237f),
    BlockKind.CRACKED to floatArrayOf(1.9030f, 0.6046f, 0.8098f),
    BlockKind.STONE to floatArrayOf(1.9011f, 0.8996f, 0.7407f)
)

data class HudFrame(
    val tick: Int,
    val running: Boolean,
    val pulled: Int,
    val courses: Int,
    val steady: Float,
    val timeLeft: Float,
    val load: Float,
    val selected: Boolean,
    val slide: Float,
    val jolt: Float
)

private class Rig {
    val pine = ArrayList<ModelNode>()
    val waxed = ArrayList<ModelNode>()
    val cracked = ArrayList<ModelNode>()
    val stone = ArrayList<ModelNode>()
    val table = ArrayList<ModelNode>()
    val lanterns = ArrayList<ModelNode>()
    val crates = ArrayList<ModelNode>()
    val cursor = IntArray(4)
    var last = 0L
    var sceneryDone = false

    fun poolFor(kind: BlockKind): ArrayList<ModelNode> = when (kind) {
        BlockKind.PINE -> pine
        BlockKind.WAXED -> waxed
        BlockKind.CRACKED -> cracked
        BlockKind.STONE -> stone
    }
}

@Composable
fun TowerScene(
    engine: TowerEngine,
    paused: Boolean,
    modifier: Modifier = Modifier,
    onHud: (HudFrame) -> Unit
) {
    val filament = rememberEngine(engineCreator = { egl ->
        Engine.Builder()
            .sharedContext(egl)
            .feature("backend.disable_parallel_shader_compile", true)
            .build()
    })
    val modelLoader = rememberModelLoader(filament)
    val rig = remember(engine) { Rig() }
    val camera = rememberCameraNode(filament)
    val mainLight = rememberMainLightNode(filament) { intensity = 112_000f }
    val bloomOff = remember { View.BloomOptions().apply { enabled = false } }
    val dynResOff = remember { View.DynamicResolutionOptions().apply { enabled = false } }
    val view = rememberView(filament).apply {
        isPostProcessingEnabled = false
        bloomOptions = bloomOff
        dynamicResolutionOptions = dynResOff
        setShadowingEnabled(false)
        setScreenSpaceRefractionEnabled(false)
    }

    SceneView(
        modifier = modifier,
        engine = filament,
        modelLoader = modelLoader,
        view = view,
        isOpaque = false,
        surfaceType = SurfaceType.TextureSurface,
        cameraNode = camera,
        mainLightNode = mainLight,
        onFrame = { nanos ->
            view.isPostProcessingEnabled = false
            view.bloomOptions = bloomOff
            view.dynamicResolutionOptions = dynResOff
            view.setShadowingEnabled(false)
            view.setScreenSpaceRefractionEnabled(false)

            val raw = if (rig.last == 0L) 0f else (nanos - rig.last) / 1_000_000_000f
            rig.last = nanos
            val dt = raw.coerceIn(0f, 0.05f)

            engine.paused = paused
            engine.update(dt)

            val viewport = view.viewport
            if (viewport.height > 0) {
                engine.viewW = viewport.width.toFloat()
                engine.viewH = viewport.height.toFloat()
                camera.setLensProjection(
                    LENS_FOCAL_MM,
                    viewport.width.toDouble() / viewport.height.toDouble(),
                    NEAR_PLANE,
                    FAR_PLANE
                )
            }

            placeScenery(rig)
            placeBlocks(rig, engine)
            camera.worldPosition = Position(engine.camX(), engine.camY(), engine.camZ())
            camera.lookAt(Position(0f, engine.camTargetY(), 0f))

            val sel = engine.current()
            onHud(
                HudFrame(
                    tick = engine.tick,
                    running = engine.state == RunState.RUNNING,
                    pulled = engine.pulled,
                    courses = engine.layerCount,
                    steady = engine.steady(),
                    timeLeft = engine.timeLeft,
                    load = if (sel != null) engine.loadOn(sel) else 0f,
                    selected = sel != null,
                    slide = sel?.slide ?: 0f,
                    jolt = engine.lastJolt
                )
            )
        },
        content = {
            fill(modelLoader, "models/table.glb", 1, rig.table)
            fill(modelLoader, "models/lantern.glb", 2, rig.lanterns)
            fill(modelLoader, "models/crate.glb", 3, rig.crates)
            fill(modelLoader, BlockKind.PINE.asset, POOL[BlockKind.PINE]!!, rig.pine)
            fill(modelLoader, BlockKind.WAXED.asset, POOL[BlockKind.WAXED]!!, rig.waxed)
            fill(modelLoader, BlockKind.CRACKED.asset, POOL[BlockKind.CRACKED]!!, rig.cracked)
            fill(modelLoader, BlockKind.STONE.asset, POOL[BlockKind.STONE]!!, rig.stone)
        }
    )
}

@Composable
private fun SceneScope.fill(
    modelLoader: ModelLoader,
    path: String,
    count: Int,
    out: ArrayList<ModelNode>
) {
    val instances: List<ModelInstance> = remember(path to count) {
        modelLoader.createInstancedModel(path, count)
    }
    instances.forEach { instance ->
        ModelNode(
            modelInstance = instance,
            isVisible = false,
            apply = { out.add(this) }
        )
    }
}

private fun placeScenery(rig: Rig) {
    if (rig.sceneryDone) return
    if (rig.table.isEmpty()) return
    val t = rig.table[0]
    t.scale = Scale(8.6f / 1.902f, 3.0f / 0.9551f, 8.6f / 1.8996f)
    t.worldPosition = Position(0f, FLOOR_Y * 0.5f, 0f)
    t.worldRotation = Rotation(0f, 0f, 0f)
    t.isVisible = true
    for (i in rig.lanterns.indices) {
        val n = rig.lanterns[i]
        val s = 2.7f / 1.9026f
        n.scale = Scale(s, s, s)
        val side = if (i == 0) 1f else -1f
        n.worldPosition = Position(side * 7.4f, FLOOR_Y + 1.35f, -5.6f + side * 2.2f)
        n.worldRotation = Rotation(0f, side * 24f, 0f)
        n.isVisible = true
    }
    for (i in rig.crates.indices) {
        val n = rig.crates[i]
        val s = (1.7f + i * 0.35f) / 1.9026f
        n.scale = Scale(s, s, s)
        val ang = 2.1f + i * 1.9f
        n.worldPosition = Position(
            kotlin.math.cos(ang) * (7.5f + i * 1.4f),
            FLOOR_Y + (1.7f + i * 0.35f) * 0.5f,
            kotlin.math.sin(ang) * (7.5f + i * 1.4f)
        )
        n.worldRotation = Rotation(0f, ang * 42f, 0f)
        n.isVisible = true
    }
    rig.sceneryDone = true
}

private fun placeBlocks(rig: Rig, engine: TowerEngine) {
    rig.cursor.fill(0)
    for (b in engine.blocks) {
        val pool = rig.poolFor(b.kind)
        val idx = rig.cursor[b.kind.ordinal]
        if (idx >= pool.size) continue
        val node = pool[idx]
        rig.cursor[b.kind.ordinal] = idx + 1
        val src = BLOCK_SOURCE_DIMS[b.kind]!!
        node.scale = Scale(
            Tower.BLOCK_LEN * 0.985f / src[0],
            Tower.BLOCK_H * 0.90f / src[1],
            Tower.BLOCK_W * 0.90f / src[2]
        )
        node.worldPosition = Position(b.px, b.py, b.pz)
        node.worldRotation = if (b.state == BlockState.FALLING) {
            Rotation(b.pitch, b.yaw, b.roll)
        } else {
            Rotation(0f, b.yaw, 0f)
        }
        node.isVisible = true
    }
    hideTail(rig.pine, rig.cursor[BlockKind.PINE.ordinal])
    hideTail(rig.waxed, rig.cursor[BlockKind.WAXED.ordinal])
    hideTail(rig.cracked, rig.cursor[BlockKind.CRACKED.ordinal])
    hideTail(rig.stone, rig.cursor[BlockKind.STONE.ordinal])
}

private fun hideTail(pool: ArrayList<ModelNode>, from: Int) {
    for (i in from until pool.size) pool[i].isVisible = false
}
