package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.ui.composables.SecondaryButton
import com.dhanuk.quickscanpro.viewmodel.BatchScanViewModel
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScanScreen(onNavigateBack: () -> Unit) {
    val vm: BatchScanViewModel = viewModel()
    val historyVm: HistoryViewModel = viewModel()
    val items by vm.results.collectAsState()
    val active by vm.isActive.collectAsState()
    val context = LocalContext.current
    val contentCounts = items.groupingBy { it.content }.eachCount()
    val duplicateIds = contentCounts.filterValues { it > 1 }.keys
    val unique = contentCounts.size
    val duplicates = items.size - unique

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) vm.startBatch()
    }

    AppBackground()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Batch Scan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Info / sort removed — no implementation yet.
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecondaryButton(
                        text = "Discard",
                        onClick = { vm.clearAll() },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Save All (${items.size})",
                        onClick = {
                            items.forEach { item ->
                                historyVm.addScanResult(
                                    com.dhanuk.quickscanpro.database.ScanResult(
                                        content = item.content,
                                        type = item.type,
                                        timestamp = item.timestamp
                                    )
                                )
                            }
                            vm.clearAll()
                            Toast.makeText(context, "${items.size} saved to history", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = items.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save All (${items.size})")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            if (active && hasCameraPermission) {
                item {
                    BatchCameraPreview(
                        active = active,
                        onScan = { content ->
                            if (vm.addResult(content)) {
                                Toast.makeText(context, "Scanned: $content", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            item {
                SessionSummaryCard(
                    scanned = items.size,
                    duplicates = duplicates,
                    unique = unique
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (active) {
                        PillButton(
                            text = "Pause Batch",
                            icon = Icons.Filled.Pause,
                            onClick = { vm.stopBatch() }
                        )
                    } else {
                        PillButton(
                            text = "Start New Batch",
                            icon = Icons.Filled.PlayArrow,
                            onClick = {
                                if (hasCameraPermission) {
                                    vm.startBatch()
                                } else {
                                    permLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        )
                    }
                }
            }
            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "How it works",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            StepRow(1, "Open camera")
                            Spacer(Modifier.height(4.dp))
                            StepRow(2, "Scan continuously")
                            Spacer(Modifier.height(4.dp))
                            StepRow(3, "Auto-save each result")
                        }
                    }
                }
            } else {
                item {
                    Column {
                        Text(
                            "Current Batch  ·  ${items.size} codes",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                itemsIndexed(items) { idx, item ->
                    BatchRow(idx, item.content, item.type, isDuplicate = item.content in duplicateIds)
                }
            }
        }
    }
}

@Composable
private fun BatchCameraPreview(
    active: Boolean,
    onScan: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val analyzer = remember(onScan) { BarcodeAnalyzer(onScan) }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner, active) {
        if (!active) {
            return@DisposableEffect onDispose {}
        }
        val future = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            try {
                val provider = future.get()
                provider.unbindAll()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }
                provider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            } catch (e: Exception) {
                Log.e("BatchCamera", "Camera bind failed", e)
            }
        }
        future.addListener(listener, executor)
        onDispose {
            try {
                future.get().unbindAll()
            } catch (_: Exception) {}
            analyzer.close()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Text(
                text = "LIVE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SessionSummaryCard(
    scanned: Int,
    duplicates: Int,
    unique: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Session Summary",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCell(
                    value = scanned.toString(),
                    label = "Scanned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCell(
                    value = duplicates.toString(),
                    label = "Duplicates",
                    dot = true,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatCell(
                    value = unique.toString(),
                    label = "Unique",
                    dot = true,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCell(
    value: String,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    dot: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dot) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(tint)
                    )
                    Spacer(Modifier.size(4.dp))
                }
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tint
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StepRow(num: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                num.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BatchRow(
    index: Int,
    content: String,
    type: String,
    isDuplicate: Boolean
) {
    val isLink = type.equals("url", ignoreCase = true) || type.equals("text", ignoreCase = true)
    val icon = when {
        type.equals("wifi", ignoreCase = true) -> Icons.Filled.Wifi
        type.equals("vcard", ignoreCase = true) -> Icons.Filled.Person
        else -> Icons.Filled.Link
    }
    val iconBg = if (isDuplicate) MaterialTheme.colorScheme.surfaceContainerHigh
                 else MaterialTheme.colorScheme.primaryContainer
    val iconTint = if (isDuplicate) MaterialTheme.colorScheme.onSurfaceVariant
                   else MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    content,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isDuplicate) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "Duplicate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}
