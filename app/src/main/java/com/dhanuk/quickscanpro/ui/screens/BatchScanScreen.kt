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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimaryGlow
import com.dhanuk.quickscanpro.viewmodel.BatchScanViewModel
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
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
                        Text("Batch Scan", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "${results.size} items scanned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
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
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (isActive) vm.stopBatch() else vm.startBatch()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Filled.Stop
                            else Icons.Filled.PlayArrow,
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (hasCamPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    GlassCard(modifier = Modifier.fillMaxSize(), cornerRadius = 24.dp,
                        glowColor = if (isActive) LuminaPrimaryGlow else null) {
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(Color.Black)) {
                    // Recreate the camera view when isActive changes so the camera
                    // unbinds (stops) when batch scanning is stopped.
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
                                                    Toast.makeText(
                                                        ctx,
                                                        "Added: ${result.take(40)}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        ctx,
                                                        "Already scanned",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
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
                                    // Unbind camera while idle
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
                            .size(240.dp))
                    }
                    if (!isActive) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Tap \"Start Batch Scan\" to begin",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    if (isActive) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "LIVE",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    }
                    }
                }
            }

            HorizontalDivider()

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
                        Text(
                            text = "No items scanned yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Start scanning to build your list",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                Text(
                    "Export Batch Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${results.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
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
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showFull = !showFull },
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (showFull) 10 else 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (content.length > 80) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (showFull) "Show less" else "Show more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
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
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
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
            type = "application/pdf"
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
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            ))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export $extension"))

        Toast.makeText(
            context,
            "Exported $fileName - choose where to save",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
