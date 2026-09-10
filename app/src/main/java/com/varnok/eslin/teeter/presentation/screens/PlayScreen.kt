package com.varnok.eslin.teeter.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varnok.eslin.teeter.audio.Sfx
import com.varnok.eslin.teeter.domain.model.Goal
import com.varnok.eslin.teeter.domain.tower.RunFault
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import com.varnok.eslin.teeter.presentation.tower.HudFrame
import com.varnok.eslin.teeter.presentation.tower.TowerScene
import com.varnok.eslin.teeter.presentation.ui.Emblem
import com.varnok.eslin.teeter.presentation.ui.GameFonts
import com.varnok.eslin.teeter.presentation.ui.OrnateButton
import com.varnok.eslin.teeter.presentation.ui.Palette
import com.varnok.eslin.teeter.presentation.ui.backdropKeys
import com.varnok.eslin.teeter.presentation.ui.drawableId
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun PlayScreen(vm: TeeterViewModel) {
    val engine = vm.engine ?: return
    BackHandler { if (!vm.paused) vm.togglePause() }

    var hud by remember(engine) {
        mutableStateOf(HudFrame(0, true, 0, engine.layerCount, 0f, engine.timeLeft, 0f, false, 0f, 0f))
    }
    val marker = remember { FloatArray(3) }
    val bgKey = if (vm.endless) backdropKeys[3] else backdropKeys[engine.spec.chapter.coerceIn(0, 3)]
    val bgId = drawableId(bgKey)

    LaunchedEffect(hud.pulled) {
        if (hud.pulled > 0) {
            vm.sound.play(Sfx.PLACE)
            vm.haptics.nudge()
        }
    }
    LaunchedEffect(hud.selected, hud.slide > 0.02f) {
        if (hud.selected && hud.slide > 0.02f) vm.sound.startSlide() else vm.sound.stopSlide()
    }
    LaunchedEffect(hud.running) {
        if (!hud.running) {
            vm.sound.stopSlide()
            if (engine.fault == RunFault.NONE) {
                vm.sound.play(Sfx.WIN)
                vm.haptics.success()
            } else {
                vm.sound.play(Sfx.CRASH)
                vm.haptics.error()
            }
            delay(1700)
            vm.settleRun()
        }
    }

    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        if (bgId != 0) {
            Image(
                painter = painterResource(bgId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Palette.Ink.copy(alpha = 0.72f),
                        Palette.Ink.copy(alpha = 0.34f),
                        Palette.Ink.copy(alpha = 0.86f)
                    )
                )
            )
        )

        TowerScene(
            engine = engine,
            paused = vm.paused,
            modifier = Modifier.fillMaxSize()
        ) { hud = it }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(engine) {
                    detectDragGestures { change, drag ->
                        engine.orbit(drag.x, drag.y)
                        change.consume()
                    }
                }
                .pointerInput(engine) {
                    detectTapGestures { offset ->
                        val id = engine.pick(offset.x, offset.y)
                        if (id >= 0) {
                            engine.select(id)
                            vm.sound.play(Sfx.TAP)
                            vm.haptics.tick()
                        } else {
                            engine.clearSelection()
                        }
                    }
                }
        )

        if (vm.guidesOn) {
            Canvas(Modifier.fillMaxSize()) {
                val frame = hud
                if (!frame.running) return@Canvas
                for (b in engine.blocks) {
                    if (!engine.isPullable(b)) continue
                    if (!engine.screenPos(b, marker)) continue
                    val chosen = b.id == engine.selectedId
                    drawCircle(
                        color = if (chosen) Palette.Amber else Color(0x66F2E3C8),
                        radius = if (chosen) 20f else 7f,
                        center = Offset(marker[0], marker[1]),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (chosen) 5f else 2.5f)
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrnateButton(
                    text = "Pause",
                    onClick = { vm.togglePause() },
                    modifier = Modifier.height(44.dp).width(100.dp),
                    fillColor = Palette.Slate,
                    fontSize = 14
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (vm.endless) "Long Night" else engine.spec.title,
                        color = Palette.Cream,
                        fontFamily = GameFonts.display,
                        fontSize = 20.sp
                    )
                    Text(
                        goalProgress(vm, hud),
                        color = Palette.Brass,
                        fontFamily = GameFonts.hud,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (!vm.endless) {
                        Text(
                            clock(hud.timeLeft),
                            color = if (hud.timeLeft < 15f) Palette.Danger else Palette.Cream,
                            fontFamily = GameFonts.hud,
                            fontSize = 20.sp
                        )
                    }
                    Text(
                        "${hud.courses} courses",
                        color = Palette.Muted,
                        fontFamily = GameFonts.hud,
                        fontSize = 13.sp
                    )
                }
            }

            SteadyGauge(hud.steady, engine.spec.steadyCap / 0.20f)

            Spacer(Modifier.weight(1f))

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    hint(hud),
                    color = Palette.Cream.copy(alpha = 0.85f),
                    fontFamily = GameFonts.hud,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoadPip(hud.load, Modifier.weight(0.34f))
                    Box(
                        Modifier
                            .weight(0.66f)
                            .height(74.dp)
                    ) {
                        OrnateButton(
                            text = if (hud.selected) "Draw" else "Choose a block",
                            onClick = { },
                            modifier = Modifier.fillMaxSize(),
                            fillColor = if (hud.selected) Palette.Sage else Palette.Slate,
                            fontSize = if (hud.selected) 26 else 17,
                            enabled = hud.selected
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .pointerInput(engine) {
                                    detectTapGestures(
                                        onPress = {
                                            engine.pullHeld = true
                                            vm.haptics.tick()
                                            tryAwaitRelease()
                                            engine.pullHeld = false
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }

        if (vm.paused) {
            PauseCurtain(vm)
        }
    }
}

@Composable
private fun SteadyGauge(steady: Float, capFraction: Float) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "STEADY",
                color = Palette.Muted,
                fontFamily = GameFonts.hud,
                fontSize = 11.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${(steady * 100).roundToInt()}%",
                color = gaugeColor(steady),
                fontFamily = GameFonts.hud,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Palette.Bark.copy(alpha = 0.85f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(steady.coerceIn(0f, 1f))
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Palette.Sage, Palette.Amber, Palette.Danger)
                        )
                    )
            )
            Box(
                Modifier
                    .fillMaxWidth(capFraction.coerceIn(0.05f, 0.98f))
                    .fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(Modifier.width(2.dp).fillMaxSize().background(Palette.Cream.copy(alpha = 0.7f)))
            }
        }
    }
}

@Composable
private fun LoadPip(load: Float, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Bark.copy(alpha = 0.82f))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "LOAD",
            color = Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 11.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Emblem("ic_course", 20)
            Spacer(Modifier.size(6.dp))
            Text(
                String.format("%.1f", load),
                color = when {
                    load < 2f -> Palette.Sage
                    load < 5f -> Palette.Amber
                    else -> Palette.Danger
                },
                fontFamily = GameFonts.display,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
private fun PauseCurtain(vm: TeeterViewModel) {
    Box(
        Modifier.fillMaxSize().background(Palette.Ink.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Hands off",
                color = Palette.Amber,
                fontFamily = GameFonts.display,
                fontSize = 34.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "The tower is holding its breath.",
                color = Palette.Cream.copy(alpha = 0.75f),
                fontFamily = GameFonts.hud,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(26.dp))
            OrnateButton(
                "Resume", { vm.togglePause() },
                Modifier.fillMaxWidth().height(62.dp), Palette.Sage, fontSize = 21
            )
            Spacer(Modifier.height(10.dp))
            OrnateButton(
                "Restart", { vm.retry() },
                Modifier.fillMaxWidth().height(56.dp), Palette.Amber, fontSize = 18
            )
            Spacer(Modifier.height(10.dp))
            OrnateButton(
                "Back to menu", { vm.quitRun() },
                Modifier.fillMaxWidth().height(56.dp), Palette.Rust, fontSize = 18
            )
        }
    }
}

private fun gaugeColor(steady: Float): Color = when {
    steady < 0.45f -> Palette.Sage
    steady < 0.75f -> Palette.Amber
    else -> Palette.Danger
}

private fun clock(seconds: Float): String {
    val s = seconds.coerceAtLeast(0f).toInt()
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}

private fun hint(hud: HudFrame): String = when {
    !hud.running -> "Settling..."
    hud.slide > 0.02f -> "Ease it out — watch the gauge"
    hud.selected -> "Hold Draw to work the block loose"
    else -> "Drag to walk around. Tap a block to choose it."
}

private fun goalProgress(vm: TeeterViewModel, hud: HudFrame): String {
    val engine = vm.engine ?: return ""
    if (vm.endless) return "pulled ${hud.pulled}  ·  best ${vm.stats.bestEndless}"
    val spec = engine.spec
    return when (spec.goal) {
        Goal.PULL -> "pull ${hud.pulled} / ${spec.target}"
        Goal.RUSH -> "rush ${hud.pulled} / ${spec.target}"
        Goal.STEADY -> "steady pulls ${hud.pulled} / ${spec.target}"
        Goal.HEIGHT -> "courses ${hud.courses} / ${spec.target}"
    }
}
