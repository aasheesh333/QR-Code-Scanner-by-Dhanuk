package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.ui.composables.StatCard
import com.dhanuk.quickscanpro.ui.theme.DhanukAccent
import com.dhanuk.quickscanpro.ui.theme.DhanukPrimary
import com.dhanuk.quickscanpro.ui.theme.DhanukSecondary
import com.dhanuk.quickscanpro.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    val vm: AnalyticsViewModel = viewModel()
    val stats by vm.stats.collectAsState()

    val barColors = listOf(
        DhanukPrimary, DhanukSecondary, DhanukAccent,
        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6)
    )

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Statistics", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Scans",
                    value = stats.totalScans.toString(),
                    icon = Icons.Filled.QrCodeScanner,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Generated",
                    value = stats.totalGeneratedQRs.toString(),
                    icon = Icons.Filled.QrCode2,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "This Week",
                    value = stats.scansThisWeek.toString(),
                    icon = Icons.Filled.DateRange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Today",
                    value = stats.scansToday.toString(),
                    icon = Icons.Filled.Today,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bar chart of top types
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Top Scan Types",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(16.dp))

                    if (stats.topTypes.isEmpty()) {
                        Text(
                            "No scans yet. Start scanning to see charts!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        val maxCount = stats.topTypes.maxOf { it.second }.coerceAtLeast(1)
                        stats.topTypes.forEachIndexed { idx, (type, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = type.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(72.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val bw = size.width * (count.toFloat() / maxCount)
                                        drawRect(
                                            color = barColors[idx % barColors.size].copy(alpha = 0.8f),
                                            topLeft = Offset.Zero,
                                            size = Size(bw, size.height)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
