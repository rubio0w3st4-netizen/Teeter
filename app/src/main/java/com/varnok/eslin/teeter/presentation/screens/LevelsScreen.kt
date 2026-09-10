package com.varnok.eslin.teeter.presentation.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varnok.eslin.teeter.data.catalog.LevelCatalog
import com.varnok.eslin.teeter.domain.model.Goal
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import com.varnok.eslin.teeter.presentation.ui.Emblem
import com.varnok.eslin.teeter.presentation.ui.GameFonts
import com.varnok.eslin.teeter.presentation.ui.HeaderBar
import com.varnok.eslin.teeter.presentation.ui.Palette
import com.varnok.eslin.teeter.presentation.ui.RoundPlate
import com.varnok.eslin.teeter.presentation.ui.StarRow
import com.varnok.eslin.teeter.presentation.ui.drawableId

@Composable
fun LevelsScreen(vm: TeeterViewModel) {
    BackHandler { vm.pop() }
    val boards = vm.boards
    val pager = rememberPagerState(pageCount = { boards.size.coerceAtLeast(1) })

    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            HeaderBar("Halls", { vm.pop() }, "${vm.totalStars} stars")
            if (boards.isEmpty()) return@Column
            HorizontalPager(
                state = pager,
                modifier = Modifier.weight(1f)
            ) { page ->
                val board = boards[page]
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val art = drawableId(board.chapter.backdrop)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        if (art != 0) {
                            Image(
                                painter = painterResource(art),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    listOf(Palette.Ink.copy(alpha = 0.25f), Palette.Ink.copy(alpha = 0.92f))
                                )
                            )
                        )
                        Column(
                            Modifier.align(Alignment.BottomStart).padding(14.dp)
                        ) {
                            Text(
                                board.chapter.title,
                                color = Palette.Cream,
                                fontFamily = GameFonts.display,
                                fontSize = 26.sp
                            )
                            Text(
                                board.chapter.blurb,
                                color = Palette.Brass,
                                fontFamily = GameFonts.hud,
                                fontSize = 13.sp
                            )
                        }
                        Row(
                            Modifier.align(Alignment.TopEnd).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Emblem("ic_star", 18)
                            Spacer(Modifier.size(5.dp))
                            Text(
                                "${board.stars} / ${board.maxStars}",
                                color = Palette.Amber,
                                fontFamily = GameFonts.hud,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    for (row in 0 until 3) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0 until 3) {
                                val idx = row * 3 + col
                                if (idx < board.cards.size) {
                                    val card = board.cards[idx]
                                    LevelNode(
                                        number = card.spec.index + 1,
                                        title = card.spec.title,
                                        goal = goalLine(card.spec.goal, card.spec.target),
                                        stars = card.progress.stars,
                                        unlocked = card.progress.unlocked
                                    ) { if (card.progress.unlocked) vm.startLevel(card.spec.index) }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(boards.size) { i ->
                    Box(
                        Modifier
                            .padding(horizontal = 5.dp)
                            .size(if (i == pager.currentPage) 11.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == pager.currentPage) Palette.Amber else Palette.Muted.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

private fun goalLine(goal: Goal, target: Int): String = when (goal) {
    Goal.PULL -> "pull $target"
    Goal.RUSH -> "rush $target"
    Goal.STEADY -> "steady $target"
    Goal.HEIGHT -> "reach $target"
}

@Composable
private fun LevelNode(
    number: Int,
    title: String,
    goal: String,
    stars: Int,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = unlocked) { onClick() }
            .padding(4.dp)
    ) {
        RoundPlate(size = 74, alpha = if (unlocked) 1f else 0.4f) {
            if (unlocked) {
                Text(
                    "$number",
                    color = Palette.Ink,
                    fontFamily = GameFonts.display,
                    fontSize = 26.sp
                )
            } else {
                Emblem("ic_lock", 30)
            }
        }
        Spacer(Modifier.height(5.dp))
        StarRow(stars, size = 13)
        Spacer(Modifier.height(3.dp))
        Text(
            title,
            color = if (unlocked) Palette.Cream else Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(92.dp)
        )
        Text(
            goal,
            color = Palette.Brass.copy(alpha = if (unlocked) 0.9f else 0.4f),
            fontFamily = GameFonts.hud,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(92.dp).alpha(0.9f)
        )
    }
}
