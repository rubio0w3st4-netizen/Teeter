package com.varnok.eslin.teeter.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import com.varnok.eslin.teeter.presentation.ui.Backdrop
import com.varnok.eslin.teeter.presentation.ui.GameFonts
import com.varnok.eslin.teeter.presentation.ui.OrnateButton
import com.varnok.eslin.teeter.presentation.ui.Palette
import com.varnok.eslin.teeter.presentation.ui.drawableId

private data class Lesson(val art: String, val head: String, val body: String)

private val lessons = listOf(
    Lesson(
        "tut_1",
        "One tower, one table",
        "Every run starts with a stack of timber courses, three blocks to a course, each course laid across the one below it. Nothing holds it together but weight and friction."
    ),
    Lesson(
        "tut_2",
        "Choose, then draw",
        "Drag anywhere to walk around the tower. Tap a block to choose it, then press and hold DRAW to ease it out. The block resists in proportion to the weight it carries, and every millimetre of travel shoves the stack."
    ),
    Lesson(
        "tut_3",
        "What comes out goes on top",
        "A freed block is carried straight to the crown of the tower. The stack never gets lighter, only taller and thinner, and the higher the load rides the less lean it forgives."
    ),
    Lesson(
        "tut_4",
        "Read the gauge",
        "The steady gauge shows how far the tower is off plumb. Let go of DRAW and it settles. Push it into the red and the whole stack goes over, so the skill is knowing when to stop pulling and wait."
    ),
    Lesson(
        "tut_5",
        "Not every block is pine",
        "Lacquered blocks slip out almost free. Cracked ones jolt the stack twice as hard. Granite weighs more than double and punishes everything above it. Learn the timber before you touch it."
    )
)

@Composable
fun TutorialScreen(vm: TeeterViewModel) {
    BackHandler { vm.finishTutorial() }
    Backdrop("bg_workbench", dim = 0.78f) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "How Teeter works",
                    color = Palette.Cream,
                    fontFamily = GameFonts.display,
                    fontSize = 23.sp
                )
                OrnateButton(
                    text = "Skip",
                    onClick = { vm.finishTutorial() },
                    modifier = Modifier.height(44.dp).width(92.dp),
                    fillColor = Palette.Slate,
                    fontSize = 14
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                lessons.forEachIndexed { i, lesson ->
                    LessonCard(i + 1, lesson)
                }
                Spacer(Modifier.height(10.dp))
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Palette.Ink.copy(alpha = 0f), Palette.Ink.copy(alpha = 0.92f))
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                OrnateButton(
                    text = "Begin!",
                    onClick = { vm.finishTutorial() },
                    modifier = Modifier.fillMaxWidth().height(62.dp),
                    fillColor = Palette.Sage,
                    fontSize = 22
                )
            }
        }
    }
}

@Composable
private fun LessonCard(number: Int, lesson: Lesson) {
    val art = drawableId(lesson.art)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Bark.copy(alpha = 0.9f))
    ) {
        if (art != 0) {
            Image(
                painter = painterResource(art),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.62f),
                contentScale = ContentScale.Crop
            )
        }
        Column(Modifier.padding(16.dp)) {
            Text(
                "Step $number",
                color = Palette.Brass,
                fontFamily = GameFonts.hud,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                lesson.head,
                color = Palette.Amber,
                fontFamily = GameFonts.display,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(7.dp))
            Text(
                lesson.body,
                color = Palette.Cream.copy(alpha = 0.88f),
                fontFamily = GameFonts.hud,
                fontSize = 16.sp,
                lineHeight = 21.sp
            )
        }
    }
}
