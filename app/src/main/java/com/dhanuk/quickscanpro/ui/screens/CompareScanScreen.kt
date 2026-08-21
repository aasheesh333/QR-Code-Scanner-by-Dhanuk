package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dhanuk.quickscanpro.ui.design.CameraPreviewBox
import com.dhanuk.quickscanpro.ui.design.previewHeight
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.ui.design.SectionLabel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CompareScanScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var left by remember { mutableStateOf<String?>(null) }
    var right by remember { mutableStateOf<String?>(null) }
    var scanningLeft by remember { mutableStateOf(true) }

    val hasPerm = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            Toast.makeText(context, "Camera permission needed to compare", Toast.LENGTH_SHORT).show()
        }
    }
    if (!hasPerm) {
        androidx.compose.runtime.LaunchedEffect(Unit) { permLauncher.launch(Manifest.permission.CAMERA) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Compare Codes", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))

            Column {
                SectionLabel(if (scanningLeft) "Scanning code A" else "Scanning code B")
                CameraPreviewBox(
                    onScan = { result ->
                        if (scanningLeft) {
                            left = result
                            scanningLeft = false
                        } else {
                            right = result
                        }
                    },
                    onCameraReady = {},
                    modifier = Modifier.fillMaxWidth().height(previewHeight(0.32f, 200.dp, 300.dp))
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                CodeSlot("Code A", left, Modifier.weight(1f))
                CodeSlot("Code B", right, Modifier.weight(1f))
            }

            val a = left
            val b = right
            if (a != null && b != null) {
                val same = a == b
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconBadge(Icons.Filled.CompareArrows)
                    Text(
                        if (same) "Result: identical content" else "Result: different content",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (same) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QsOutlinedButton(
                        text = "Copy Code A",
                        modifier = Modifier.weight(1f),
                        onClick = { copy(context, a) }
                    )
                    QsOutlinedButton(
                        text = "Compare again",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            left = null
                            right = null
                            scanningLeft = true
                        }
                    )
                }
            } else {
                QsButton(
                    text = if (a == null) "Scan code A first" else "Now scan code B",
                    onClick = {}
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CodeSlot(label: String, value: String?, modifier: Modifier = Modifier) {
    QsCard(modifier = modifier, contentPadding = 14.dp) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(
            value ?: "Waiting…",
            style = MaterialTheme.typography.bodyMedium,
            color = if (value == null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            maxLines = 3
        )
    }
}

private fun copy(context: Context, text: String) {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        .setPrimaryClip(ClipData.newPlainText("compare", text))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}
