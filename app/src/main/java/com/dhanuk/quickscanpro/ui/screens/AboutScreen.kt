package com.dhanuk.quickscanpro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val BASE_URL = "https://dhanuk.page.gd/QuickScan-Pro"

/**
 * About screen — clean professional list of links.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current

    fun openUrl(page: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$BASE_URL/$page")))
        } catch (_: Exception) {}
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text("About", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            AboutItem(
                icon = Icons.Filled.Info,
                text = "About Us",
                onClick = { openUrl("about-us.html") }
            )
            AboutItem(
                icon = Icons.Filled.Email,
                text = "Contact Us",
                onClick = { openUrl("contact-us.html") }
            )
            AboutItem(
                icon = Icons.Filled.PrivacyTip,
                text = "Privacy Policy",
                onClick = { openUrl("privacy-policy.html") }
            )
            AboutItem(
                icon = Icons.Filled.Lock,
                text = "Permissions Usage",
                onClick = { openUrl("permissions.html") }
            )
            AboutItem(
                icon = Icons.Filled.Description,
                text = "Terms & Conditions",
                onClick = { openUrl("terms.html") }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun AboutItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    com.dhanuk.quickscanpro.ui.composables.GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
