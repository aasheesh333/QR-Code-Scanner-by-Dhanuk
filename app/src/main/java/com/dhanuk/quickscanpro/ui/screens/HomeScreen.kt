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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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

import com.dhanuk.quickscanpro.ui.composables.ScanOverlay
import com.dhanuk.quickscanpro.ui.theme.DhanukAccent
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onScan: (String) -> Unit, onBatchScan: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val svm: SettingsViewModel = viewModel()

    val vibrateEnabled by svm.vibrateEnabled.collectAsState()
    val soundEnabled by svm.soundEnabled.collectAsState()
    val incognitoMode by svm.incognitoMode.collectAsState()

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
        onResult = { uri: android.net.Uri? ->
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
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "QuickScan Pro")
                        if (incognitoMode) {
                            Spacer(Modifier.width(6.dp))
                            BadgedBox(badge = {
                                Badge(containerColor = Color(0xFF8B5CF6)) { Text("INC") }
                            }) {}
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onBatchScan) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Batch")
                    }
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCamPermission) {
                if (cameraError != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.VideocamOff, contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Camera failed to start",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = cameraError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            cameraError = null
                            cameraRetryKey++
                        }, shape = RoundedCornerShape(12.dp)) {
                            Text("Retry")
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                            ContextCompat.getMainExecutor(ctx),
                                            analyzer
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
                                    modifier = Modifier.fillMaxSize(),
                                    onRelease = { /* analyzer closed via DisposableEffect */ }
                                )
                            }
                            DisposableEffect(cameraRetryKey) {
                                onDispose {
                                    try {
                                        ProcessCameraProvider.getInstance(context).get().unbindAll()
                                    } catch (_: Exception) {}
                                }
                            }
                            ScanOverlay(
                                modifier = Modifier.fillMaxSize(0.8f).align(Alignment.Center),
                                color = DhanukAccent
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            val newTorch = !isTorchOn
                            cameraControl?.enableTorch(newTorch)
                            isTorchOn = newTorch
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        shape = CircleShape,
                        containerColor = if (isTorchOn) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isTorchOn) Color.White
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = if (incognitoMode) "Incognito — scans won't be saved"
                    else "Point camera at a QR code or barcode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (incognitoMode) Color(0xFF8B5CF6)
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                TextButton(
                    onClick = { scanned = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Scan Next")
                }

                Spacer(modifier = Modifier.weight(1f))
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Camera permission required.\nGrant to start scanning.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Permission")
                    }
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
