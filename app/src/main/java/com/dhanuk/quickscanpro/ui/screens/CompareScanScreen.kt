package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.ui.composables.GlassCard
import com.dhanuk.quickscanpro.ui.composables.HexScanFrame
import com.dhanuk.quickscanpro.ui.theme.SafetyRisky
import com.dhanuk.quickscanpro.ui.theme.SafetySafe

/**
 * Compare scan screen — clean professional layout:
 *  - Two side-by-side slots
 *  - Inline camera preview
 *  - Result card showing identical/different + diff stats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScanScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val accent = MaterialTheme.colorScheme.primary

    var first by remember { mutableStateOf<String?>(null) }
    var second by remember { mutableStateOf<String?>(null) }
    var currentSlot by remember { mutableStateOf<Int?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableStateOf(0) }

    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )
    LaunchedEffect(Unit) {
        if (!hasCamPermission) launcher.launch(Manifest.permission.CAMERA)
        currentSlot = when {
            first == null -> 1
            second == null -> 2
            else -> null
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Compare", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Text("Scan two codes side-by-side",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Tap an empty slot, then scan the matching QR code.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompareSlot(
                    slot = 1,
                    content = first,
                    active = currentSlot == 1,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentSlot = 1
                        if (hasCamPermission.not()) launcher.launch(Manifest.permission.CAMERA)
                    }
                )
                Icon(Icons.Filled.CompareArrows, contentDescription = null,
                    tint = accent, modifier = Modifier.size(24.dp).align(Alignment.CenterVertically))
                CompareSlot(
                    slot = 2,
                    content = second,
                    active = currentSlot == 2,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentSlot = 2
                        if (hasCamPermission.not()) launcher.launch(Manifest.permission.CAMERA)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            if (first != null && second != null) {
                CompareResultCard(first!!, second!!)
            }

            if (currentSlot != null && hasCamPermission && cameraError == null) {
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)) {
                    key(retryKey, currentSlot) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val preview = Preview.Builder().build()
                                val selector = CameraSelector.Builder()
                                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                    .build()
                                preview.setSurfaceProvider(previewView.surfaceProvider)
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                val slot = currentSlot ?: 1
                                val analyzer = BarcodeAnalyzer { result ->
                                    when (slot) {
                                        1 -> {
                                            first = result
                                            currentSlot = if (second == null) 2 else null
                                        }
                                        2 -> {
                                            second = result
                                            currentSlot = null
                                        }
                                    }
                                }
                                imageAnalysis.setAnalyzer(
                                    ContextCompat.getMainExecutor(ctx), analyzer)
                                val future = ProcessCameraProvider.getInstance(ctx)
                                future.addListener({
                                    try {
                                        val provider = future.get()
                                        provider.unbindAll()
                                        provider.bindToLifecycle(
                                            lifecycleOwner, selector, preview, imageAnalysis)
                                    } catch (e: Exception) {
                                        cameraError = e.message
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    HexScanFrame(modifier = Modifier
                        .fillMaxSize(0.82f)
                        .align(Alignment.Center))
                }
            }

            if (first != null && second != null) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        first = null
                        second = null
                        currentSlot = 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Reset & Compare Again") }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CompareSlot(
    slot: Int,
    content: String?,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (active) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline
    GlassCard(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp)),
        cornerRadius = 16.dp
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
            contentAlignment = Alignment.Center) {
            if (content == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Add, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Slot $slot", color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold)
                    if (active) Text("Scanning…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null,
                        tint = SafetySafe, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(content, maxLines = 2,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("Slot $slot",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CompareResultCard(first: String, second: String) {
    val isExact = first == second
    val color = if (isExact) SafetySafe else SafetyRisky
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isExact) Icons.Filled.CheckCircle else Icons.Filled.Difference,
                    contentDescription = null, tint = color, modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (isExact) "Identical" else "Different",
                        style = MaterialTheme.typography.titleMedium,
                        color = color, fontWeight = FontWeight.Bold)
                    Text("Compare Mode result",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            CompareRow(label = "Content A", value = first)
            Spacer(Modifier.height(8.dp))
            CompareRow(label = "Content B", value = second)
            Spacer(Modifier.height(8.dp))
            CompareRow(label = "Length A", value = "${first.length} chars")
            CompareRow(label = "Length B", value = "${second.length} chars")
            if (!isExact && first.length == second.length) {
                var diffs = 0
                for (i in first.indices) {
                    if (i < second.length && first[i] != second[i]) diffs++
                }
                CompareRow(
                    label = "Diff characters",
                    value = if (diffs == 1) "1 difference" else "$diffs differences",
                    highlights = diffs > 0
                )
            }
        }
    }
}

@Composable
private fun CompareRow(label: String, value: String, highlights: Boolean = false) {
    Row(verticalAlignment = Alignment.Top) {
        Text(label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f))
        Text(value,
            style = MaterialTheme.typography.bodySmall,
            color = if (highlights) SafetyRisky else MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(0.6f))
    }
}
