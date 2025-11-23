package com.quickscanpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quickscanpro.ui.composables.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateToAboutUs: () -> Unit,
    onNavigateToContactUs: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToTerms: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "About") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            GradientButton(onClick = onNavigateToAboutUs, text = "About Us")
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(onClick = onNavigateToContactUs, text = "Contact Us")
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(onClick = onNavigateToPrivacyPolicy, text = "Privacy Policy")
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(onClick = onNavigateToPermissions, text = "Permissions Usage")
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(onClick = onNavigateToTerms, text = "Terms & Conditions")
        }
    }
}
