package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.composables.BannerAd
import com.dhanuk.quickscanpro.ui.composables.EmptyState
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.HistoryExporter
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    val viewModel: HistoryViewModel = viewModel()
    val context = LocalContext.current
    val fullHistory by viewModel.history.collectAsState()
    val filtered by viewModel.filteredHistory.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) { viewModel.setSearchQuery(searchQuery) }
    LaunchedEffect(selectedType) { viewModel.setSelectedType(selectedType) }
    LaunchedEffect(showFavoritesOnly) { viewModel.setShowFavoritesOnly(showFavoritesOnly) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteAll()
                        },
                        enabled = fullHistory.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear All")
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val uri = HistoryExporter.exportAsCsv(context, fullHistory)
                                uri?.let { HistoryExporter.shareCsv(context, it) }
                            }
                        },
                        enabled = fullHistory.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Export CSV")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search history...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                }
            )

            // Type filter chips
            val typeChips = listOf(
                BarcodeTypeDetector.TYPE_URL,
                BarcodeTypeDetector.TYPE_TEXT,
                BarcodeTypeDetector.TYPE_WIFI,
                BarcodeTypeDetector.TYPE_VCARD,
                BarcodeTypeDetector.TYPE_PHONE,
                BarcodeTypeDetector.TYPE_PRODUCT
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == null && !showFavoritesOnly,
                    onClick = { selectedType = null; showFavoritesOnly = false },
                    label = { Text("All") }
                )
                typeChips.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = if (selectedType == type) null else type
                            showFavoritesOnly = false
                        },
                        label = { Text(type.uppercase()) }
                    )
                }
                FilterChip(
                    selected = showFavoritesOnly,
                    onClick = { showFavoritesOnly = !showFavoritesOnly },
                    label = { Text("★ Fav") }
                )
            }

            // List
            if (filtered.isEmpty()) {
                EmptyState(
                    icon = if (fullHistory.isEmpty()) Icons.Filled.History else Icons.Filled.SearchOff,
                    title = if (fullHistory.isEmpty()) "No history yet" else "No matches",
                    message = if (fullHistory.isEmpty())
                        "Scanned codes will appear here"
                    else "Try a different search or filter"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { scanResult ->
                        HistoryItem(
                            scanResult = scanResult,
                            onDelete = { viewModel.delete(scanResult.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(scanResult) }
                        )
                    }
                }
            }
            BannerAd(adUnitId = com.dhanuk.quickscanpro.config.AppConfig.AdMob.BANNER_AD_UNIT_ID_HISTORY)
        }
    }
}

@Composable
private fun HistoryItem(
    scanResult: ScanResult,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val cardColor by animateColorAsState(
        if (scanResult.isFavorite) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "card"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scanResult.content,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[${scanResult.type.uppercase()}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatTimestamp(scanResult.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (scanResult.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (scanResult.isFavorite)
                        Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
