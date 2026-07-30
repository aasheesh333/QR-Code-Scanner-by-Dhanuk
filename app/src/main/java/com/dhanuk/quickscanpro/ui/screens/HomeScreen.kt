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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.dhanuk.quickscanpro.ui.composables.HexScanFrame
import com.dhanuk.quickscanpro.ui.composables.ModePill
import com.dhanuk.quickscanpro.util.AutoOrganizer
import com.dhanuk.quickscanpro.util.VoiceSpeaker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScanMode { AUTO, BATCH, COMPARE }

/**
 * Home screen — clean professional layout:
 *  - Standard top app bar
 *  - Camera preview card with simple viewfinder
 *  - Mode chips, torch/gallery orbs
 *  - Primary SCAN action button
 *  - Recent scan peek row
 * All 15 unique features preserved.
 */
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
    LaunchedEffect(Unit) {
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

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("QuickScan Pro", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Text(
                            if (incognitoMode) "Incognito mode is on" else "Scan codes instantly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Mode selector chips
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
                        .aspectRatio(0.92f),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 20.dp
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
                                                            delay(1200)
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
                                            .clip(RoundedCornerShape(20.dp))
                                    )
                                }
                                HexScanFrame(
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
                                    OutlinedButton(onClick = {
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
                            .offset(x = (-6).dp),
                        onClick = {
                            val newTorch = !isTorchOn
                            cameraControl?.enableTorch(newTorch)
                            isTorchOn = newTorch
                        },
                        active = isTorchOn,
                        size = 46.dp,
                        icon = {
                            Icon(
                                if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = "Torch",
                                tint = if (isTorchOn) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                    ActionOrb(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 6.dp),
                        onClick = { galleryLauncher.launch("image/*") },
                        size = 46.dp,
                        icon = {
                            Icon(
                                Icons.Filled.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = if (scanMode == ScanMode.COMPARE) "Scan first code to compare"
                    else "Align QR code inside the frame",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Primary SCAN button — solid indigo
                Button(
                    onClick = {
                        when (scanMode) {
                            ScanMode.BATCH -> onBatchScan()
                            ScanMode.COMPARE -> onCompareScan()
                            else -> scanned = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null,
                        modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when (scanMode) {
                            ScanMode.BATCH -> "START BATCH SCAN"
                            ScanMode.COMPARE -> "COMPARE TWO QR CODES"
                            else -> "TAP TO SCAN"
                        },
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                // Hands-free toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        checked = handsFree,
                        onCheckedChange = { handsFree = it },
                        shape = RoundedCornerShape(50),
                        color = if (handsFree) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (handsFree) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (handsFree) Icons.Filled.RecordVoiceOver else Icons.Filled.Mic,
                                contentDescription = null,
                                tint = if (handsFree) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (handsFree) "Hands-free on" else "Hands-free off",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (handsFree) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Recent scan peek
                if (recentHistory.isNotEmpty()) {
                    val latest = recentHistory.first()
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (scanMode == ScanMode.COMPARE) onCompareScan()
                                else onScan(latest.content)
                            },
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
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
                            Icon(
                                Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    TextButton(onClick = onViewAllHistory) {
                        Text("View all history",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(100.dp))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
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
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Grant Permission") }
                }
            }
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
