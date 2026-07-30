package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.dhanuk.quickscanpro.ui.theme.CameraFrame
import com.dhanuk.quickscanpro.ui.theme.CameraOverlayScrim

@Composable
fun ScanFrame(modifier: Modifier = Modifier, aspect: Float = 1f) {
    Box(
        modifier = modifier
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(20.dp))
            .background(CameraOverlayScrim),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 6f
            val corner = 56f
            val w = size.width
            val h = size.height
            fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(
                    color = CameraFrame,
                    start = Offset(x, y),
                    end = Offset(x + dx * corner, y),
                    strokeWidth = stroke
                )
                drawLine(
                    color = CameraFrame,
                    start = Offset(x, y),
                    end = Offset(x, y + dy * corner),
                    strokeWidth = stroke
                )
            }
            corner(0f, 0f, +1f, +1f)
            corner(w, 0f, -1f, +1f)
            corner(0f, h, +1f, -1f)
            corner(w, h, -1f, -1f)
        }
    }
}
