package com.quickscanpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            CenterAlignedTopAppBar(
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
            TextButton(
                onClick = onNavigateToAboutUs,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "About Us")
                }
            }
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            TextButton(
                onClick = onNavigateToContactUs,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Contact Us")
                }
            }
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            TextButton(
                onClick = onNavigateToPrivacyPolicy,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Privacy Policy")
                }
            }
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            TextButton(
                onClick = onNavigateToPermissions,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Permissions Usage")
                }
            }
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            TextButton(
                onClick = onNavigateToTerms,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Terms & Conditions")
                }
            }
        }
    }
}
