package com.varnok.eslin.teeter.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varnok.eslin.teeter.data.catalog.LevelCatalog
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import com.varnok.eslin.teeter.presentation.ui.GameFonts
import com.varnok.eslin.teeter.presentation.ui.HeaderBar
import com.varnok.eslin.teeter.presentation.ui.Palette

@Composable
fun StatsScreen(vm: TeeterViewModel) {
    BackHandler { vm.pop() }
    val s = vm.stats
    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            HeaderBar("Records", { vm.pop() })
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
            ) {
                StatRow("Runs started", "${s.runs}")
                StatRow("Blocks pulled", "${s.pulled}")
                StatRow("Towers dropped", "${s.collapses}")
                StatRow("Levels cleared", "${s.cleared} / ${LevelCatalog.levels.size}")
                StatRow("Stars banked", "${s.stars} / ${LevelCatalog.levels.size * 3}")
                StatRow("Tallest tower", "${s.tallest} courses")
                StatRow("Best endless run", "${s.bestEndless} blocks")
                StatRow(
                    "Standing rate",
                    if (s.runs == 0) "—" else "${(100 * (s.runs - s.collapses) / s.runs)}%"
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.Bark.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Palette.Cream.copy(alpha = 0.85f), fontFamily = GameFonts.hud, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(value, color = Palette.Amber, fontFamily = GameFonts.display, fontSize = 19.sp)
    }
}
