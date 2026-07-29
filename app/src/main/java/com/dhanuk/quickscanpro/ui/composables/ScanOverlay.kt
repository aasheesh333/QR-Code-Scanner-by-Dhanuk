package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimary
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimaryGlow

/**
 * Lumina Glass scan viewfinder overlay:
 * four glowing pulsing corner brackets + a scanning beam
 * that sweeps vertically with a soft trailing glow.
 */
@Composable
fun ScanOverlay(
    modifier: Modifier = Modifier,
    color: Color = LuminaPrimaryGlow,
    strokeWidth: Float = 7f,
    cornerLengthFraction: Float = 0.14f
) {
    val transition = rememberInfiniteTransition(label = "scan")

    // Vertical beam sweep
    val beamProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beam"
    )

    // Bracket glow pulse
    val glowAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val corner = w * cornerLengthFraction

        // Glowing corner brackets
        val bracketColor = color.copy(alpha = glowAlpha)
        val bracketStroke = strokeWidth

        // Top-left
        drawLine(bracketColor, Offset(0f, corner), Offset(0f, 0f), bracketStroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(0f, 0f), Offset(corner, 0f), bracketStroke, StrokeCap.Round)
        // Top-right
        drawLine(bracketColor, Offset(w - corner, 0f), Offset(w, 0f), bracketStroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(w, 0f), Offset(w, corner), bracketStroke, StrokeCap.Round)
        // Bottom-right
        drawLine(bracketColor, Offset(w, h - corner), Offset(w, h), bracketStroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(w, h), Offset(w - corner, h), bracketStroke, StrokeCap.Round)
        // Bottom-left
        drawLine(bracketColor, Offset(corner, h), Offset(0f, h), bracketStroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(0f, h), Offset(0f, h - corner), bracketStroke, StrokeCap.Round)

        // Scanning beam with gradient trail
        val beamY = beamProgress * h
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = 0.55f),
                    color,
                    color.copy(alpha = 0.55f),
                    Color.Transparent
                ),
                startY = (beamY - 90f).coerceAtLeast(0f),
                endY = (beamY + 10f).coerceAtMost(h)
            ),
            topLeft = Offset(0f, (beamY - 90f).coerceAtLeast(0f)),
            size = androidx.compose.ui.geometry.Size(w, ((beamY + 10f).coerceAtMost(h)) - (beamY - 90f).coerceAtLeast(0f))
        )
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(0f, beamY),
            end = Offset(w, beamY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}
