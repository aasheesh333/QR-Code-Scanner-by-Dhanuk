package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Big white scan button floating above the camera preview.
 * Matches Gamma Scan / QRbot pattern.
 */
@Composable
fun ScanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Scan"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        val btnColor = Color.White
        androidx.compose.material3.Button(
            onClick = onClick,
            shape = CircleShape,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = btnColor,
                contentColor = Color.Black
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 36.dp, vertical = 16.dp
            )
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Small dark circular icon FAB over camera (flash, gallery).
 */
@Composable
fun CameraIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = Color.Black.copy(alpha = 0.55f),
        contentColor = Color.White
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
