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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.viewmodel.AnalyticsViewModel
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel

@Composable
fun AnalyticsScreen() {
    val vm: AnalyticsViewModel = viewModel()
    val stats by vm.stats.collectAsState()
    val historyVm: HistoryViewModel = viewModel()
    val leakCheckList by historyVm.leakChecks.collectAsState()
    val totalScans = stats.totalScans.coerceAtLeast(0)
    val generated = stats.totalGeneratedQRs.coerceAtLeast(0)
    val leakChecks = leakCheckList.size

    AppBackground()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        AnalyticsHeader()
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            KpiRow(
                totalScans = totalScans,
                generated = generated,
                leakChecks = leakChecks
            )
            Spacer(Modifier.height(24.dp))
            if (totalScans > 0) {
                ScanTypesCard(stats.topTypes, totalScans)
                Spacer(Modifier.height(24.dp))
            }
            WeeklyActivityCard()
            Spacer(Modifier.height(24.dp))
            TopSourcesCard()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnalyticsHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun KpiRow(
    totalScans: Int,
    generated: Int,
    leakChecks: Int
) {
    val cards = listOf(
        Kpi(Icons.Filled.QrCode, "$totalScans", "Total Scans", MaterialTheme.colorScheme.primary),
        Kpi(Icons.Filled.AddBox, "$generated", "Generated", MaterialTheme.colorScheme.secondary),
        Kpi(Icons.Filled.GppMaybe, "$leakChecks", "Leak Checks", MaterialTheme.colorScheme.tertiary)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cards.forEach { kpi ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = kpi.icon,
                        contentDescription = null,
                        tint = kpi.color,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = kpi.value,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = kpi.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private data class Kpi(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val value: String,
    val label: String,
    val color: Color
)

@Composable
private fun SurfaceCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun CardTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun ScanTypesCard(
    vmTypes: List<Pair<String, Int>>,
    totalScans: Int
) {
    val types = if (vmTypes.isNotEmpty() && totalScans > 0) {
        vmTypes.take(3).map { (type, count) ->
            val pct = count.toFloat() / totalScans * 100f
            val mapped = typeMeta(type.lowercase())
            TypeRow(mapped.first, mapped.second, pct, mapped.third)
        }
    } else {
        listOf(
            TypeRow(Icons.Filled.Link, "URL", 65f, MaterialTheme.colorScheme.primary),
            TypeRow(Icons.Filled.Wifi, "Wi-Fi", 20f, MaterialTheme.colorScheme.secondary),
            TypeRow(Icons.Filled.ContactPage, "vCard", 15f, MaterialTheme.colorScheme.tertiary)
        )
    }
    SurfaceCard {
        CardTitle("Scan Types")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            types.forEach { row ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = row.icon,
                                contentDescription = null,
                                tint = row.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = row.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${row.percent.toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    ProgressTrack(percent = row.percent, color = row.color)
                }
            }
        }
    }
}

private data class TypeRow(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val percent: Float,
    val color: Color
)

private fun typeMeta(type: String): Triple<androidx.compose.ui.graphics.vector.ImageVector, String, Color> {
    val primary = Color(0xFF004AC6)
    val secondary = Color(0xFF006C49)
    val tertiary = Color(0xFF943700)
    return when {
        type.contains("url") || type.contains("link") -> Triple(Icons.Filled.Link, "URL", primary)
        type.contains("wifi") -> Triple(Icons.Filled.Wifi, "Wi-Fi", secondary)
        type.contains("vcard") || type.contains("contact") -> Triple(Icons.Filled.ContactPage, "vCard", tertiary)
        type.contains("text") -> Triple(Icons.Filled.Description, "Text", primary)
        else -> Triple(Icons.Filled.QrCode, "Other", secondary)
    }
}

@Composable
private fun ProgressTrack(percent: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percent.coerceIn(0f, 100f) / 100f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
private fun WeeklyActivityCard() {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val heights = listOf(0.30f, 0.60f, 0.45f, 0.20f, 0.80f, 1.00f, 0.50f)
    val maxIndex = 5
    SurfaceCard {
        CardTitle("Weekly Activity")
        BarChart(days = days, heights = heights, highlightIndex = maxIndex)
    }
}

@Composable
private fun BarChart(
    days: List<String>,
    heights: List<Float>,
    highlightIndex: Int
) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.indices.forEach { i ->
            val h = heights.getOrElse(i) { 0.1f }.coerceAtLeast(0.05f)
            val isHighlight = i == highlightIndex
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .width(24.dp)
                        .height(120.dp)
                ) {
                    val barHeight = size.height * h
                    val barWidth = size.width
                    drawRoundRect(
                        color = if (isHighlight) primary else track,
                        topLeft = Offset(0f, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = days[i],
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
        }
    }
}

@Composable
private fun TopSourcesCard() {
    val sources = listOf(
        Source(Icons.Filled.Language, "google.com", 45, 0.80f),
        Source(Icons.Filled.Language, "example.com", 22, 0.40f),
        Source(Icons.Filled.Language, "github.com", 14, 0.25f)
    )
    SurfaceCard {
        CardTitle("Top Sources")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            sources.forEach { src ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = src.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = src.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(src.fill.coerceIn(0f, 1f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${src.count}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private data class Source(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val name: String,
    val count: Int,
    val fill: Float
)
