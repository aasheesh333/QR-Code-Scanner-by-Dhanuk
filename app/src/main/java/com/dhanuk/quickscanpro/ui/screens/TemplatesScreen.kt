package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import com.dhanuk.quickscanpro.util.QRTemplateRegistry

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onUseTemplate: (QRTemplateRegistry.Template) -> Unit
) {
    val templates = QRTemplateRegistry.templates

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QR Templates", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(0.dp))
            SectionLabel("One-tap starters")
            templates.forEach { template ->
                QsCard(onClick = { onUseTemplate(template) }, contentPadding = 16.dp) {
                    Row {
                        IconBadge(template.icon)
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Column(Modifier.weight(1f)) {
                            Text(template.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
