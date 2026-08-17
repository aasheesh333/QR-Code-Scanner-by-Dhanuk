package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.design.CameraPreviewBox
import com.dhanuk.quickscanpro.ui.design.previewHeight
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsEmptyState
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.HistoryExporter
import com.dhanuk.quickscanpro.viewmodel.BatchScanViewModel
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BatchScanScreen(onNavigateBack: () -> Unit) {
    val vm: BatchScanViewModel = viewModel()
    val historyVm: HistoryViewModel = viewModel()
    val settingsVm: SettingsViewModel = viewModel()
    val historyEnabled by settingsVm.scanHistory.collectAsState()
    val incognito by settingsVm.incognitoMode.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val items by vm.results.collectAsState()
    val active by vm.isActive.collectAsState()

    var pendingRemove by remember { mutableStateOf<com.dhanuk.quickscanpro.viewmodel.BatchScanItem?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val hasPerm = remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { g ->
        hasPerm.value = g
        if (g) vm.startBatch()
    }

    // Re-reads the live grant so returning from system settings doesn't re-prompt.
    fun cameraGranted(): Boolean {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasPerm.value = granted
        return granted
    }

    fun exportCsv() {
        scope.launch {
            val rows = items.map {
                ScanResult(content = it.content, type = it.type, timestamp = it.timestamp)
            }
            val uri = HistoryExporter.exportAsCsv(context, rows)
            if (uri != null) HistoryExporter.shareCsv(context, uri)
        }
    }

    fun shareResults() {
        val text = buildString {
            appendLine("QuickScan Pro batch scan — ${items.size} items")
            items.forEachIndexed { i, it -> appendLine("${i + 1}. [${it.type.uppercase()}] ${it.content}") }
        }
        runCatching {
            context.startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                "Share batch results"
            ))
        }.onFailure { Toast.makeText(context, "No app to share results", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Batch Scan", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { vm.stopBatch(); onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            if (!active && items.isEmpty()) {
                QsEmptyState(
                    icon = Icons.Filled.QrCodeScanner,
                    title = "Scan multiple codes",
                    subtitle = "Start scanning, and every unique code gets added to this list.",
                    actionLabel = "Start batch",
                    onAction = {
                        if (cameraGranted()) vm.startBatch()
                        else permLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (active) {
                        item {
                            CameraPreviewBox(
                                onScan = { content ->
                                    if (vm.addResult(content)) {
                                        Toast.makeText(context, "Added (${vm.totalScanned})", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onCameraReady = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(previewHeight(0.28f, 170.dp, 260.dp))
                            )
                        }
                    }

                    item {
                        QsButton(text = if (active) "Stop scanning" else "Resume scanning", onClick = {
                            if (active) vm.stopBatch()
                            else if (cameraGranted()) vm.startBatch()
                            else permLauncher.launch(Manifest.permission.CAMERA)
                        })
                    }

                    if (items.isEmpty()) {
                        item {
                            Text("No codes yet", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp))
                        }
                    } else {
                        itemsIndexed(items) { _, item ->
                            QsCard(contentPadding = 12.dp) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.content, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                        Text(item.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { pendingRemove = item }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                    }
                                }
                            }
                        }
                        val total = items.size
                        val unique = items.distinctBy { it.content }.size
                        val products = items.count { it.type == BarcodeTypeDetector.TYPE_PRODUCT }
                        item {
                            QsCard(contentPadding = 14.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Session summary", style = MaterialTheme.typography.titleSmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                                        StatChip("$total", "Total", Modifier.weight(1f))
                                        StatChip("$unique", "Unique", Modifier.weight(1f))
                                        StatChip("$products", "Products", Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                QsOutlinedButton(
                                    text = "Export CSV",
                                    icon = Icons.Filled.FileDownload,
                                    onClick = { exportCsv() },
                                    modifier = Modifier.weight(1f)
                                )
                                QsOutlinedButton(
                                    text = "Share",
                                    icon = Icons.Filled.Share,
                                    onClick = { shareResults() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                QsOutlinedButton("Clear", onClick = { showClearConfirm = true }, modifier = Modifier.weight(1f))
                                QsButton(
                                    text = "Save (${items.size})",
                                    icon = Icons.Filled.Save,
                                    onClick = {
                                        if (historyEnabled && !incognito) {
                                            items.forEach {
                                                historyVm.addScanResult(ScanResult(content = it.content, type = it.type, timestamp = it.timestamp))
                                            }
                                            vm.clearAll()
                                            Toast.makeText(context, "${items.size} saved to history", Toast.LENGTH_SHORT).show()
                                            onNavigateBack()
                                        } else {
                                            Toast.makeText(context, "History saving is off — enable it in Settings", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    pendingRemove?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text("Remove this item?") },
            text = { Text("It will be removed from the current batch session.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.remove(item)
                    pendingRemove = null
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingRemove = null }) { Text("Cancel") } }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all results?") },
            text = { Text("Every item in this batch session will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAll()
                    showClearConfirm = false
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatChip(value: String, label: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
