package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.theme.*

/**
 * Frosted-glass card used across the Lumina Glass design.
 * Semi-transparent fill + 10% white (dark) / purple (light) border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    glowColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val fill = if (dark) GlassFillDark else GlassFillLight
    val borderColor = if (dark) GlassBorderDark else GlassBorderLight

    val baseModifier = modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(fill)
        .border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(cornerRadius)
        )

    val finalModifier = if (glowColor != null) {
        baseModifier.border(
            width = 1.dp,
            color = glowColor.copy(alpha = 0.35f),
            shape = RoundedCornerShape(cornerRadius)
        )
    } else baseModifier

    androidx.compose.foundation.layout.Column(modifier = finalModifier) {
        content()
    }
}

/**
 * Slow-shifting diagonal gradient backdrop approximating the
 * "liquid shader" atmosphere from the Stitch design.
 */
@Composable
fun LiquidBackground(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "liquid")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    val colors = if (dark)
        listOf(LiquidDarkA, LiquidDarkB, LiquidDarkC, LiquidDarkA)
    else
        listOf(LiquidLightA, LiquidLightB, LiquidLightC, LiquidLightA)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, shift * 1200f),
                    end = Offset(1200f, shift * 1200f + 1200f)
                )
            )
    )
}

/** Radial purple glow accent used behind hero elements. */
@Composable
fun GlowOrb(modifier: Modifier = Modifier, color: Color = LuminaPrimary) {
    Box(
        modifier = modifier.background(
            Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.35f), Color.Transparent)
            )
        )
    )
}
