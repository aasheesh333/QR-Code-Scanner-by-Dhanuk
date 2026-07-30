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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.dhanuk.quickscanpro.ads.InterstitialAdManager
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.ui.composables.ActionOrb
import com.dhanuk.quickscanpro.ui.composables.GlassCard
import com.dhanuk.quickscanpro.ui.composables.GlowOrb
import com.dhanuk.quickscanpro.ui.composables.HexScanFrame
import com.dhanuk.quickscanpro.ui.composables.ModePill
import com.dhanuk.quickscanpro.ui.theme.*
import com.dhanuk.quickscanpro.util.AutoOrganizer
import com.dhanuk.quickscanpro.util.VoiceSpeaker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScanMode { AUTO, BATCH, COMPARE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScan: (String) -> Unit,
    onBatchScan: () -> Unit,
    onCompareScan: () -> Unit = {},
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

    var scanMode by rememberSaveable { mutableStateOf(ScanMode.AUTO) }
    var handsFree by rememberSaveable { mutableStateOf(false) }
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
        VoiceSpeaker.init(context)
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
        GlowOrb(
            modifier = Modifier
                .size(440.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp),
            color = if (dark) LuminaPrimaryGlow else LuminaPrimary
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(14.dp))

            // Top: brand pill bar with live indicator
            BrandPillBar(incognito = incognitoMode, handsFree = handsFree) {
                handsFree = !handsFree
            }
            Spacer(Modifier.height(10.dp))

            // Mode pills row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModePill("Auto", scanMode == ScanMode.AUTO) { scanMode = ScanMode.AUTO }
                ModePill("Batch", scanMode == ScanMode.BATCH) { scanMode = ScanMode.BATCH }
                ModePill("Compare", scanMode == ScanMode.COMPARE) { scanMode = ScanMode.COMPARE }
            }
            Spacer(Modifier.height(16.dp))

            if (hasCamPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.86f)
                        .padding(horizontal = 6.dp),
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
                                                if (!scanned || handsFree) {
                                                    scanned = true
                                                    if (vibrateEnabled) vibrate(ctx)
                                                    if (soundEnabled) playSound(ctx)
                                                    VoiceSpeaker.init(ctx)
                                                    InterstitialAdManager.recordScan(ctx)
                                                    if (handsFree) VoiceSpeaker.speak(result)
                                                    onScan(result)
                                                    if (handsFree) {
                                                        scope.launch {
                                                            kotlinx.coroutines.delay(1200)
                                                            scanned = false
                                                        }
                                                    }
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
                                HexScanFrame(
                                    modifier = Modifier
                                        .fillMaxSize(0.84f)
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

                    ActionOrb(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (-8).dp),
                        onClick = {
                            val newTorch = !isTorchOn
                            cameraControl?.enableTorch(newTorch)
                            isTorchOn = newTorch
                        },
                        active = isTorchOn,
                        size = 48.dp,
                        icon = {
                            Icon(
                                if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = "Torch",
                                tint = if (isTorchOn) LuminaPrimaryGlow
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                    ActionOrb(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 8.dp),
                        onClick = { galleryLauncher.launch("image/*") },
                        size = 48.dp,
                        icon = {
                            Icon(
                                Icons.Filled.PhotoLibrary, contentDescription = "Gallery",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                }

                Spacer(Modifier.height(18.dp))

                if (scanMode == ScanMode.COMPARE) {
                    PulseLabel("Scan first of two codes to compare")
                } else {
                    PulseLabel("Align QR code within the frame")
                }
                Spacer(Modifier.height(18.dp))

                // Big round SCAN pulsing button
                ScanButton(
                    pulseAlways = !scanned || handsFree,
                    onClick = {
                        when (scanMode) {
                            ScanMode.BATCH -> onBatchScan()
                            ScanMode.COMPARE -> onCompareScan()
                            else -> scanned = false
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                // Recent scan peek card
                if (recentHistory.isNotEmpty()) {
                    val latest = recentHistory.first()
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (scanMode == ScanMode.COMPARE) onCompareScan()
                                else onScan(latest.content)
                            },
                        cornerRadius = 20.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LuminaPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    AutoOrganizer.emojiFor(latest.autoCategory),
                                    fontSize = 18.sp
                                )
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
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null,
                                tint = if (dark) LuminaPrimaryGlow else LuminaPrimary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onViewAllHistory) {
                        Text("View All History",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (dark) LuminaPrimaryGlow else LuminaPrimary)
                    }
                }
                Spacer(Modifier.height(90.dp))
            } else {
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
private fun BrandPillBar(incognito: Boolean, handsFree: Boolean, onHandsFree: () -> Unit) {
    val dark = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "live")
    val dotAlpha by transition.animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900), repeatMode = RepeatMode.Reverse
        ), label = "dot"
    )
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50)),
        cornerRadius = 50.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (handsFree) LuminaSuccess else LuminaPrimaryGlow.copy(alpha = dotAlpha)
                    )
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "QuickScan Pro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (dark) LuminaPrimaryGlow else LuminaPrimary,
                modifier = Modifier.weight(1f)
            )
            if (incognito) {
                Icon(
                    Icons.Filled.VisibilityOff, contentDescription = null,
                    tint = LuminaPrimaryGlow, modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                if (handsFree) Icons.Filled.RecordVoiceOver else Icons.Filled.Mic,
                contentDescription = "Hands-free",
                tint = if (handsFree) LuminaSuccess
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onHandsFree)
                    .padding(2.dp)
            )
        }
    }
}

@Composable
private fun ScanButton(pulseAlways: Boolean, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "scan_btn")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = if (pulseAlways) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100), repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    Box(
        modifier = Modifier
            .size((82 * scale).dp)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = LuminaPrimary.copy(alpha = 0.5f),
                spotColor = LuminaPrimary.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(LuminaPrimary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan",
                tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(2.dp))
            Text("SCAN",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun PulseLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
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
