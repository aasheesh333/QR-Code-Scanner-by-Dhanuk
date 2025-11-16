package com.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quickscanpro.ui.composables.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateToPrivacyPolicy: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "About App") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "QuickScan Pro", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Version 1.0")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "A simple and fast QR & Barcode scanner.")
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(onClick = onNavigateToPrivacyPolicy, text = "Privacy Policy")
        }
    }
}
