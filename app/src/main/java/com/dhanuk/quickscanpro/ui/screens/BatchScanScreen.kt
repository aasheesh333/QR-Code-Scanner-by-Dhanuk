package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.design.CameraPreviewBox
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsEmptyState
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.viewmodel.BatchScanViewModel
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BatchScanScreen(onNavigateBack: () -> Unit) {
    val vm: BatchScanViewModel = viewModel()
    val historyVm: HistoryViewModel = viewModel()
    val context = LocalContext.current
    val items by vm.results.collectAsState()
    val active by vm.isActive.collectAsState()

    val hasPerm = remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { g ->
        hasPerm.value = g
        if (g) vm.startBatch()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                        if (hasPerm.value) vm.startBatch()
                        else permLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            } else {
                if (active) {
                    CameraPreviewBox(
                        onScan = { content ->
                            if (vm.addResult(content)) {
                                Toast.makeText(context, "Added (${vm.totalScanned})", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCameraReady = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                QsButton(text = if (active) "Stop scanning" else "Resume scanning", onClick = {
                    if (active) vm.stopBatch()
                    else if (hasPerm.value) vm.startBatch()
                    else permLauncher.launch(Manifest.permission.CAMERA)
                })

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(items) { idx, item ->
                        QsCard(contentPadding = 12.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.content, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                    Text(item.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { vm.removeAt(idx) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }

                if (items.isEmpty()) {
                    Text("No codes yet", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QsOutlinedButton("Clear", onClick = { vm.clearAll() }, modifier = Modifier.weight(1f))
                        QsButton(
                            text = "Save (${items.size})",
                            icon = Icons.Filled.Save,
                            onClick = {
                                items.forEach {
                                    historyVm.addScanResult(ScanResult(content = it.content, type = it.type, timestamp = it.timestamp))
                                }
                                vm.clearAll()
                                Toast.makeText(context, "${items.size} saved", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
