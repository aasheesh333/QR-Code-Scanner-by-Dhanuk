package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.dhanuk.quickscanpro.ads.InterstitialAdManager
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.ui.composables.GlassCard
import com.dhanuk.quickscanpro.ui.composables.GlowOrb
import com.dhanuk.quickscanpro.ui.composables.ScanOverlay
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimary
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimaryGlow
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScan: (String) -> Unit,
    onBatchScan: () -> Unit,
    onViewAllHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val svm: SettingsViewModel = viewModel()
    val hvm: HistoryViewModel = viewModel()
    val dark = isSystemInDarkTheme()

    val vibrateEnabled by svm.vibrateEnabled.collectAsState()
    val soundEnabled by svm.soundEnabled.collectAsState()
    val incognitoMode by svm.incognitoMode.collectAsState()
    val recentHistory by hvm.history.collectAsState()

    var isTorchOn by rememberSaveable { mutableStateOf(false) }
    var scanned by remember { mutableStateOf(false) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var cameraRetryKey by remember { mutableStateOf(0) }

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
    LaunchedEffect(key1 = Unit) {
        if (!hasCamPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    val scope = rememberCoroutineScope()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    val found = scanImage(context, it)
                    if (found != null) {
                        InterstitialAdManager.recordScan(context)
                        onScan(found)
                    } else {
                        android.widget.Toast.makeText(
                            context, "No QR code found in image", android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient glow behind the viewfinder
        GlowOrb(
            modifier = Modifier
                .size(420.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp),
            color = if (dark) LuminaPrimaryGlow else LuminaPrimary
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Scanner",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (incognitoMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.VisibilityOff, contentDescription = null,
                                tint = LuminaPrimaryGlow, modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Incognito — scans won't be saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = LuminaPrimaryGlow
                            )
                        }
                    }
                }
                GlassIconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery")
                }
            }

            Spacer(Modifier.height(24.dp))

            if (hasCamPermission) {
                // ---- Glass viewfinder ----
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 32.dp,
                        glowColor = LuminaPrimary
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (cameraError == null) {
                                key(cameraRetryKey) {
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
                                            val analyzer = BarcodeAnalyzer { result ->
                                                if (!scanned) {
                                                    scanned = true
                                                    if (vibrateEnabled) vibrate(ctx)
                                                    if (soundEnabled) playSound(ctx)
                                                    InterstitialAdManager.recordScan(ctx)
                                                    onScan(result)
                                                }
                                            }
                                            imageAnalysis.setAnalyzer(
                                                ContextCompat.getMainExecutor(ctx), analyzer
                                            )

                                            val future: ListenableFuture<ProcessCameraProvider> =
                                                ProcessCameraProvider.getInstance(ctx)
                                            future.addListener({
                                                try {
                                                    val provider = future.get()
                                                    provider.unbindAll()
                                                    val cam = provider.bindToLifecycle(
                                                        lifecycleOwner, selector, preview, imageAnalysis
                                                    )
                                                    cameraControl = cam.cameraControl
                                                } catch (e: Exception) {
                                                    cameraError = e.message ?: e.javaClass.simpleName
                                                }
                                            }, ContextCompat.getMainExecutor(ctx))
                                            previewView
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(32.dp))
                                    )
                                }
                                ScanOverlay(
                                    modifier = Modifier
                                        .fillMaxSize(0.82f)
                                        .align(Alignment.Center)
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Filled.VideocamOff, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text("Camera failed to start",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(12.dp))
                                    TextButton(onClick = {
                                        cameraError = null
                                        cameraRetryKey++
                                    }) { Text("Retry") }
                                }
                            }
                        }
                    }

                    // Floating torch button (left)
                    FloatingToolButton(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (-14).dp),
                        onClick = {
                            val newTorch = !isTorchOn
                            cameraControl?.enableTorch(newTorch)
                            isTorchOn = newTorch
                        }
                    ) {
                        Icon(
                            if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isTorchOn) LuminaPrimaryGlow
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    "Align QR code within the frame",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))

                // ---- Action cards ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.QrCodeScanner,
                        title = "Scan Next",
                        subtitle = "IMMEDIATE",
                        highlighted = true,
                        onClick = { scanned = false }
                    )
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Layers,
                        title = "Batch Scan",
                        subtitle = "MULTI-ENTRY",
                        highlighted = false,
                        onClick = onBatchScan
                    )
                }

                Spacer(Modifier.height(22.dp))

                // ---- Recent history preview ----
                if (recentHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "RECENT HISTORY",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onViewAllHistory) {
                            Text("View all", style = MaterialTheme.typography.labelLarge,
                                color = if (dark) LuminaPrimaryGlow else LuminaPrimary)
                        }
                    }
                    val latest = recentHistory.first()
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onScan(latest.content) },
                        cornerRadius = 20.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LuminaPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Link, contentDescription = null,
                                    tint = if (dark) LuminaPrimaryGlow else LuminaPrimary,
                                    modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    latest.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    formatTime(latest.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(90.dp))
            } else {
                // Permission prompt
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (dark) LuminaPrimaryGlow else LuminaPrimary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Camera permission required.\nGrant to start scanning.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Grant Permission") }
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    GlassCard(cornerRadius = 999.dp, modifier = Modifier.size(44.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
            ) { content() }
        }
    }
}

@Composable
private fun FloatingToolButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    GlassCard(cornerRadius = 999.dp, modifier = modifier.size(52.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) { content() }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val accent = if (dark) LuminaPrimaryGlow else LuminaPrimary
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = 24.dp,
        glowColor = if (highlighted) accent else null
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (highlighted) 0.25f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, contentDescription = null,
                    tint = if (highlighted) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = if (highlighted) accent else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = (if (highlighted) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                    .copy(alpha = 0.6f)
            )
        }
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val m = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        m.defaultVibrator
    } else @Suppress("DEPRECATION") {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else @Suppress("DEPRECATION") vibrator.vibrate(200)
}

private fun playSound(context: Context) {
    try {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, notification) ?: return
        ringtone.play()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try { ringtone.stop() } catch (_: Exception) {}
        }, 1500)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private suspend fun scanImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val scanner = BarcodeScanning.getClient()
        try {
            com.google.android.gms.tasks.Tasks.await(scanner.process(image))
                .firstOrNull()?.rawValue
        } finally {
            scanner.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
