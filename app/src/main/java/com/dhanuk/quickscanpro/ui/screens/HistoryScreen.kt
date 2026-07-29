package com.dhanuk.quickscanpro.ui.screens

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

import com.dhanuk.quickscanpro.ui.composables.EmptyState
import com.dhanuk.quickscanpro.ui.composables.GlassCard
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimary
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimaryGlow
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
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(searchQuery) { viewModel.setSearchQuery(searchQuery) }
    LaunchedEffect(selectedType) { viewModel.setSelectedType(selectedType) }
    LaunchedEffect(showFavoritesOnly) { viewModel.setShowFavoritesOnly(showFavoritesOnly) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text("History", style = MaterialTheme.typography.headlineSmall)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(
                        onClick = { showDeleteAllDialog = true },
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
                            onDelete = { pendingDeleteId = scanResult.id },
                            onToggleFavorite = { viewModel.toggleFavorite(scanResult) },
                            onSaveNote = { note -> viewModel.setNote(scanResult.id, note) }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Clear all history?") },
            text = { Text("This will permanently delete all ${fullHistory.size} scanned items.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAll()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("Cancel") }
            }
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete this item?") },
            text = { Text("This scan result will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(id)
                        pendingDeleteId = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HistoryItem(
    scanResult: ScanResult,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    var showNoteDialog by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        cornerRadius = 18.dp,
        glowColor = if (scanResult.isFavorite) LuminaPrimaryGlow else null
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
                if (scanResult.note.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.StickyNote2, contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = LuminaPrimaryGlow)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = scanResult.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = LuminaPrimaryGlow,
                            maxLines = 1
                        )
                    }
                }
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
            IconButton(onClick = { showNoteDialog = true }) {
                Icon(
                    Icons.Filled.EditNote,
                    contentDescription = "Note",
                    tint = if (scanResult.note.isNotBlank()) LuminaPrimaryGlow
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
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

    if (showNoteDialog) {
        var noteText by remember { mutableStateOf(scanResult.note) }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Scan Note") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Add a reminder or note...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onSaveNote(noteText.trim())
                    showNoteDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
