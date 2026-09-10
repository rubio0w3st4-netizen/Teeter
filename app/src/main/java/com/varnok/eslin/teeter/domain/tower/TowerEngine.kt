package com.varnok.eslin.teeter.domain.tower

import com.varnok.eslin.teeter.domain.model.BlockKind
import com.varnok.eslin.teeter.domain.model.BlockState
import com.varnok.eslin.teeter.domain.model.Goal
import com.varnok.eslin.teeter.domain.model.LevelSpec
import com.varnok.eslin.teeter.domain.model.TowerBlock
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class RunState { RUNNING, WON, LOST }

enum class RunFault { NONE, COLLAPSE, TIMEOUT, SHAKEN }

object Tower {
    const val BLOCK_LEN = 3.0f
    const val BLOCK_W = 1.0f
    const val BLOCK_H = 0.62f
    const val SLIDE_MAX = 2.9f
    const val HALF_SPAN = 1.5f
    const val STEP = 1f / 120f
    const val STIFFNESS = 30f
    const val DAMPING = 3.4f
    const val JOLT = 0.115f
    const val PULL_RATE = 0.9f
    const val RETURN_RATE = 2.6f
    const val CARRY_TIME = 0.62f
    const val LEAN_LIMIT = 0.20f
    const val CAM_MIN_PITCH = 0.06f
    const val CAM_MAX_PITCH = 0.85f
    const val FOCAL_MM = 26.0f
}

class TowerEngine(
    val spec: LevelSpec,
    val endless: Boolean,
    seed: Long
) {
    private val rng = Random(seed)
    private val layers = ArrayList<Array<TowerBlock?>>()
    val blocks = ArrayList<TowerBlock>()

    var state: RunState = RunState.RUNNING
        private set
    var fault: RunFault = RunFault.NONE
        private set
    var pulled: Int = 0
        private set
    var stonePulled: Boolean = false
        private set
    var elapsed: Float = 0f
        private set
    var maxLean: Float = 0f
        private set
    var leanX: Float = 0f
        private set
    var leanZ: Float = 0f
        private set
    var selectedId: Int = -1
        private set
    var pullHeld: Boolean = false
    var paused: Boolean = false
    var camYaw: Float = 0.45f
        private set
    var camPitch: Float = 0.34f
        private set
    var tick: Int = 0
        private set
    var lastJolt: Float = 0f
        private set
    var placedFlash: Float = 0f
        private set

    private var leanVelX = 0f
    private var leanVelZ = 0f
    private var gustPhase = 0f
    private var tapTimer = 0f
    private var acc = 0f
    private var nextId = 0
    private var settleTimer = 0f

    var viewW: Float = 1080f
    var viewH: Float = 2000f

    val timeLimit: Float get() = spec.timeLimit
    val timeLeft: Float get() = if (endless) 0f else max(0f, spec.timeLimit - elapsed)
    val layerCount: Int get() = layers.size
    val towerTop: Float get() = layers.size * Tower.BLOCK_H

    init {
        buildTower()
        syncAll()
    }

    private fun buildTower() {
        val total = spec.blockCount
        val kinds = ArrayList<BlockKind>(total)
        repeat(spec.stoneCount) { kinds.add(BlockKind.STONE) }
        repeat(spec.crackedCount) { kinds.add(BlockKind.CRACKED) }
        repeat(spec.waxedCount) { kinds.add(BlockKind.WAXED) }
        while (kinds.size < total) kinds.add(BlockKind.PINE)
        while (kinds.size > total) kinds.removeAt(kinds.lastIndex)
        for (i in kinds.indices.reversed()) {
            val j = rng.nextInt(i + 1)
            val t = kinds[i]; kinds[i] = kinds[j]; kinds[j] = t
        }
        var k = 0
        for (l in 0 until spec.startLayers) {
            val row = arrayOfNulls<TowerBlock>(3)
            for (s in 0..2) {
                val b = TowerBlock(nextId++, kinds[k++], l, s)
                row[s] = b
                blocks.add(b)
            }
            layers.add(row)
        }
    }

    fun orbit(dxPx: Float, dyPx: Float) {
        camYaw -= dxPx * 0.006f
        camPitch = (camPitch + dyPx * 0.004f).coerceIn(Tower.CAM_MIN_PITCH, Tower.CAM_MAX_PITCH)
    }

    fun camDistance(): Float = 12.2f + layers.size * 0.52f

    fun camTargetY(): Float = towerTop * 0.52f + 0.5f

    fun camX(): Float = camDistance() * cos(camPitch) * sin(camYaw)
    fun camY(): Float = camTargetY() + camDistance() * sin(camPitch)
    fun camZ(): Float = camDistance() * cos(camPitch) * cos(camYaw)

    fun topCompleteLayer(): Int {
        var top = -1
        for (l in layers.indices) if (layers[l].all { it != null }) top = l
        return top
    }

    fun maxPullLayer(): Int = topCompleteLayer() - 1

    fun isPullable(b: TowerBlock): Boolean =
        b.state == BlockState.SEATED && b.layer in 0..maxPullLayer()

    fun select(id: Int) {
        val b = blocks.firstOrNull { it.id == id } ?: return
        if (!isPullable(b)) return
        if (selectedId != id) {
            val cur = current()
            if (cur != null && cur.state == BlockState.SLIDING && cur.slide > 0f) return
            selectedId = id
        }
    }

    fun clearSelection() {
        val cur = current() ?: return
        if (cur.slide <= 0.02f) selectedId = -1
    }

    fun current(): TowerBlock? = blocks.firstOrNull { it.id == selectedId }

    fun steady(): Float = (hypot(leanX, leanZ) / Tower.LEAN_LIMIT).coerceIn(0f, 1f)

    fun stars(): Int {
        if (state != RunState.WON) return 0
        var s = 1
        if (maxLean <= spec.steadyCap) s++
        if (endless || timeLeft > spec.timeLimit * 0.30f) s++
        return s
    }

    fun update(dt: Float) {
        if (paused || state != RunState.RUNNING) {
            if (state != RunState.RUNNING) {
                acc += min(dt, 0.1f)
                while (acc >= Tower.STEP) { acc -= Tower.STEP; fallStep(Tower.STEP) }
                syncAll()
            }
            return
        }
        acc += min(dt, 0.1f)
        while (acc >= Tower.STEP) {
            acc -= Tower.STEP
            step(Tower.STEP)
            if (state != RunState.RUNNING) break
        }
        tick++
        syncAll()
    }

    private fun step(h: Float) {
        elapsed += h
        placedFlash = max(0f, placedFlash - h)
        lastJolt = max(0f, lastJolt - h * 3f)

        val sel = current()
        if (sel != null && sel.state != BlockState.CARRIED) {
            if (pullHeld && sel.state != BlockState.CARRIED) {
                sel.state = BlockState.SLIDING
                val load = loadOn(sel)
                val grip = sel.kind.grip * spec.gripScale
                val rate = Tower.PULL_RATE / (1f + load * grip * 0.24f)
                sel.slide = min(1f, sel.slide + rate * h)
                val push = load * grip * Tower.JOLT * h
                applyPullForce(sel, push)
                lastJolt = min(1f, lastJolt + push * 2.2f)
                if (sel.slide >= 1f) liftOut(sel)
            } else if (sel.slide > 0f) {
                sel.slide = max(0f, sel.slide - Tower.RETURN_RATE * h)
                if (sel.slide == 0f) sel.state = BlockState.SEATED
            }
        }

        for (b in blocks) if (b.state == BlockState.CARRIED) {
            b.carryT += h / Tower.CARRY_TIME
            if (b.carryT >= 1f) seatOnTop(b)
        }

        if (spec.gustStrength > 0f) {
            gustPhase += h * (6.2831853f / max(0.8f, spec.gustPeriod))
            val g = spec.gustStrength * sin(gustPhase)
            val dir = gustPhase * 0.21f
            leanVelX += g * cos(dir) * h
            leanVelZ += g * sin(dir) * h
        }
        if (spec.tapPeriod > 0f) {
            tapTimer += h
            if (tapTimer >= spec.tapPeriod) {
                tapTimer = 0f
                val a = rng.nextFloat() * 6.2831853f
                leanVelX += spec.tapForce * cos(a)
                leanVelZ += spec.tapForce * sin(a)
                lastJolt = 1f
            }
        }

        val margin = stabilityMargin()
        val soft = (0.25f + margin * 1.4f).coerceIn(0.16f, 1f)
        leanVelX += (-leanX * Tower.STIFFNESS * soft) * h
        leanVelZ += (-leanZ * Tower.STIFFNESS * soft) * h
        val damp = 1f - Tower.DAMPING * h
        leanVelX *= damp
        leanVelZ *= damp
        leanX += leanVelX * h
        leanZ += leanVelZ * h

        val lean = hypot(leanX, leanZ)
        if (lean > maxLean) maxLean = lean

        if (spec.goal == Goal.STEADY && lean > spec.steadyCap && pulled < spec.target) {
            finish(RunState.LOST, RunFault.SHAKEN)
            return
        }
        if (margin < 0f || lean > Tower.LEAN_LIMIT) {
            finish(RunState.LOST, RunFault.COLLAPSE)
            return
        }
        if (!endless && spec.timeLimit > 0f && elapsed >= spec.timeLimit) {
            finish(RunState.LOST, RunFault.TIMEOUT)
            return
        }
        if (!endless && goalMet()) finish(RunState.WON, RunFault.NONE)
    }

    private fun goalMet(): Boolean = when (spec.goal) {
        Goal.PULL -> pulled >= spec.target
        Goal.RUSH -> pulled >= spec.target
        Goal.STEADY -> pulled >= spec.target
        Goal.HEIGHT -> layers.size >= spec.target
    }

    private fun finish(s: RunState, f: RunFault) {
        state = s
        fault = f
        if (f == RunFault.COLLAPSE || f == RunFault.SHAKEN) scatter()
    }

    private fun applyPullForce(b: TowerBlock, push: Float) {
        if (b.layer % 2 == 0) leanVelX += push * b.slideDir * 0.9f
        else leanVelZ += push * b.slideDir * 0.9f
        val a = rng.nextFloat() * 6.2831853f
        leanVelX += push * 0.35f * cos(a)
        leanVelZ += push * 0.35f * sin(a)
    }

    private fun liftOut(b: TowerBlock) {
        layers[b.layer][b.slot] = null
        b.state = BlockState.CARRIED
        b.carryT = 0f
        b.carryFromX = b.px
        b.carryFromY = b.py
        b.carryFromZ = b.pz
        pulled++
        if (b.kind == BlockKind.STONE) stonePulled = true
        selectedId = -1
        settleTimer = 0.2f
    }

    private fun seatOnTop(b: TowerBlock) {
        val last = layers.lastIndex
        if (last < 0 || layers[last].all { it != null }) layers.add(arrayOfNulls(3))
        val l = layers.lastIndex
        var s = layers[l].indexOfFirst { it == null }
        if (s < 0) { layers.add(arrayOfNulls(3)); s = 0 }
        val li = layers.lastIndex
        layers[li][s] = b
        b.layer = li
        b.slot = s
        b.slide = 0f
        b.state = BlockState.SEATED
        placedFlash = 0.4f
        val impulse = b.kind.mass * 0.05f
        val a = rng.nextFloat() * 6.2831853f
        leanVelX += impulse * cos(a)
        leanVelZ += impulse * sin(a)
    }

    private fun massAbove(layer: Int): Float {
        var m = 0f
        for (b in blocks) {
            if (b.state == BlockState.CARRIED) { m += b.kind.mass; continue }
            if (b.state == BlockState.SEATED || b.state == BlockState.SLIDING) {
                if (b.layer >= layer) m += b.kind.mass
            }
        }
        return m
    }

    fun loadOn(b: TowerBlock): Float {
        val above = massAbove(b.layer + 1)
        if (above <= 0f) return 0f
        val row = layers[b.layer]
        val present = ArrayList<TowerBlock>(3)
        for (s in 0..2) row[s]?.let { present.add(it) }
        if (present.isEmpty()) return above
        val comAxis = comAboveAxis(b.layer)
        var totalW = 0f
        var mine = 0f
        for (p in present) {
            val pos = slotOffset(p.slot)
            val base = if (p.slot == 1) 0.22f else 0.39f
            val skew = (1f + 1.1f * comAxis * sign(pos)).coerceAtLeast(0.12f)
            val w = base * skew
            totalW += w
            if (p.id == b.id) mine = w
        }
        if (totalW <= 0f) return above / present.size
        return above * (mine / totalW)
    }

    private fun comAboveAxis(layer: Int): Float {
        var m = 0f
        var sx = 0f
        var sz = 0f
        for (b in blocks) {
            if (b.state != BlockState.SEATED && b.state != BlockState.SLIDING) continue
            if (b.layer <= layer) continue
            m += b.kind.mass
            sx += b.kind.mass * baseX(b)
            sz += b.kind.mass * baseZ(b)
        }
        if (m <= 0f) return 0f
        val cx = sx / m
        val cz = sz / m
        return (if (layer % 2 == 0) cz else cx) / Tower.BLOCK_W
    }

    private fun stabilityMargin(): Float {
        var worst = 1f
        for (l in 1 until layers.size) {
            val below = layers[l - 1]
            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
            var any = false
            for (s in 0..2) {
                val b = below[s] ?: continue
                any = true
                val hx: Float; val hz: Float
                if (b.layer % 2 == 0) { hx = Tower.HALF_SPAN; hz = Tower.BLOCK_W * 0.5f }
                else { hx = Tower.BLOCK_W * 0.5f; hz = Tower.HALF_SPAN }
                val bx = baseX(b); val bz = baseZ(b)
                minX = min(minX, bx - hx); maxX = max(maxX, bx + hx)
                minZ = min(minZ, bz - hz); maxZ = max(maxZ, bz + hz)
            }
            if (!any) return -1f
            var m = 0f
            var sx = 0f
            var sz = 0f
            var sy = 0f
            for (b in blocks) {
                if (b.state == BlockState.CARRIED) {
                    m += b.kind.mass; sx += b.kind.mass * 0f; sz += b.kind.mass * 0f
                    sy += b.kind.mass * (towerTop + 1.2f)
                    continue
                }
                if (b.state != BlockState.SEATED && b.state != BlockState.SLIDING) continue
                if (b.layer < l) continue
                m += b.kind.mass
                sx += b.kind.mass * baseX(b)
                sz += b.kind.mass * baseZ(b)
                sy += b.kind.mass * layerY(b.layer)
            }
            if (m <= 0f) continue
            val cx = sx / m
            val cz = sz / m
            val cy = sy / m
            val armY = cy - (l * Tower.BLOCK_H)
            val ex = cx + leanX * armY
            val ez = cz + leanZ * armY
            val dx = min(ex - minX, maxX - ex)
            val dz = min(ez - minZ, maxZ - ez)
            val d = min(dx, dz) / Tower.HALF_SPAN
            if (d < worst) worst = d
        }
        return worst
    }

    fun marginNow(): Float = stabilityMargin()

    private fun scatter() {
        val dirX = if (hypot(leanX, leanZ) > 1e-4f) leanX else 0.4f
        val dirZ = if (hypot(leanX, leanZ) > 1e-4f) leanZ else 0.2f
        val n = hypot(dirX, dirZ).coerceAtLeast(1e-4f)
        for (b in blocks) {
            if (b.state == BlockState.FALLING) continue
            b.state = BlockState.FALLING
            val h = layerY(b.layer)
            b.vx = dirX / n * (1.4f + h * 0.55f) + (rng.nextFloat() - 0.5f) * 1.6f
            b.vz = dirZ / n * (1.4f + h * 0.55f) + (rng.nextFloat() - 0.5f) * 1.6f
            b.vy = rng.nextFloat() * 1.1f
            b.spinX = (rng.nextFloat() - 0.5f) * 420f
            b.spinY = (rng.nextFloat() - 0.5f) * 320f
            b.spinZ = (rng.nextFloat() - 0.5f) * 420f
        }
    }

    private fun fallStep(h: Float) {
        for (b in blocks) {
            if (b.state != BlockState.FALLING) continue
            b.vy -= 17f * h
            b.px += b.vx * h
            b.py += b.vy * h
            b.pz += b.vz * h
            b.pitch += b.spinX * h
            b.yaw += b.spinY * h
            b.roll += b.spinZ * h
            if (b.py < Tower.BLOCK_H * 0.5f) {
                b.py = Tower.BLOCK_H * 0.5f
                b.vy = -b.vy * 0.26f
                b.vx *= 0.72f
                b.vz *= 0.72f
                b.spinX *= 0.45f
                b.spinY *= 0.45f
                b.spinZ *= 0.45f
                if (abs(b.vy) < 0.35f) { b.vy = 0f; b.spinX = 0f; b.spinY *= 0.5f; b.spinZ = 0f }
            }
        }
    }

    private fun slotOffset(slot: Int): Float = (slot - 1) * Tower.BLOCK_W

    private fun layerY(l: Int): Float = l * Tower.BLOCK_H + Tower.BLOCK_H * 0.5f

    private fun baseX(b: TowerBlock): Float {
        val off = if (b.layer % 2 == 0) 0f else slotOffset(b.slot)
        val slide = if (b.state == BlockState.SLIDING && b.layer % 2 == 0) b.slideDir * b.slide * Tower.SLIDE_MAX else 0f
        return off + slide
    }

    private fun baseZ(b: TowerBlock): Float {
        val off = if (b.layer % 2 == 0) slotOffset(b.slot) else 0f
        val slide = if (b.state == BlockState.SLIDING && b.layer % 2 != 0) b.slideDir * b.slide * Tower.SLIDE_MAX else 0f
        return off + slide
    }

    private fun syncAll() {
        val cx = camX(); val cz = camZ()
        for (b in blocks) {
            when (b.state) {
                BlockState.FALLING -> {}
                BlockState.CARRIED -> {
                    val t = b.carryT.coerceIn(0f, 1f)
                    val e = t * t * (3f - 2f * t)
                    val tx = 0f
                    val ty = towerTop + Tower.BLOCK_H * 1.6f
                    val tz = 0f
                    b.px = b.carryFromX + (tx - b.carryFromX) * e
                    b.py = b.carryFromY + (ty - b.carryFromY) * e + sin(e * 3.1415927f) * 1.1f
                    b.pz = b.carryFromZ + (tz - b.carryFromZ) * e
                    b.yaw = if (layers.lastIndex % 2 == 0) 0f else 90f
                    b.pitch = 0f
                    b.roll = 0f
                }
                else -> {
                    if (b.state == BlockState.SEATED && b.id == selectedId) {
                        b.slideDir = if (b.layer % 2 == 0) {
                            if (cx >= 0f) 1f else -1f
                        } else {
                            if (cz >= 0f) 1f else -1f
                        }
                    }
                    val y = layerY(b.layer)
                    b.px = baseX(b) + leanX * y
                    b.py = y
                    b.pz = baseZ(b) + leanZ * y
                    b.yaw = if (b.layer % 2 == 0) 0f else 90f
                    b.pitch = 0f
                    b.roll = 0f
                }
            }
        }
    }

    fun screenPos(b: TowerBlock, out: FloatArray): Boolean {
        val ex = camX(); val ey = camY(); val ez = camZ()
        val tx = 0f; val ty = camTargetY(); val tz = 0f
        var fx = tx - ex; var fy = ty - ey; var fz = tz - ez
        val fl = sqrt(fx * fx + fy * fy + fz * fz).coerceAtLeast(1e-5f)
        fx /= fl; fy /= fl; fz /= fl
        var rx = -fz
        var ry = 0f
        var rz = fx
        val rl = sqrt(rx * rx + ry * ry + rz * rz).coerceAtLeast(1e-5f)
        rx /= rl; ry /= rl; rz /= rl
        val ux = ry * fz - rz * fy
        val uy = rz * fx - rx * fz
        val uz = rx * fy - ry * fx
        val dx = b.px - ex; val dy = b.py - ey; val dz = b.pz - ez
        val zc = dx * fx + dy * fy + dz * fz
        if (zc <= 0.2f) return false
        val xc = dx * rx + dy * ry + dz * rz
        val yc = dx * ux + dy * uy + dz * uz
        val tanHalfV = 12f / Tower.FOCAL_MM
        val aspect = viewW / max(1f, viewH)
        val ndcX = (xc / zc) / (tanHalfV * aspect)
        val ndcY = (yc / zc) / tanHalfV
        out[0] = (ndcX * 0.5f + 0.5f) * viewW
        out[1] = (0.5f - ndcY * 0.5f) * viewH
        out[2] = zc
        return true
    }

    fun pick(sx: Float, sy: Float): Int {
        val out = FloatArray(3)
        var best = -1
        var bestScore = Float.MAX_VALUE
        for (b in blocks) {
            if (!isPullable(b)) continue
            if (!screenPos(b, out)) continue
            val d = hypot(out[0] - sx, out[1] - sy)
            if (d > viewW * 0.11f) continue
            val score = d + out[2] * 6f
            if (score < bestScore) { bestScore = score; best = b.id }
        }
        return best
    }

    fun forceLose() {
        if (state == RunState.RUNNING) finish(RunState.LOST, RunFault.COLLAPSE)
    }
}
