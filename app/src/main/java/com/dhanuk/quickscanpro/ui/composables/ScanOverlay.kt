package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimary

/**
 * Clean, minimal scan viewfinder: simple corner brackets in the brand color
 * with a thin sweeping line. No glows, no gradients.
 */
@Composable
fun ScanOverlay(
    modifier: Modifier = Modifier,
    color: Color = LuminaPrimary,
    strokeWidth: Float = 4f,
    cornerLengthFraction: Float = 0.12f
) {
    val transition = rememberInfiniteTransition(label = "scan")
    val beamProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beam"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val corner = w * cornerLengthFraction

        // Corner brackets
        drawLine(color, Offset(0f, corner), Offset(0f, 0f), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(0f, 0f), Offset(corner, 0f), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(w - corner, 0f), Offset(w, 0f), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(w, 0f), Offset(w, corner), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(w, h - corner), Offset(w, h), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(w, h), Offset(w - corner, h), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(corner, h), Offset(0f, h), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(0f, h), Offset(0f, h - corner), strokeWidth, StrokeCap.Round)

        // Thin sweep line
        val beamY = beamProgress * h
        drawLine(
            color = color.copy(alpha = 0.8f),
            start = Offset(0f, beamY),
            end = Offset(w, beamY),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

/** Spacer for the scanner tinted mask. Kept for API compatibility. */
@Composable
fun ScannerMask(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(modifier = modifier)
}
