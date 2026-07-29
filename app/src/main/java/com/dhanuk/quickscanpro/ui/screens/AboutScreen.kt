package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AboutItem(
                icon = Icons.Filled.Info,
                text = "About Us",
                onClick = onNavigateToAboutUs
            )
            HorizontalDivider()
            AboutItem(
                icon = Icons.Filled.Email,
                text = "Contact Us",
                onClick = onNavigateToContactUs
            )
            HorizontalDivider()
            AboutItem(
                icon = Icons.Filled.PrivacyTip,
                text = "Privacy Policy",
                onClick = onNavigateToPrivacyPolicy
            )
            HorizontalDivider()
            AboutItem(
                icon = Icons.Filled.Lock,
                text = "Permissions Usage",
                onClick = onNavigateToPermissions
            )
            HorizontalDivider()
            AboutItem(
                icon = Icons.Filled.Description,
                text = "Terms & Conditions",
                onClick = onNavigateToTerms
            )
        }
    }
}

@Composable
fun AboutItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
        )
    }
}
