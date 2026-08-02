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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CompareScanScreen(onNavigateBack: () -> Unit) {
    val slotAState = remember { mutableStateOf<String?>(null) }
    val slotBState = remember { mutableStateOf<String?>(null) }
    val scanningState = remember { mutableStateOf<Int?>(null) }
    val slotA = slotAState.value
    val slotB = slotBState.value
    val scanning = scanningState.value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Compare Codes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ScanSlot("Slot A", slotA, scanning == 1, { scanningState.value = 1 }, { scanned ->
                    slotAState.value = scanned
                    scanningState.value = null
                }, Modifier.weight(1f))
                ScanSlot("Slot B", slotB, scanning == 2, { scanningState.value = 2 }, { scanned ->
                    slotBState.value = scanned
                    scanningState.value = null
                }, Modifier.weight(1f))
            }
            PrimaryButton(text = "Scan to Compare", onClick = { scanningState.value = 1 }, modifier = Modifier.fillMaxWidth()) { Spacer(Modifier.width(8.dp)); Text("Scan to Compare") }
            if (slotA != null && slotB != null) {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow).padding(16.dp)) { Text("COMPARISON", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600), color = MaterialTheme.colorScheme.onSurface) }
                        ComparisonRow("Scan A", slotA!!, slotB!!)
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 16.dp, end = 16.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))
                        ComparisonRow("Scan B", slotB!!, slotA!!)
                    }
                }
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(20.dp)) }
                        Column { Text("Recommendation", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = MaterialTheme.colorScheme.onSurface); Spacer(Modifier.height(4.dp)); Text("Compare the two scanned codes to verify their contents match or differ.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanSlot(
    label: String,
    value: String?,
    scanning: Boolean,
    onStartScan: () -> Unit,
    onScan: (String) -> Unit,
    modifier: Modifier
) {
    val accentColor = if (label == "Slot A") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(accentColor))
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(4.dp), color = if (label == "Slot A") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest) {
                    Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600), color = if (label == "Slot A") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
                if (value != null) {
                    Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600), color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
                } else {
                    if (scanning) {
                        ScanSlotCamera(onScan)
                    } else {
                        Spacer(Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanSlotCamera(onScan: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val analyzer = remember(onScan) { BarcodeAnalyzer(onScan) }
    val previewView = remember { PreviewView(context) }
    val hasPermState = remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val hasPerm = hasPermState.value
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermState.value = it }

    DisposableEffect(lifecycleOwner, hasPerm) {
        if (!hasPerm) return@DisposableEffect onDispose {}
        val future = ProcessCameraProvider.getInstance(context)
        val listener = Runnable { try { val p = future.get(); p.unbindAll(); val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }; val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(executor, analyzer) }; p.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis) } catch (_: Exception) {} }
        future.addListener(listener, executor)
        onDispose { try { future.get().unbindAll() } catch (_: Exception) {}; analyzer.close() }
    }

    if (hasPerm) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black)) { AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) }
    } else {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
            Text("Camera permission required", color = MaterialTheme.colorScheme.onSurface)
        }
    }
    androidx.compose.material3.Button(onClick = { if (!hasPerm) permLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)) { Text("Enable Camera") }
}

@Composable
private fun ComparisonRow(label: String, valA: String, valB: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(valA.take(20), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1); Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(16.dp)); Text(valB.take(20), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}
