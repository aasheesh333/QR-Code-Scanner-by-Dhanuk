package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.dhanuk.quickscanpro.ui.theme.CameraFrame

@Composable
fun ScanFrame(modifier: Modifier = Modifier, aspectRatio: Float = 1f) {
    val density = LocalDensity.current
    val strokePx = with(density) { 4.dp.toPx() }
    val cornerLen = with(density) { 48.dp.toPx() }
    val halfStroke = strokePx / 2f

    val transition = rememberInfiniteTransition(label = "scanLine")
    val scanLineProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineProgress"
    )

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(20.dp))
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            fun drawCorner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(
                    color = CameraFrame,
                    start = Offset(x, y),
                    end = Offset(x + dx * cornerLen, y),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = CameraFrame,
                    start = Offset(x, y),
                    end = Offset(x, y + dy * cornerLen),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round
                )
            }

            drawCorner(halfStroke, halfStroke, +1f, +1f)
            drawCorner(w - halfStroke, halfStroke, -1f, +1f)
            drawCorner(halfStroke, h - halfStroke, +1f, -1f)
            drawCorner(w - halfStroke, h - halfStroke, -1f, -1f)

            val scanY = scanLineProgress * h
            drawLine(
                color = CameraFrame.copy(alpha = 0.6f),
                start = Offset(strokePx, scanY),
                end = Offset(w - strokePx, scanY),
                strokeWidth = with(density) { 2.dp.toPx() }
            )
        }
    }
}
