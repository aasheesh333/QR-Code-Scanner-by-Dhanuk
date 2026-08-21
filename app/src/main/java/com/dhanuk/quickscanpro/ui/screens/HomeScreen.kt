package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.ui.design.CameraPreviewBox
import com.dhanuk.quickscanpro.ui.design.previewHeight
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.IconBadgeRadius
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import com.dhanuk.quickscanpro.util.ScanFeedback
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

private const val TAG = "HomeScreen"

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
    onOpenGenerate: () -> Unit = {},
    onOpenTextScan: () -> Unit = {},
    onOpenWifiShare: () -> Unit = {}
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
    var hasRequested by rememberSaveable { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        showRationale = !granted &&
            (context as? android.app.Activity)?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !hasRequested) {
            hasRequested = true
            permLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val lastContent = remember { mutableStateOf<String?>(null) }
    val lastTime = remember { mutableLongStateOf(0L) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    val onScanWithFeedback: (String) -> Unit = { result ->
        val now = System.currentTimeMillis()
        if (lastContent.value != result || (now - lastTime.longValue) >= 2500L) {
            lastContent.value = result
            lastTime.longValue = now
            if (soundEnabled) ScanFeedback.playBeep()
            if (vibrateEnabled) {
                ScanFeedback.vibrate(context)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onScan(result)
            com.dhanuk.quickscanpro.ads.InterstitialAdManager.recordScan(context)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val decoded = withContext(Dispatchers.IO) { decodeQrFromUri(context, uri) }
                if (decoded != null) onScanWithFeedback(decoded)
                else Toast.makeText(context, "No QR code found in that image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("QuickScan Pro", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAnalytics) {
                        Icon(Icons.Filled.BarChart, contentDescription = "Insights", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Scanner viewport
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(previewHeight(0.30f, 220.dp, 320.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.inverseSurface)
                ) {
                    if (hasCameraPermission) {
                        CameraPreviewBox(
                            onScan = onScanWithFeedback,
                            onCameraReady = { camera = it },
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 0.dp
                        )
                        HintPill("Center the code inside the frame", Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp))
                    } else {
                        PermissionPanel(
                            showRationale = showRationale,
                            onRequest = { permLauncher.launch(Manifest.permission.CAMERA) },
                            onOpenSettings = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                    )
                                }
                            }
                        )
                    }
                }

                FlashToggle(
                    enabled = hasCameraPermission,
                    camera = camera,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                )
            }

            // Primary quick tools
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolTile(Icons.Filled.CameraAlt, "Batch", Modifier.weight(1f), onOpenBatch)
                ToolTile(Icons.Filled.CompareArrows, "Compare", Modifier.weight(1f), onOpenCompare)
                ToolTile(Icons.Filled.PhotoLibrary, "Gallery", Modifier.weight(1f)) {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                ToolTile(Icons.Filled.History, "History", Modifier.weight(1f), onViewAllHistory)
            }

            Column {
                SectionLabel("Smart tools")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ToolTile(Icons.Filled.TextFields, "OCR Scan", Modifier.weight(1f), onOpenTextScan)
                    ToolTile(Icons.Filled.Wifi, "Share Wi-Fi", Modifier.weight(1f), onOpenWifiShare)
                    ToolTile(Icons.Filled.Lock, "Vault", Modifier.weight(1f), onOpenVault)
                    ToolTile(Icons.Filled.QrCodeScanner, "Create", Modifier.weight(1f), onOpenGenerate)
                }
            }

            Column {
                SectionLabel("Explore")
                LazyRow(
                    contentPadding = PaddingValues(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        listOf(
                            ExploTile(Icons.Filled.Timeline, "Timeline", onOpenTimeline),
                            ExploTile(Icons.Filled.ViewModule, "Templates", onOpenTemplates),
                            ExploTile(Icons.Filled.Bolt, "Leak Check", onOpenLeakCheck),
                            ExploTile(Icons.Filled.BarChart, "Insights", onOpenAnalytics)
                        )
                    ) { tile ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .clickable(onClick = tile.onClick)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(tile.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(tile.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            QsCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Filled.Lightbulb)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Pro tip", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Good lighting helps scan faster. Every scanned link is safety-checked before it opens.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

private data class ExploTile(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
private fun ToolTile(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconBadge(icon)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun HintPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}

@Composable
private fun FlashToggle(enabled: Boolean, camera: Camera?, modifier: Modifier = Modifier) {
    var on by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(IconBadgeRadius)
            .background(Color.Black.copy(alpha = if (!enabled) 0.2f else if (on) 0.75f else 0.4f))
            .clickable(enabled = enabled) {
                on = !on
                camera?.cameraControl?.enableTorch(on)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (on) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
            contentDescription = if (on) "Turn flash off" else "Turn flash on",
            tint = if (on) Color(0xFFFFD75E) else Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PermissionPanel(showRationale: Boolean, onRequest: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(14.dp))
        Text(
            if (showRationale) "Camera access needed" else "Allow camera access",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Use your camera to scan QR codes and barcodes. Nothing is uploaded.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.72f)
        )
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onRequest)
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Text("Grant permission", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
        }
        if (!showRationale) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Open app settings",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.clickable(onClick = onOpenSettings)
            )
        }
    }
}

private suspend fun decodeQrFromUri(context: android.content.Context, uri: Uri): String? {
    return suspendCancellableCoroutine { cont ->
        try {
            val bitmap = decodeSampledBitmap(context, uri, 2048)
            if (bitmap == null) {
                cont.resume(null, null)
                return@suspendCancellableCoroutine
            }
            val scanner = BarcodeScanning.getClient()
            scanner.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { barcodes ->
                    scanner.close()
                    cont.resume(barcodes.firstOrNull()?.rawValue, null)
                }
                .addOnFailureListener { e ->
                    scanner.close()
                    Log.e(TAG, "decode failed", e)
                    cont.resume(null, null)
                }
        } catch (e: Exception) {
            Log.e(TAG, "decode failed", e)
            cont.resume(null, null)
        }
    }
}

/** Decodes a content Uri, sub-sampling so the longest edge stays under [maxEdge] px. */
private fun decodeSampledBitmap(context: android.content.Context, uri: Uri, maxEdge: Int): android.graphics.Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > maxEdge) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    } catch (e: Exception) {
        Log.e(TAG, "bitmap decode failed", e)
        null
    }
}
