package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.QRTemplate
import com.dhanuk.quickscanpro.ui.composables.EmptyState
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel

@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onUseTemplate: (QRTemplate) -> Unit
) {
    val vm: HistoryViewModel = viewModel()
    val recentTemplates by vm.recentTemplates.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TemplatesHeader(onNavigateBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (recentTemplates.isNotEmpty()) {
                Text("RECENT", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentTemplates.take(2).forEach { t -> RecentTemplateRow(t, onUseTemplate) }
                }
            }
            Text("ALL TEMPLATES", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ALL_TEMPLATES.forEach { t -> TemplateCardRow(t, onUseTemplate) }
            }
        }
    }
}

@Composable
private fun TemplatesHeader(onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary) }
            Text("Templates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RecentTemplateRow(template: QRTemplate, onUseTemplate: (QRTemplate) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onUseTemplate(template) },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(tintForType(template.type).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(iconForType(template.type), contentDescription = null, tint = tintForType(template.type), modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(template.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun TemplateCardRow(template: TemplateDef, onUseTemplate: (QRTemplate) -> Unit) {
    val qrTemplate = QRTemplate(name = template.name, type = template.type, prefill = template.prefill)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onUseTemplate(qrTemplate) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(template.tint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(template.icon, contentDescription = null, tint = template.tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W600, fontSize = 16.sp, lineHeight = 24.sp), color = MaterialTheme.colorScheme.onSurface)
                Text(template.subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.W500, lineHeight = 16.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
    }
}

data class TemplateDef(val name: String, val type: String, val subtitle: String, val icon: ImageVector, val tint: androidx.compose.ui.graphics.Color, val prefill: QRTemplate.Prefill)

@Composable
private val ALL_TEMPLATES = listOf(
    TemplateDef("Business Card", "vcard", "vCard with name, phone, email", Icons.Filled.Badge, MaterialTheme.colorScheme.primary, QRTemplate.Prefill("", "", "", "")),
    TemplateDef("Home Wi-Fi", "wifi", "Share your Wi-Fi with guests", Icons.Filled.Wifi, MaterialTheme.colorScheme.secondary, QRTemplate.Prefill("", "", "", "")),
    TemplateDef("Event Invite", "calendar", "ICS calendar event", Icons.Filled.Event, MaterialTheme.colorScheme.tertiary, QRTemplate.Prefill("", "", "", "")),
    TemplateDef("Tap-to-Call", "phone", "tel: link", Icons.Filled.Phone, MaterialTheme.colorScheme.primary, QRTemplate.Prefill("", "", "", "")),
    TemplateDef("Website Link", "url", "Any URL you want to share", Icons.Filled.Link, MaterialTheme.colorScheme.primary, QRTemplate.Prefill("", "", "", "")),
    TemplateDef("Support Email", "email", "Pre-filled mail message", Icons.Filled.Mail, MaterialTheme.colorScheme.tertiary, QRTemplate.Prefill("", "", "", ""))
)

@Composable
private fun tintForType(type: String) = when (type) {
    "wifi" -> androidx.compose.ui.graphics.Color(0xFF006C49); "calendar" -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.primary
}
private fun iconForType(type: String) = when (type) {
    "wifi" -> Icons.Filled.Wifi; "vcard" -> Icons.Filled.Badge; "calendar" -> Icons.Filled.Event; "phone" -> Icons.Filled.Phone; "email" -> Icons.Filled.Mail; else -> Icons.Filled.Link
}
