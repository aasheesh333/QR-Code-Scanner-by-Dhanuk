package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.theme.GlassBorderDark
import com.dhanuk.quickscanpro.ui.theme.GlassBorderLight
import com.dhanuk.quickscanpro.ui.theme.GlassFillDark
import com.dhanuk.quickscanpro.ui.theme.GlassFillLight

/**
 * Clean surface card used across the app. Renders as a white card with a
 * subtle gray border in light mode, and a dark-neutral card in dark mode.
 * The "glowColor" parameter is kept for API stability but now only applies
 * a thin tint stroke — no glow effect.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    glowColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val fill = if (dark) GlassFillDark else GlassFillLight
    val borderColor = when {
        glowColor != null -> glowColor.copy(alpha = 0.25f)
        dark -> GlassBorderDark
        else -> GlassBorderLight
    }

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(fill)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

/**
 * Flat background. The previous "liquid" gradient is now a solid theme
 * background so the UI looks clean and professional.
 */
@Composable
fun LiquidBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
}

/** Glow placeholder kept for API stability; renders nothing. */
@Composable
fun GlowOrb(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Box(modifier = modifier)
}
