package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.SectionTitle
import com.dhanuk.quickscanpro.util.QRTemplateRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onUseTemplate: (QRTemplateRegistry.Template) -> Unit = {}
) {
    val all = QRTemplateRegistry.templates
    val recent = all.take(2)
    AppBackground()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Templates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            SectionTitle(text = "RECENT")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recent.forEach { RecentRow(it, onUseTemplate) }
            }
            Spacer(Modifier.height(16.dp))
            SectionTitle(text = "ALL TEMPLATES")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                all.forEach { AllTemplateRow(it, onUseTemplate) }
            }
            Spacer(Modifier.height(20.dp))
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RecentRow(
    template: QRTemplateRegistry.Template,
    onUseTemplate: (QRTemplateRegistry.Template) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onUseTemplate(template) },
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(tintFor(template).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        template.icon,
                        contentDescription = null,
                        tint = tintFor(template),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AllTemplateRow(
    template: QRTemplateRegistry.Template,
    onUseTemplate: (QRTemplateRegistry.Template) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onUseTemplate(template) },
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(tintFor(template).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        template.icon,
                        contentDescription = null,
                        tint = tintFor(template),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = template.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun tintFor(template: QRTemplateRegistry.Template): Color {
    return when (template.key) {
        "business_card", "simple_link" -> MaterialTheme.colorScheme.primary
        "home_wifi" -> MaterialTheme.colorScheme.secondary
        "event_invite", "support_email" -> MaterialTheme.colorScheme.tertiary
        "contact_phone" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }
}
