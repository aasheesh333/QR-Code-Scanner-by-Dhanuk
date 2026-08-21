package com.dhanuk.quickscanpro.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ads.InterstitialAdManager
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.PillChip
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsEmptyState
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.HistoryExporter
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpenVault: () -> Unit,
    onOpenTimeline: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onRowClick: (ScanResult) -> Unit
) {
    val vm: HistoryViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val items by vm.filteredHistory.collectAsState()
    val all by vm.history.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    var pendingDelete by remember { mutableStateOf<ScanResult?>(null) }

    fun exportNow() {
        if (all.isEmpty()) {
            Toast.makeText(context, "Nothing to export", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            val uri = HistoryExporter.exportAsCsv(context, all)
            if (uri != null) {
                HistoryExporter.shareCsv(context, uri)
                InterstitialAdManager.showAfterAction(context)
            }
        }
    }

    val filters = listOf("All", "Favorites", "Link", "Wi-Fi", "Contact", "Product")
    val visible = remember(items, filter) {
        when (filter) {
            "Favorites" -> items.filter { it.isFavorite }
            "Link" -> items.filter { it.type == BarcodeTypeDetector.TYPE_URL }
            "Wi-Fi" -> items.filter { it.type == BarcodeTypeDetector.TYPE_WIFI }
            "Contact" -> items.filter {
                it.type == BarcodeTypeDetector.TYPE_VCARD || it.type == BarcodeTypeDetector.TYPE_PHONE ||
                    it.type == BarcodeTypeDetector.TYPE_EMAIL || it.type == BarcodeTypeDetector.TYPE_SMS
            }
            "Product" -> items.filter { it.type == BarcodeTypeDetector.TYPE_PRODUCT }
            else -> items
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("History", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onOpenTimeline) {
                        Icon(Icons.Filled.SmartDisplay, contentDescription = "Timeline")
                    }
                    IconButton(onClick = onOpenVault) {
                        Icon(Icons.Filled.Lock, contentDescription = "Vault")
                    }
                    IconButton(onClick = { exportNow() }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Export history")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    vm.setSearchQuery(it)
                },
                placeholder = { Text("Search your scans") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { f ->
                    PillChip(label = f, selected = f == filter, onClick = { filter = f })
                }
            }

            Spacer(Modifier.height(6.dp))

            when {
                visible.isEmpty() && search.isNotBlank() -> QsEmptyState(
                    icon = Icons.Filled.Search,
                    title = "No matches",
                    subtitle = "Try a different search term.",
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                visible.isEmpty() -> QsEmptyState(
                    icon = Icons.Filled.QrCodeScanner,
                    title = "No scans yet",
                    subtitle = "Scanned codes will show up here automatically.",
                    actionLabel = "Start scanning",
                    onAction = onNavigateToScanner,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                else -> {
                    val displayItems = remember(visible) {
                        val out = mutableListOf<HistItem>()
                        val seenBatches = mutableSetOf<String>()
                        visible.forEach { s ->
                            val b = s.batchId
                            if (b == null) {
                                out.add(HistItem.Single(s))
                            } else if (seenBatches.add(b)) {
                                val children = visible.filter { it.batchId == b }
                                out.add(HistItem.Batch(b, children))
                            }
                        }
                        out
                    }
                    var expandedBatch by rememberSaveable { mutableStateOf<String?>(null) }
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayItems, key = { it.key }) { item ->
                            when (item) {
                                is HistItem.Single -> HistoryRow(
                                    scan = item.scan,
                                    onClick = { onRowClick(item.scan) },
                                    onFavorite = { vm.toggleFavorite(item.scan) },
                                    onShare = { shareScan(context, item.scan) },
                                    onHide = { vm.setHidden(item.scan, true) },
                                    onDelete = { pendingDelete = item.scan }
                                )
                                is HistItem.Batch -> BatchGroupRow(
                                    batch = item,
                                    expanded = expandedBatch == item.batchId,
                                    onToggleExpand = {
                                        expandedBatch = if (expandedBatch == item.batchId) null else item.batchId
                                    },
                                    vm = vm,
                                    onRowClick = onRowClick,
                                    onDelete = { pendingDelete = it }
                                )
                            }
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }

    pendingDelete?.let { scan ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this scan?") },
            text = { Text("This will permanently remove the scan from your history.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(scan.id, context)
                    pendingDelete = null
                    scope.launch {
                        val res = snackbar.showSnackbar("Scan deleted", actionLabel = "Undo")
                        if (res == SnackbarResult.ActionPerformed) vm.restore(scan)
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

private sealed class HistItem {
    abstract val key: String
    data class Single(val scan: ScanResult) : HistItem() {
        override val key: String get() = "s${scan.id}"
    }
    data class Batch(val batchId: String, val children: List<ScanResult>) : HistItem() {
        override val key: String get() = "b$batchId"
    }
}

private fun shareScan(context: android.content.Context, scan: ScanResult) {
    runCatching {
        context.startActivity(android.content.Intent.createChooser(
            android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, scan.content)
            },
            "Share scan"
        ))
    }.onFailure { Toast.makeText(context, "No app to share with", Toast.LENGTH_SHORT).show() }
}

@Composable
private fun BatchGroupRow(
    batch: HistItem.Batch,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    vm: HistoryViewModel,
    onRowClick: (ScanResult) -> Unit,
    onDelete: (ScanResult) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val batchId = batch.batchId
    // Full children (incl. hidden) so hidden rows stay reachable / unhideable.
    val allChildren by remember(batchId) { vm.batchItems(batchId) }
        .collectAsState(initial = batch.children)
    val children = if (allChildren.isEmpty()) batch.children else allChildren
    val visibleChildren = children.filter { !it.isHidden }
    val anyHidden = children.any { it.isHidden }
    val latest = children.maxOfOrNull { it.timestamp } ?: 0L
    var showBatchDelete by remember { mutableStateOf(false) }

    QsCard(contentPadding = 14.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconBadge(Icons.Filled.Layers)
            Spacer(Modifier.size(12.dp))
            Column(
                Modifier
                    .weight(1f)
                    .clickable(onClick = onToggleExpand)
            ) {
                Text(
                    "Bulk scan (${children.size} items)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${visibleChildren.size} visible · ${relativeTime(latest)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleExpand) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (expanded) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(6.dp))

            // Group-level actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = {
                    scope.launch {
                        if (visibleChildren.isEmpty()) {
                            Toast.makeText(context, "Nothing visible to export — unhide rows first", Toast.LENGTH_SHORT).show()
                        } else {
                            val uri = HistoryExporter.exportAsCsv(context, visibleChildren)
                            if (uri != null) HistoryExporter.shareCsv(context, uri)
                        }
                    }
                }) { Icon(Icons.Filled.FileDownload, contentDescription = "Export batch", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = {
                    if (visibleChildren.isEmpty()) {
                        Toast.makeText(context, "Nothing visible to share — unhide rows first", Toast.LENGTH_SHORT).show()
                        return@IconButton
                    }
                    val text = buildString {
                        appendLine("QuickScan Pro bulk scan — ${visibleChildren.size} items")
                        visibleChildren.forEachIndexed { i, it -> appendLine("${i + 1}. [${it.type.uppercase()}] ${it.content}") }
                    }
                    runCatching {
                        context.startActivity(android.content.Intent.createChooser(
                            android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, text)
                            },
                            "Share bulk scan"
                        ))
                    }
                }) { Icon(Icons.Filled.Share, contentDescription = "Share batch", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = {
                    if (anyHidden) vm.setBatchHidden(batchId, false) else vm.setBatchHidden(batchId, true)
                    Toast.makeText(
                        context,
                        if (anyHidden) "All rows unhidden" else "All rows hidden (excluded from export)",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Icon(
                        if (anyHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (anyHidden) "Unhide all" else "Hide all",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showBatchDelete = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete batch", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(4.dp))
            children.forEach { child ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !child.isHidden) { onRowClick(child) }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        historyIcon(child.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (child.isHidden) 0.3f else 1f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            child.note.ifBlank { child.content },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (child.isHidden) 0.35f else 1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (child.isHidden) {
                            Text(
                                "Hidden — excluded from history & exports",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                    IconButton(onClick = { shareScan(context, child) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { vm.setHidden(child, !child.isHidden) }) {
                        Icon(
                            if (child.isHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (child.isHidden) "Unhide" else "Hide",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { onDelete(child) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showBatchDelete) {
        AlertDialog(
            onDismissRequest = { showBatchDelete = false },
            title = { Text("Delete this batch?") },
            text = { Text("All ${children.size} scans in this bulk scan will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteBatch(batchId)
                    showBatchDelete = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showBatchDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HistoryRow(
    scan: ScanResult,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit
) {
    QsCard(onClick = onClick, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(historyIcon(scan.type))
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    scan.note.ifBlank { scan.content },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    buildString {
                        append(historyLabel(scan.type))
                        append(" · ")
                        append(relativeTime(scan.timestamp))
                        if (scan.isVault) append(" · 🔒")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    if (scan.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Toggle favorite",
                    tint = if (scan.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "Share scan", tint = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onHide) {
                Icon(Icons.Filled.VisibilityOff, contentDescription = "Hide from history", tint = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete scan", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

private fun historyIcon(type: String): ImageVector = when (type) {
    BarcodeTypeDetector.TYPE_URL -> Icons.Filled.Link
    BarcodeTypeDetector.TYPE_WIFI -> Icons.Filled.Wifi
    BarcodeTypeDetector.TYPE_VCARD -> Icons.Filled.Person
    BarcodeTypeDetector.TYPE_EMAIL -> Icons.Filled.Email
    BarcodeTypeDetector.TYPE_PHONE -> Icons.Filled.Call
    BarcodeTypeDetector.TYPE_SMS -> Icons.Filled.Sms
    BarcodeTypeDetector.TYPE_GEO -> Icons.Filled.LocationOn
    BarcodeTypeDetector.TYPE_PRODUCT -> Icons.Filled.SdStorage
    else -> Icons.Filled.Description
}

private fun historyLabel(type: String) = when (type) {
    BarcodeTypeDetector.TYPE_URL -> "Link"
    BarcodeTypeDetector.TYPE_WIFI -> "Wi-Fi"
    BarcodeTypeDetector.TYPE_VCARD -> "Contact"
    BarcodeTypeDetector.TYPE_EMAIL -> "Email"
    BarcodeTypeDetector.TYPE_PHONE -> "Phone"
    BarcodeTypeDetector.TYPE_SMS -> "SMS"
    BarcodeTypeDetector.TYPE_GEO -> "Location"
    BarcodeTypeDetector.TYPE_PRODUCT -> "Product"
    BarcodeTypeDetector.TYPE_CALENDAR -> "Event"
    else -> "Text"
}

private fun relativeTime(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "yesterday"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(ts))
    }
}
