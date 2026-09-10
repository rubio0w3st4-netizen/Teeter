package com.varnok.eslin.teeter.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.varnok.eslin.teeter.R

object Palette {
    val Ink = Color(0xFF14100A)
    val Bark = Color(0xFF241A11)
    val Cream = Color(0xFFF2E3C8)
    val Amber = Color(0xFFD9A441)
    val Brass = Color(0xFFB98A3C)
    val Sage = Color(0xFF6E8B4E)
    val Slate = Color(0xFF4B6076)
    val Rust = Color(0xFFB2492E)
    val Muted = Color(0xFF9C8A72)
    val Danger = Color(0xFFCB5A38)
}

object GameFonts {
    val display = FontFamily(Font(R.font.zilla_slab_bold, FontWeight.Bold))
    val hud = FontFamily(Font(R.font.barlow_semicondensed_semibold, FontWeight.SemiBold))
}

val backdropKeys = listOf("bg_workbench", "bg_attic", "bg_hall", "bg_foundry")
val backdropNames = listOf("Workbench", "Attic Loft", "Draught Hall", "Foundry Floor")

@Composable
fun drawableId(name: String): Int {
    val ctx = LocalContext.current
    return remember(name) { ctx.resources.getIdentifier(name, "drawable", ctx.packageName) }
}

@Composable
fun Backdrop(key: String, dim: Float = 0.62f, content: @Composable () -> Unit) {
    val id = drawableId(key)
    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        if (id != 0) {
            Image(
                painter = painterResource(id),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Palette.Ink.copy(alpha = dim * 0.9f),
                        Palette.Ink.copy(alpha = dim * 0.55f),
                        Palette.Ink.copy(alpha = dim * 1.05f)
                    )
                )
            )
        )
        content()
    }
}

private fun plateFor(fill: Color): String = when (fill) {
    Palette.Slate -> "btn_plate_blue"
    Palette.Amber -> "btn_plate_gold"
    Palette.Danger, Palette.Rust -> "btn_plate_red"
    else -> "btn_plate_green"
}

@Composable
fun OrnateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillColor: Color = Palette.Sage,
    textColor: Color? = null,
    enabled: Boolean = true,
    fontSize: Int = 20,
    emblem: String? = null
) {
    val plateId = drawableId(plateFor(fillColor))
    val alpha = if (enabled) 1f else 0.45f
    val label = (textColor ?: if (fillColor == Palette.Amber) Palette.Ink else Palette.Cream)
        .copy(alpha = alpha)
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (plateId != 0) {
            Image(
                painter = painterResource(plateId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
                alpha = alpha
            )
        } else {
            Box(Modifier.matchParentSize().background(fillColor.copy(alpha = alpha)))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            if (emblem != null) {
                val e = drawableId(emblem)
                if (e != 0) {
                    Image(
                        painter = painterResource(e),
                        contentDescription = null,
                        modifier = Modifier.size((fontSize * 1.35f).dp).alpha(alpha)
                    )
                    Spacer(Modifier.width(10.dp))
                }
            }
            Text(
                text = text,
                color = label,
                fontFamily = GameFonts.display,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StarRow(stars: Int, max: Int = 3, size: Int = 18, modifier: Modifier = Modifier) {
    val id = drawableId("ic_star")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(max) { i ->
            if (id != 0) {
                Image(
                    painter = painterResource(id),
                    contentDescription = null,
                    modifier = Modifier.size(size.dp).alpha(if (i < stars) 1f else 0.22f)
                )
            }
        }
    }
}

@Composable
fun Emblem(name: String, size: Int, modifier: Modifier = Modifier, alpha: Float = 1f) {
    val id = drawableId(name)
    if (id != 0) {
        Image(
            painter = painterResource(id),
            contentDescription = null,
            modifier = modifier.size(size.dp).alpha(alpha)
        )
    }
}

@Composable
fun HeaderBar(title: String, onBack: () -> Unit, trailing: String? = null) {
    Row(
        Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OrnateButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.height(46.dp).width(104.dp),
            fillColor = Palette.Slate,
            fontSize = 15
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            color = Palette.Cream,
            fontFamily = GameFonts.display,
            fontSize = 24.sp,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                color = Palette.Amber,
                fontFamily = GameFonts.hud,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Bark.copy(alpha = 0.86f))
            .padding(14.dp)
    ) { content() }
}

@Composable
fun RoundPlate(size: Int, modifier: Modifier = Modifier, alpha: Float = 1f, content: @Composable () -> Unit) {
    val id = drawableId("btn_node")
    Box(modifier.size(size.dp), contentAlignment = Alignment.Center) {
        if (id != 0) {
            Image(
                painter = painterResource(id),
                contentDescription = null,
                modifier = Modifier.matchParentSize().clip(CircleShape).alpha(alpha),
                contentScale = ContentScale.Crop
            )
        }
        content()
    }
}
