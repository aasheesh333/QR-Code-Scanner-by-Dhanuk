package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen() {
    val vm: AnalyticsViewModel = viewModel()
    val stats by vm.stats.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnalyticsHeader()
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KPIRow(
                    totalScans = stats.totalScans,
                    generated = stats.totalGeneratedQRs,
                    leakChecks = 0
                )
                if (stats.totalScans > 0) {
                    ScanTypesCard(stats.topTypes)
                    WeeklyActivityCard()
                    TopSourcesCard(stats.topTypes)
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No scans yet. Start scanning to see analytics.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeader() {
    Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            Spacer(Modifier.weight(1f))
            Text("Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun KPIRow(totalScans: Int, generated: Int, leakChecks: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KPICard(Icons.Filled.QrCode, totalScans.toString(), "Total Scans", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        KPICard(Icons.Filled.AddBox, generated.toString(), "Generated", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
        KPICard(Icons.Filled.GppBad, leakChecks.toString(), "Leak Checks", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
    }
}

@Composable
private fun KPICard(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.W700, fontSize = 24.sp, letterSpacing = (-0.01).sp, lineHeight = 32.sp), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.W500, lineHeight = 16.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ScanTypesCard(types: List<Pair<String, Int>>) {
    if (types.isEmpty()) return
    val total = types.sumOf { it.second }
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 0.5.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Scan Types", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600), color = MaterialTheme.colorScheme.onSurface)
            types.take(3).forEach { (type, count) ->
                val pct = if (total > 0) (count * 100 / total) else 0
                val icon = when (type) {
                    "url" -> Icons.Filled.Link; "wifi" -> Icons.Filled.Wifi; "vcard" -> Icons.Filled.Person; else -> Icons.Filled.Link
                }
                val tint = when (type) {
                    "url" -> MaterialTheme.colorScheme.primary; "wifi" -> MaterialTheme.colorScheme.secondary; else -> MaterialTheme.colorScheme.tertiary
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                    Text(type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text("$pct%", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(progress = { pct / 100f }, color = tint, trackColor = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
            }
        }
    }
}

@Composable
private fun WeeklyActivityCard() {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val heights = listOf(0.30, 0.60, 0.45, 0.20, 0.80, 1.0, 0.50)
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 0.5.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Weekly Activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600), color = MaterialTheme.colorScheme.onSurface)
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 16.dp, bottom = 8.dp)) {
                Row(modifier = Modifier.fillMaxSize().align(Alignment.BottomCenter), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.forEachIndexed { idx, day ->
                        val h = heights[idx]
                        val isHighlight = idx == 1
                        val color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        Column(modifier = Modifier.weight(1f).align(Alignment.Bottom), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.width(24.dp).height((h * 120).dp).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(color))
                            Text(day, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.W500, lineHeight = 16.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSourcesCard(types: List<Pair<String, Int>>) {
    val urlType = types.firstOrNull { it.first == "url" }
    if (urlType == null) return
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 0.5.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Top Sources", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600), color = MaterialTheme.colorScheme.onSurface)
            listOf("google.com" to 45, "example.com" to 22, "github.com" to 14).forEach { (domain, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(domain, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500, fontSize = 16.sp, lineHeight = 24.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.width(64.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(modifier = Modifier.fillMaxWidth(((count / 45f))).height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(count.toString(), style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, lineHeight = 20.sp, letterSpacing = 0.01.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(32.dp))
                }
            }
        }
    }
}
