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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.composables.EmptyState
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(onNavigateBack: () -> Unit) {
    val vm: HistoryViewModel = viewModel()
    val items by vm.history.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TimelineHeader(onNavigateBack)
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                EmptyState(icon = Icons.Filled.Description, title = "No scans yet", subtitle = "Start scanning to build your timeline")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val grouped = groupByDay(items)
                grouped.forEach { (dayLabel, dayItems) ->
                    item { Text(dayLabel, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp)) }
                    item {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)), shadowElevation = 0.5.dp) {
                            Column {
                                dayItems.forEachIndexed { idx, scan ->
                                    TimelineRow(scan)
                                    if (idx < dayItems.lastIndex) Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 56.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineHeader(onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary) }
            Text("Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun TimelineRow(scan: ScanResult) {
    val icon = timelineIcon(scan.type)
    val containerColor = timelineColor(scan.type)
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(containerColor), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(scan.content, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, fontWeight = FontWeight.W500)
            Text(scan.typeLabel(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.W500, lineHeight = 16.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Text(SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(scan.timestamp)), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.W500, lineHeight = 16.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun ScanResult.typeLabel() = when (type) {
    BarcodeTypeDetector.TYPE_URL -> "Web URL"; BarcodeTypeDetector.TYPE_WIFI -> "Wi-Fi Network"; BarcodeTypeDetector.TYPE_VCARD -> "Contact Info"; BarcodeTypeDetector.TYPE_EMAIL -> "Email"; BarcodeTypeDetector.TYPE_PHONE -> "Phone"; BarcodeTypeDetector.TYPE_SMS -> "SMS"
    else -> "Plain Text"
}

private fun timelineIcon(type: String) = when (type) {
    BarcodeTypeDetector.TYPE_URL -> Icons.Filled.Link; BarcodeTypeDetector.TYPE_WIFI -> Icons.Filled.Wifi; BarcodeTypeDetector.TYPE_VCARD -> Icons.Filled.Person; BarcodeTypeDetector.TYPE_EMAIL -> Icons.Filled.Email; BarcodeTypeDetector.TYPE_PHONE -> Icons.Filled.Phone; BarcodeTypeDetector.TYPE_SMS -> Icons.Filled.Sms; else -> Icons.Filled.Description
}

@Composable
private fun timelineColor(type: String) = when (type) {
    BarcodeTypeDetector.TYPE_VCARD -> MaterialTheme.colorScheme.secondaryContainer; BarcodeTypeDetector.TYPE_EMAIL -> MaterialTheme.colorScheme.tertiaryContainer; else -> MaterialTheme.colorScheme.primaryContainer
}

private fun groupByDay(items: List<ScanResult>): List<Pair<String, List<ScanResult>>> {
    val now = System.currentTimeMillis()
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
    val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now - 86400000L))
    val groups = items.groupBy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) }
    return groups.map { (day, scans) ->
        val label = when (day) {
            today -> "Today"; yesterday -> "Yesterday"; else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(scans.first().timestamp))
        }
        label to scans
    }
}
