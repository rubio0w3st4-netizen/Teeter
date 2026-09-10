package com.varnok.eslin.teeter.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import com.varnok.eslin.teeter.presentation.ui.GameFonts
import com.varnok.eslin.teeter.presentation.ui.HeaderBar
import com.varnok.eslin.teeter.presentation.ui.OrnateButton
import com.varnok.eslin.teeter.presentation.ui.Palette
import com.varnok.eslin.teeter.presentation.ui.backdropKeys
import com.varnok.eslin.teeter.presentation.ui.backdropNames
import com.varnok.eslin.teeter.presentation.ui.drawableId

@Composable
fun SettingsScreen(vm: TeeterViewModel) {
    BackHandler { vm.pop() }
    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            HeaderBar("Setup", { vm.pop() })
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
            ) {
                Text(
                    "Workshop",
                    color = Palette.Brass,
                    fontFamily = GameFonts.hud,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(backdropKeys) { index, key ->
                        val id = drawableId(key)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .size(96.dp, 132.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        if (vm.backdrop == index) 3.dp else 1.dp,
                                        if (vm.backdrop == index) Palette.Amber else Palette.Muted.copy(alpha = 0.35f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { vm.chooseBackdrop(index) }
                            ) {
                                if (id != 0) {
                                    Image(
                                        painter = painterResource(id),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                backdropNames[index],
                                color = if (vm.backdrop == index) Palette.Amber else Palette.Muted,
                                fontFamily = GameFonts.hud,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                ToggleRow("Sound", "Knocks, slides and the drop.", vm.soundOn) { vm.setSound(it) }
                ToggleRow("Vibration", "A tick on every course you move.", vm.vibrationOn) { vm.setVibration(it) }
                ToggleRow("Block markers", "Ring the courses you are allowed to draw from.", vm.guidesOn) { vm.setGuides(it) }
                Spacer(Modifier.height(20.dp))
                OrnateButton(
                    "Wipe all progress",
                    { vm.wipeProgress() },
                    Modifier.fillMaxWidth().height(54.dp),
                    Palette.Rust,
                    fontSize = 17
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Teeter plays offline. Nothing leaves this device.",
                    color = Palette.Muted,
                    fontFamily = GameFonts.hud,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(26.dp))
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, detail: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.Bark.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Palette.Cream, fontFamily = GameFonts.display, fontSize = 18.sp)
            Text(detail, color = Palette.Muted, fontFamily = GameFonts.hud, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Switch(
            checked = value,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Palette.Cream,
                checkedTrackColor = Palette.Sage,
                uncheckedThumbColor = Palette.Muted,
                uncheckedTrackColor = Palette.Bark
            )
        )
    }
}
