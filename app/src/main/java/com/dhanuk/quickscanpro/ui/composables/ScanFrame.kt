package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.theme.CameraFrame
import com.dhanuk.quickscanpro.ui.theme.CameraOverlayScrim

/**
 * Scan frame on camera preview — matches top QR apps:
 * dim 80% black scrim + white corner brackets. Square by default.
 */
@Composable
fun ScanFrame(modifier: Modifier = Modifier, aspect: Float = 1f) {
    Box(
        modifier = modifier
            .aspectRatio(aspect)
            .background(CameraOverlayScrim)
            .clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Clear hole in the middle (transparent) so the QR area is visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Transparent)
        )
        Canvas(modifier = Modifier.fillMaxSize().padding(28.dp)) {
            val stroke = 6f
            val corner = 56f
            val w = size.width
            val h = size.height
            fun cornerPath(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(CameraFrame, x, y, x + dx * corner, y, stroke)
                drawLine(CameraFrame, x, y, x, y + dy * corner, stroke)
            }
            cornerPath(0f, 0f, +1f, +1f)
            cornerPath(w, 0f, -1f, +1f)
            cornerPath(0f, h, +1f, -1f)
            cornerPath(w, h, -1f, -1f)
        }
    }
}
