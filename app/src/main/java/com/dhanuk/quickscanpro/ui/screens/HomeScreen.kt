package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.util.ScanFeedback
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel

private const val TAG = "HomeScreen"

@Composable
fun HomeScreen(
    onScan: (String) -> Unit,
    onViewAllHistory: () -> Unit,
    onOpenBatch: () -> Unit,
    onOpenCompare: () -> Unit = {},
    onOpenVault: () -> Unit = {},
    onOpenTimeline: () -> Unit = {},
    onOpenTemplates: () -> Unit,
    onOpenLeakCheck: () -> Unit,
    onOpenAnalytics: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenGenerate: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsVm: SettingsViewModel = viewModel()
    val soundEnabled by settingsVm.soundEnabled.collectAsState()
    val vibrateEnabled by settingsVm.vibrateEnabled.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var showRationale by rememberSaveable { mutableStateOf(false) }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        showRationale = !granted &&
            (context as? android.app.Activity)?.shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            ) == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !hasRequestedPermission) {
            hasRequestedPermission = true
            permLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val haptic = LocalHapticFeedback.current
    val lastScanContent = remember { mutableStateOf<String?>(null) }
    val lastScanTime = remember { mutableLongStateOf(0L) }

    val onScanWithFeedback: (String) -> Unit = { result ->
        val now = System.currentTimeMillis()
        val isDup = lastScanContent.value == result && (now - lastScanTime.value) < 2500L
        if (!isDup) {
            lastScanContent.value = result
            lastScanTime.longValue = now
            if (soundEnabled) ScanFeedback.playBeep()
            if (vibrateEnabled) {
                ScanFeedback.vibrate(context)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onScan(result)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top app bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "QuickScan Pro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Camera viewfinder card with floating scan pill ──
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.inverseSurface)
                ) {
                    if (hasCameraPermission) {
                        CameraPreview(
                            onScan = onScanWithFeedback,
                            modifier = Modifier.fillMaxSize()
                        )
                        ScanFrameOverlay(Modifier.fillMaxSize())
                        // Center hint pill
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Center QR code in frame",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    } else {
                        PermissionPlaceholder(
                            showRationale = showRationale,
                            onRequestPermission = { permLauncher.launch(Manifest.permission.CAMERA) },
                            onOpenAppSettings = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                // Floating "Tap to Scan" pill overlapping bottom of viewfinder
                Button(
                    onClick = { if (!hasCameraPermission) permLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 28.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Filled.CenterFocusStrong, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tap to Scan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(56.dp))

            // ── Quick actions row: Flash / Gallery / History ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickAction(icon = Icons.Filled.FlashOn, label = "Flash", onClick = onOpenBatch)
                QuickAction(icon = Icons.Filled.Image, label = "Gallery", onClick = onOpenTemplates)
                QuickAction(icon = Icons.Filled.History, label = "History", onClick = onViewAllHistory)
            }

            Spacer(Modifier.height(28.dp))

            // ── Pro Tip card ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "Pro Tip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Ensure enough light is on the code for faster scanning.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CameraPreview(
    onScan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val analyzer = remember(onScan) { BarcodeAnalyzer(onScan) }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
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

    AndroidView(factory = { previewView }, modifier = modifier)
}

@Composable
private fun ScanFrameOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scanline")
    val lineProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanline"
    )

    Box(modifier = modifier.padding(40.dp)) {
        // Corner brackets
        val bracketColor = MaterialTheme.colorScheme.primary
        val bracketLength = 40.dp
        val bracketStroke = 4.dp

        // Top-left
        Box(Modifier.align(Alignment.TopStart)) {
            Box(Modifier.width(bracketLength).height(bracketStroke).clip(RoundedCornerShape(2.dp)).background(bracketColor))
            Box(Modifier.width(bracketStroke).height(bracketLength).clip(RoundedCornerShape(2.dp)).background(bracketColor))
        }
        // Top-right
        Box(Modifier.align(Alignment.TopEnd)) {
            Box(Modifier.align(Alignment.TopEnd).width(bracketLength).height(bracketStroke).clip(RoundedCornerShape(2.dp)).background(bracketColor))
            Box(Modifier.align(Alignment.TopEnd).width(bracketStroke).height(bracketLength).clip(RoundedCornerShape(2.dp)).background(bracketColor))
        }
        // Bottom-left
        Box(Modifier.align(Alignment.BottomStart)) {
            Box(Modifier.align(Alignment.BottomStart).width(bracketLength).height(bracketStroke).clip(RoundedCornerShape(2.dp)).background(bracketColor))
            Box(Modifier.align(Alignment.BottomStart).width(bracketStroke).height(bracketLength).clip(RoundedCornerShape(2.dp)).background(bracketColor))
        }
        // Bottom-right
        Box(Modifier.align(Alignment.BottomEnd)) {
            Box(Modifier.align(Alignment.BottomEnd).width(bracketLength).height(bracketStroke).clip(RoundedCornerShape(2.dp)).background(bracketColor))
            Box(Modifier.align(Alignment.BottomEnd).width(bracketStroke).height(bracketLength).clip(RoundedCornerShape(2.dp)).background(bracketColor))
        }

        // Animated scan line
        BoxWithConstraintsScope(lineProgress)
    }
}

@Composable
private fun BoxWithConstraintsScope(progress: Float) {
    androidx.compose.foundation.layout.BoxWithConstraints {
        val travel = maxHeight - 8.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .offset(y = travel * progress)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
        )
    }
}

@Composable
private fun PermissionPlaceholder(
    showRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (showRationale) "Camera access needed" else "Grant camera permission",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "QuickScan needs your camera to scan QR codes and barcodes.",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Grant permission")
        }
        if (!showRationale) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onOpenAppSettings) {
                Text("Open app settings", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
