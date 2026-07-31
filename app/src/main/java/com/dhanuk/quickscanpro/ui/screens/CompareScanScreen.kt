package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.ui.composables.ScanFrame

private const val TAG = "CompareScanScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScanScreen(onNavigateBack: () -> Unit) {
    var scanA by rememberSaveable { mutableStateOf<String?>(null) }
    var scanB by rememberSaveable { mutableStateOf<String?>(null) }
    val isMatch = !scanA.isNullOrEmpty() && !scanB.isNullOrEmpty() && scanA == scanB
    var isScanning by rememberSaveable { mutableStateOf(false) }
    var activeSlot by rememberSaveable { mutableStateOf<Int?>(null) }

    // While the modal camera sheet is open, system back should dismiss the sheet
    // (preserving both scans) instead of leaving the whole screen.
    BackHandler(enabled = isScanning) {
        isScanning = false
        activeSlot = null
    }

    AppBackground()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Compare Scans", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val tmp = scanA
                        scanA = scanB
                        scanB = tmp
                    }) {
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = "Swap")
                    }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    PrimaryButton(
                        text = "Scan to Compare",
                        onClick = {
                            activeSlot = if (scanA == null) 0 else 1
                            isScanning = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "Scan two codes back-to-back to check if they're identical.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ScanSlot(
                        label = "Scan A",
                        value = scanA,
                        modifier = Modifier.weight(1f)
                    ) {
                        activeSlot = 0
                        isScanning = true
                    }
                    ScanSlot(
                        label = "Scan B",
                        value = scanB,
                        modifier = Modifier.weight(1f)
                    ) {
                        activeSlot = 1
                        isScanning = true
                    }
                }
                if (scanA != null && scanB != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isMatch)
                            MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                if (isMatch) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isMatch)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = if (isMatch) "MATCH — codes identical"
                                       else "DIFFERENT — codes don't match",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isMatch)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            if (isScanning && activeSlot != null) {
                CompareCameraSheet(
                    onScan = { result ->
                        when (activeSlot) {
                            0 -> scanA = result
                            1 -> scanB = result
                        }
                        isScanning = false
                        activeSlot = null
                    },
                    onClose = {
                        isScanning = false
                        activeSlot = null
                    }
                )
            }
        }
    }
}

@Composable
private fun CompareCameraSheet(
    onScan: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val analyzer = remember(onScan) {
        BarcodeAnalyzer { result ->
            if (result.isNotEmpty()) onScan(result)
        }
    }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner, hasPermission) {
        if (!hasPermission) {
            return@DisposableEffect onDispose {}
        }
        val future = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val provider = try { future.get() } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera provider", e)
                return@Runnable
            }
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview, analysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera lifecycle", e)
            }
        }
        future.addListener(listener, executor)

        onDispose {
            try {
                analyzer.close()
                val provider = try { future.get() } catch (_: Exception) { null }
                provider?.unbindAll()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unbind camera", e)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasPermission) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 48.dp, vertical = 24.dp)
                ) {
                    ScanFrame(modifier = Modifier.fillMaxWidth())
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Camera permission required",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.4f)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ScanSlot(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    onScan: () -> Unit
) {
    val borderColor = if (value != null)
        MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier = modifier
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onScan
            ),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value ?: "Tap to scan",
                style = MaterialTheme.typography.bodySmall,
                color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
