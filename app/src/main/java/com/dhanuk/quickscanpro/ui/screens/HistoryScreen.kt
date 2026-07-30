package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.clickable
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
    val collections by viewModel.collections.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var selectedCollectionId by remember { mutableStateOf<Int?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }
    var showNewCollectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) { viewModel.setSearchQuery(searchQuery) }
    LaunchedEffect(selectedType) { viewModel.setSelectedType(selectedType) }
    LaunchedEffect(showFavoritesOnly) { viewModel.setShowFavoritesOnly(showFavoritesOnly) }
    LaunchedEffect(selectedCollectionId) { viewModel.setSelectedCollection(selectedCollectionId) }

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

            // Collections row
            if (collections.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedCollectionId == null,
                        onClick = { selectedCollectionId = null },
                        label = { Text("All Scans") }
                    )
                    collections.forEach { collection ->
                        FilterChip(
                            selected = selectedCollectionId == collection.id,
                            onClick = {
                                selectedCollectionId =
                                    if (selectedCollectionId == collection.id) null
                                    else collection.id
                            },
                            label = { Text("${collection.emoji} ${collection.name}") }
                        )
                    }
                    AssistChip(
                        onClick = { showNewCollectionDialog = true },
                        label = { Text("+ New") },
                        leadingIcon = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

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
                    selected = selectedType == null && !showFavoritesOnly && selectedCollectionId == null,
                    onClick = { selectedType = null; showFavoritesOnly = false; selectedCollectionId = null },
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
                            collections = collections,
                            onDelete = { pendingDeleteId = scanResult.id },
                            onToggleFavorite = { viewModel.toggleFavorite(scanResult) },
                            onSaveNote = { note -> viewModel.setNote(scanResult.id, note) },
                            onAssignCollection = { collectionId ->
                                viewModel.setCollection(scanResult.id, collectionId)
                            },
                            onDeleteCollection = { viewModel.deleteCollection(it) }
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

    if (showNewCollectionDialog) {
        NewCollectionDialog(
            onDismiss = { showNewCollectionDialog = false },
            onCreate = { name, emoji ->
                viewModel.addCollection(name, 0x700B97L, emoji)
                showNewCollectionDialog = false
            }
        )
    }
}

@Composable
private fun NewCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📁") }
    val emojis = listOf("📁", "🔗", "🔐", "👤", "🏠", "📞", "🏪", "🎯", "⭐", "💡")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New collection") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Collection name...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text("Icon", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    emojis.forEach { e ->
                        FilterChip(
                            selected = emoji == e,
                            onClick = { emoji = e },
                            label = { Text(e) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim(), emoji) },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun HistoryItem(
    scanResult: ScanResult,
    collections: List<com.dhanuk.quickscanpro.database.ScanCollection>,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSaveNote: (String) -> Unit,
    onAssignCollection: (Int?) -> Unit,
    onDeleteCollection: (Int) -> Unit
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var showCollectionPicker by remember { mutableStateOf(false) }

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
            IconButton(onClick = { showCollectionPicker = true }) {
                Icon(
                    if (scanResult.collectionId != null) Icons.Filled.Folder else Icons.Filled.FolderOpen,
                    contentDescription = "Collection",
                    tint = if (scanResult.collectionId != null)
                        LuminaPrimaryGlow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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

    if (showCollectionPicker) {
        AlertDialog(
            onDismissRequest = { showCollectionPicker = false },
            title = { Text("Move to collection") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("None (remove)") },
                        leadingContent = {
                            Icon(
                                if (scanResult.collectionId == null) Icons.Filled.RadioButtonChecked
                                else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = LuminaPrimaryGlow
                            )
                        },
                        modifier = Modifier.clickable {
                            onAssignCollection(null)
                            showCollectionPicker = false
                        }
                    )
                    HorizontalDivider()
                    collections.forEach { collection ->
                        ListItem(
                            headlineContent = { Text("${collection.emoji} ${collection.name}") },
                            leadingContent = {
                                Icon(
                                    if (scanResult.collectionId == collection.id) Icons.Filled.RadioButtonChecked
                                    else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = LuminaPrimaryGlow
                                )
                            },
                            trailingContent = {
                                if (collections.size > 1 || scanResult.collectionId == collection.id) {
                                    TextButton(
                                        onClick = { onDeleteCollection(collection.id) },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete collection", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier.clickable {
                                onAssignCollection(collection.id)
                                showCollectionPicker = false
                            }
                        )
                    }
                    if (collections.isEmpty()) {
                        Text(
                            "No collections yet. Create one with the \"+ New\" button above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCollectionPicker = false }) { Text("Done") }
            }
        )
    }
}
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
