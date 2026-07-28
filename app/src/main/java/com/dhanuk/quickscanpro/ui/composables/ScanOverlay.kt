package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ScanOverlay(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00F260),
    strokeWidth: Float = 6f,
    cornerLengthFraction: Float = 0.12f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val corner = w * cornerLengthFraction
        val strokeStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        // Top-left corner
        drawLine(color, Offset(0f, corner), Offset(0f, 0f), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(0f, 0f), Offset(corner, 0f), strokeWidth, StrokeCap.Round)

        // Top-right corner
        drawLine(color, Offset(w - corner, 0f), Offset(w, 0f), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(w, 0f), Offset(w, corner), strokeWidth, StrokeCap.Round)

        // Bottom-right corner
        drawLine(color, Offset(w, h - corner), Offset(w, h), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(w, h), Offset(w - corner, h), strokeWidth, StrokeCap.Round)

        // Bottom-left corner
        drawLine(color, Offset(corner, h), Offset(0f, h), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(0f, h), Offset(0f, h - corner), strokeWidth, StrokeCap.Round)

        // Vertical scan line
        val lineY = scanProgress * h
        drawLine(
            color = color.copy(alpha = 0.8f),
            start = Offset(0f, lineY),
            end = Offset(w, lineY),
            strokeWidth = 2f
        )
    }
}
