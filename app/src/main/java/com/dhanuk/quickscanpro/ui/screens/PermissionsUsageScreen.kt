package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsUsageScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Permissions Usage") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Permissions Used – Quick Scan Pro (Dhanuk)\n\nQuick Scan Pro requires the following permissions:\n\n1. Camera Permission\nUsed only for scanning documents using your device camera.\nWe do NOT upload any images automatically.\n\n2. Storage / Gallery Permission\nUsed for:\n• Importing images or documents from your gallery\n• Saving scanned files\nWe DO NOT access your files automatically. Only files selected by the user are processed.\n\n3. Internet Permission\nUsed for:\n• Showing ads (AdMob)\n• App updates and analytics\n\nWe never collect or store personal files, images, or private data.\nAll files remain on the user’s device.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
