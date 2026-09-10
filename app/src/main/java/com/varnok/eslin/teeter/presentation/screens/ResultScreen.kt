package com.varnok.eslin.teeter.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varnok.eslin.teeter.domain.tower.RunFault
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import com.varnok.eslin.teeter.presentation.ui.Backdrop
import com.varnok.eslin.teeter.presentation.ui.Emblem
import com.varnok.eslin.teeter.presentation.ui.GameFonts
import com.varnok.eslin.teeter.presentation.ui.OrnateButton
import com.varnok.eslin.teeter.presentation.ui.Palette
import com.varnok.eslin.teeter.presentation.ui.StarRow
import com.varnok.eslin.teeter.presentation.ui.backdropKeys

@Composable
fun ResultScreen(vm: TeeterViewModel) {
    BackHandler { vm.goMenu() }
    val outcome = vm.outcome ?: return
    val engine = vm.engine
    val chapter = engine?.spec?.chapter?.coerceIn(0, 3) ?: 0
    val headline = when {
        outcome.cleared -> "Course held"
        engine?.fault == RunFault.TIMEOUT -> "Out of time"
        engine?.fault == RunFault.SHAKEN -> "Shaken loose"
        else -> "Down it went"
    }
    val note = when {
        vm.endless -> "The night ends when the tower does."
        outcome.cleared -> "The stack is still standing and the brief is met."
        engine?.fault == RunFault.TIMEOUT -> "The tower survived, the clock did not."
        engine?.fault == RunFault.SHAKEN -> "The gauge left the green and the course was voided."
        else -> "Every course above the gap came with it."
    }

    Backdrop(backdropKeys[if (vm.endless) 3 else chapter], dim = 0.82f) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                headline,
                color = if (outcome.cleared) Palette.Amber else Palette.Danger,
                fontFamily = GameFonts.display,
                fontSize = 38.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                note,
                color = Palette.Cream.copy(alpha = 0.8f),
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            if (!vm.endless) {
                StarRow(outcome.stars, size = 44)
                Spacer(Modifier.height(20.dp))
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Tally("Pulled", "${outcome.pulled}")
                Tally("Courses", "${outcome.courses}")
                if (vm.endless) Tally("Best", "${vm.stats.bestEndless}")
                else Tally("Stars", "${vm.totalStars}")
            }
            if (outcome.newAwards.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Palette.Bark.copy(alpha = 0.88f))
                        .padding(14.dp)
                ) {
                    Text(
                        "New awards",
                        color = Palette.Brass,
                        fontFamily = GameFonts.hud,
                        fontSize = 13.sp
                    )
                    outcome.newAwards.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Emblem("ic_medal", 24)
                            Spacer(Modifier.height(0.dp))
                            Text(
                                "  ${it.title}",
                                color = Palette.Cream,
                                fontFamily = GameFonts.display,
                                fontSize = 17.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            if (outcome.cleared && !vm.endless) {
                OrnateButton(
                    "Next course", { vm.advance() },
                    Modifier.fillMaxWidth().height(62.dp), Palette.Sage, fontSize = 21
                )
                Spacer(Modifier.height(10.dp))
            }
            OrnateButton(
                if (vm.endless) "Another night" else "Try again", { vm.retry() },
                Modifier.fillMaxWidth().height(56.dp), Palette.Amber, fontSize = 18
            )
            Spacer(Modifier.height(10.dp))
            OrnateButton(
                "Back to menu", { vm.goMenu() },
                Modifier.fillMaxWidth().height(56.dp), Palette.Slate, fontSize = 18
            )
        }
    }
}

@Composable
private fun Tally(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Palette.Cream, fontFamily = GameFonts.display, fontSize = 28.sp)
        Text(label, color = Palette.Muted, fontFamily = GameFonts.hud, fontSize = 12.sp)
    }
}
