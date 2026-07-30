package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.ui.composables.GlassCard
import com.dhanuk.quickscanpro.ui.composables.ScanOverlay
import com.dhanuk.quickscanpro.viewmodel.BatchScanViewModel
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Batch scan screen — clean professional layout:
 *  - Top camera preview card with simple viewfinder
 *  - Start/Stop button
 *  - Scanned items list below with remove action
 *  - Export bottom sheet (PDF, CSV, JSON, TXT, Clipboard)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScanScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val vm: BatchScanViewModel = viewModel()
    val results by vm.results.collectAsState()
    val isActive by vm.isActive.collectAsState()

    var lastScanned by remember { mutableStateOf("") }
    var showExportSheet by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamPermission = granted }

    LaunchedEffect(Unit) { if (!hasCamPermission) launcher.launch(Manifest.permission.CAMERA) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Batch Scan", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Text("${results.size} items scanned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    if (results.isNotEmpty()) {
                        IconButton(onClick = { showExportSheet = true }) {
                            Icon(Icons.Filled.IosShare, contentDescription = "Export")
                        }
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear all")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { if (isActive) vm.stopBatch() else vm.startBatch() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isActive) "Stop Scanning" else "Start Batch Scan",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (hasCamPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(vertical = 12.dp)
                ) {
                    GlassCard(modifier = Modifier.fillMaxSize(), cornerRadius = 20.dp) {
                        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(Color.Black)) {
                            key(isActive) {
                                AndroidView(
                                    factory = { ctx ->
                                        val previewView = PreviewView(ctx)
                                        if (isActive) {
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }
                                            val selector = CameraSelector.DEFAULT_BACK_CAMERA
                                            val imageAnalysis = ImageAnalysis.Builder()
                                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                .build()

                                            imageAnalysis.setAnalyzer(
                                                ContextCompat.getMainExecutor(ctx),
                                                BarcodeAnalyzer { result ->
                                                    if (result != lastScanned) {
                                                        lastScanned = result
                                                        if (vm.addResult(result)) {
                                                            Toast.makeText(ctx,
                                                                "Added: ${result.take(40)}",
                                                                Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(ctx,
                                                                "Already scanned", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            )

                                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                            cameraProviderFuture.addListener({
                                                val cameraProvider = cameraProviderFuture.get()
                                                try {
                                                    cameraProvider.unbindAll()
                                                    cameraProvider.bindToLifecycle(
                                                        lifecycleOwner, selector, preview, imageAnalysis
                                                    )
                                                } catch (e: Exception) {
                                                    Toast.makeText(ctx,
                                                        "Camera binding failed: ${e.message}",
                                                        Toast.LENGTH_LONG).show()
                                                }
                                            }, ContextCompat.getMainExecutor(ctx))
                                        } else {
                                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                            cameraProviderFuture.addListener({
                                                try { cameraProviderFuture.get().unbindAll() } catch (_: Exception) {}
                                            }, ContextCompat.getMainExecutor(ctx))
                                        }
                                        previewView
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (isActive) {
                                ScanOverlay(modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(220.dp))
                            }
                            if (!isActive) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 18.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        "Tap \"Start Batch Scan\" to begin",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.error)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "LIVE",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No items scanned yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        Text("Start scanning to build your list",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(results, key = { _, item -> "${item.content}_${item.timestamp}" }) { index, item ->
                        BatchScanItemCard(
                            index = index,
                            content = item.content,
                            type = item.type,
                            onRemove = { vm.removeAt(index) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showExportSheet) {
        ModalBottomSheet(onDismissRequest = { showExportSheet = false }) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Export Batch Results",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("${results.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(20.dp))

                ExportOptionRow(
                    icon = Icons.Filled.PictureAsPdf,
                    title = "Export as PDF",
                    subtitle = "Printable document format"
                ) {
                    showExportSheet = false
                    exportPdf(context, vm)
                }
                ExportOptionRow(
                    icon = Icons.Filled.GridView,
                    title = "Export as CSV",
                    subtitle = "Spreadsheet compatible"
                ) {
                    showExportSheet = false
                    exportToFile(context, vm.exportAsCsv(), "batch_scan", "csv")
                }
                ExportOptionRow(
                    icon = Icons.Filled.Code,
                    title = "Export as JSON",
                    subtitle = "Structured data format"
                ) {
                    showExportSheet = false
                    exportToFile(context, vm.exportAsJson(), "batch_scan", "json")
                }
                ExportOptionRow(
                    icon = Icons.Filled.TextSnippet,
                    title = "Export as TXT",
                    subtitle = "Plain text file"
                ) {
                    showExportSheet = false
                    exportToFile(context, vm.exportAsText(), "batch_scan", "txt")
                }
                ExportOptionRow(
                    icon = Icons.Filled.ContentCopy,
                    title = "Copy to Clipboard",
                    subtitle = "Copy all items as text"
                ) {
                    showExportSheet = false
                    val txt = vm.exportAsText()
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText("Batch Scan", txt))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all items?") },
            text = { Text("This will remove all ${results.size} scanned items from the list.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.clearAll()
                        showClearDialog = false
                        Toast.makeText(context, "Cleared all items", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BatchScanItemCard(
    index: Int,
    content: String,
    type: String,
    onRemove: () -> Unit
) {
    var showFull by remember { mutableStateOf(false) }
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (showFull) 10 else 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ExportOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

private fun exportPdf(context: Context, vm: BatchScanViewModel) {
    try {
        val file = com.dhanuk.quickscanpro.util.PdfExporter.writePdf(
            context,
            com.dhanuk.quickscanpro.util.PdfExporter.timestampedName("batch_scan"),
            vm.getPdfLines()
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            setType("application/pdf")
            putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            ))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export PDF"))
        Toast.makeText(context, "Exported ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "PDF export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun exportToFile(
    context: Context,
    content: String,
    baseName: String,
    extension: String
) {
    try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${baseName}_$timestamp.$extension"
        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "QuickScanPro")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)
        FileWriter(file).use { it.write(content) }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            setType("*/*")
            putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            ))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export $extension"))
        Toast.makeText(context, "Exported $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
