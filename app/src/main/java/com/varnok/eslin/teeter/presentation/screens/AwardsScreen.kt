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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import com.varnok.eslin.teeter.presentation.ui.Emblem
import com.varnok.eslin.teeter.presentation.ui.GameFonts
import com.varnok.eslin.teeter.presentation.ui.HeaderBar
import com.varnok.eslin.teeter.presentation.ui.Palette

@Composable
fun AwardsScreen(vm: TeeterViewModel) {
    BackHandler { vm.pop() }
    val cards = vm.awards
    val unlocked = cards.count { it.unlocked }
    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            HeaderBar("Awards", { vm.pop() }, "$unlocked / ${cards.size}")
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 18.dp)
            ) {
                items(cards) { card ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (card.unlocked) Palette.Bark.copy(alpha = 0.92f)
                                else Palette.Bark.copy(alpha = 0.5f)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Emblem(
                            if (card.unlocked) "ic_medal" else "ic_lock",
                            38,
                            alpha = if (card.unlocked) 1f else 0.45f
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                card.award.title,
                                color = if (card.unlocked) Palette.Amber else Palette.Muted,
                                fontFamily = GameFonts.display,
                                fontSize = 19.sp
                            )
                            Text(
                                card.award.detail,
                                color = Palette.Cream.copy(alpha = if (card.unlocked) 0.82f else 0.42f),
                                fontFamily = GameFonts.hud,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
