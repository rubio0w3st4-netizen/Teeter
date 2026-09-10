package com.varnok.eslin.teeter.presentation.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varnok.eslin.teeter.presentation.Route
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import com.varnok.eslin.teeter.presentation.ui.Emblem
import com.varnok.eslin.teeter.presentation.ui.GameFonts
import com.varnok.eslin.teeter.presentation.ui.OrnateButton
import com.varnok.eslin.teeter.presentation.ui.Palette
import com.varnok.eslin.teeter.presentation.ui.backdropKeys
import com.varnok.eslin.teeter.presentation.ui.drawableId

@Composable
fun MenuScreen(vm: TeeterViewModel) {
    val activity = LocalContext.current as? Activity
    BackHandler { activity?.finish() }

    val heroId = drawableId(backdropKeys[vm.backdrop.coerceIn(0, backdropKeys.lastIndex)])
    val logoId = drawableId("logo")

    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(0.40f)) {
                if (heroId != 0) {
                    Image(
                        painter = painterResource(heroId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                Palette.Ink.copy(alpha = 0.55f),
                                Palette.Ink.copy(alpha = 0.18f),
                                Palette.Ink
                            )
                        )
                    )
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (logoId != 0) {
                        Image(
                            painter = painterResource(logoId),
                            contentDescription = null,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "TEETER",
                            color = Palette.Cream,
                            fontFamily = GameFonts.display,
                            fontSize = 40.sp
                        )
                        Text(
                            "Pull it out. Put it on top. Don't breathe.",
                            color = Palette.Brass,
                            fontFamily = GameFonts.hud,
                            fontSize = 14.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Emblem("ic_star", 26)
                        Text(
                            "${vm.totalStars}",
                            color = Palette.Amber,
                            fontFamily = GameFonts.hud,
                            fontSize = 17.sp
                        )
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(0.60f)
                    .padding(start = 20.dp, end = 20.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                RailButton("Play", "ic_play", 1.00f, Palette.Sage, 23, 66) { vm.continueRun() }
                RailButton("Long Night", "ic_course", 0.86f, Palette.Amber, 19, 56) { vm.startEndless() }
                RailButton("Halls", "ic_map", 0.78f, Palette.Slate, 18, 54) { vm.push(Route.Levels) }
                RailButton("Awards", "ic_medal", 0.70f, Palette.Slate, 17, 52) { vm.push(Route.Awards) }
                RailButton("Records", "ic_chart", 0.63f, Palette.Slate, 17, 50) { vm.push(Route.Stats) }
                RailButton("Setup", "ic_gear", 0.56f, Palette.Slate, 16, 48) { vm.push(Route.Settings) }
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Best endless run  ${vm.stats.bestEndless}",
                        color = Palette.Muted,
                        fontFamily = GameFonts.hud,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Leave",
                        color = Palette.Muted,
                        fontFamily = GameFonts.hud,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activity?.finish() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RailButton(
    label: String,
    emblem: String,
    widthFraction: Float,
    fill: androidx.compose.ui.graphics.Color,
    fontSize: Int,
    heightDp: Int,
    onClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth()) {
        OrnateButton(
            text = label,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(heightDp.dp)
                .align(Alignment.CenterStart),
            fillColor = fill,
            fontSize = fontSize,
            emblem = emblem
        )
    }
}
