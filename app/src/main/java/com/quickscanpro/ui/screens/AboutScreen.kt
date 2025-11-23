package com.quickscanpro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
                .verticalScroll(rememberScrollState())
        ) {
            AboutItem(text = "About Us", onClick = onNavigateToAboutUs)
            AboutItem(text = "Contact Us", onClick = onNavigateToContactUs)
            AboutItem(text = "Privacy Policy", onClick = onNavigateToPrivacyPolicy)
            AboutItem(text = "Permissions Usage", onClick = onNavigateToPermissions)
            AboutItem(text = "Terms & Conditions", onClick = onNavigateToTerms)
        }
    }
}

@Composable
fun AboutItem(text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
}
