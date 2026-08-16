package com.dhanuk.quickscanpro.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
                    IconButton(
                        onClick = {
                            if (all.isEmpty()) {
                                Toast.makeText(context, "Nothing to export", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    val uri = HistoryExporter.exportAsCsv(context, all)
                                    if (uri != null) {
                                        HistoryExporter.shareCsv(context, uri)
                                    }
                                }
                            }
                        }
                    ) {
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
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visible, key = { it.id }) { scan ->
                        HistoryRow(
                            scan = scan,
                            onClick = { onRowClick(scan) },
                            onFavorite = { vm.toggleFavorite(scan) },
                            onDelete = {
                                vm.delete(scan.id)
                                scope.launch {
                                    val res = snackbar.showSnackbar("Scan deleted", actionLabel = "Undo")
                                    if (res == SnackbarResult.ActionPerformed) vm.restore(scan)
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    scan: ScanResult,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
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
