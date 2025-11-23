package com.quickscanpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Button(onClick = onNavigateToAboutUs, modifier = Modifier.fillMaxWidth()) {
                Text(text = "About Us")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToContactUs, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Contact Us")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToPrivacyPolicy, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Privacy Policy")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToPermissions, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Permissions Usage")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToTerms, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Terms & Conditions")
            }
        }
    }
}
