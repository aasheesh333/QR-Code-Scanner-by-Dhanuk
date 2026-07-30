package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.CameraIconButton
import com.dhanuk.quickscanpro.ui.composables.EmptyState
import com.dhanuk.quickscanpro.ui.composables.ScanButton
import com.dhanuk.quickscanpro.ui.composables.ScanFrame
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onScan: (String) -> Unit,
    onViewAllHistory: () -> Unit,
    onOpenBatch: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenLeakCheck: () -> Unit
) {
    val context = LocalContext.current
    val historyVm: HistoryViewModel = viewModel()
    val recent by historyVm.history.collectAsState()
    val recent1 = recent.firstOrNull()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permLauncher.launch(Manifest.permission.CAMERA)
    }

    AppBackground()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopHeaderRow(
            onSettings = onViewAllHistory,
            settingsIcon = Icons.Filled.History
        )

        CameraCard(
            hasCameraPermission = hasCameraPermission,
            onRequestPermission = { permLauncher.launch(Manifest.permission.CAMERA) },
            onScan = onScan,
            onBatch = onOpenBatch
        )

        QuickToolsRow(
            onGenerate = onViewAllHistory,
            onHistory = onViewAllHistory,
            onTemplates = onOpenTemplates,
            onLeak = onOpenLeakCheck
        )

        RecentScanRow(recent1, onViewAll = onViewAllHistory)
    }
}

@Composable
private fun TopHeaderRow(
    onSettings: () -> Unit,
    settingsIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 14.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "QuickScan Pro",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Point at any QR or barcode",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp).clickable(onClick = onSettings)
        ) {
            Icon(
                settingsIcon,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun CameraCard(
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onScan: (String) -> Unit,
    onBatch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = ContextCompat.getMainExecutor(ctx)
                    ProcessCameraProvider.getInstance(ctx).also { future ->
                        future.addListener({
                            val provider = future.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(executor, BarcodeAnalyzer(onScan))
                                }
                            try {
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    ctx as androidx.lifecycle.LifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview, analysis
                                )
                            } catch (_: Exception) {}
                        }, executor)
                    }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onRequestPermission)
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Filled.Security,
                            contentDescription = "Camera",
                            tint = Color.Black,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Grant camera permission", color = Color.White)
                }
            }
        }

        ScanFrame(modifier = Modifier.fillMaxSize().padding(28.dp))

        // Floating icons above camera — evenly spaced via SpaceBetween
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CameraIconButton(
                icon = Icons.Filled.FlashOn,
                onClick = {},
                contentDescription = "Flash"
            )
            CameraIconButton(
                icon = Icons.Filled.AutoAwesomeMotion,
                onClick = onBatch,
                contentDescription = "Batch scan"
            )
        }

        // Big white Scan Now button at bottom of camera card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ScanButton(onClick = {}, label = "Scan Now")
        }
    }
}

@Composable
private fun QuickToolsRow(
    onGenerate: () -> Unit,
    onHistory: () -> Unit,
    onTemplates: () -> Unit,
    onLeak: () -> Unit
) {
    val tiles = listOf(
        Triple("Generate QR", Icons.Filled.QrCode, onGenerate),
        Triple("History", Icons.Filled.History, onHistory),
        Triple("Templates", Icons.Filled.ViewAgenda, onTemplates),
        Triple("Leak Check", Icons.Filled.Security, onLeak)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        tiles.forEach { (title, icon, onClick) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onClick)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 16.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun RecentScanRow(scan: ScanResult?, onViewAll: () -> Unit) {
    if (scan == null) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            EmptyState(
                icon = Icons.Filled.History,
                title = "No scans yet",
                subtitle = "Point your camera at a QR code to begin"
            )
        }
        return
    }
    val ts = remember(scan.id) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(scan.timestamp))
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onViewAll)
            .clip(RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Last scan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = scan.content.take(50),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${scan.type.uppercase()} · $ts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "View all →",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
