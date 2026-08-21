package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dhanuk.quickscanpro.ui.design.CameraPreviewBox
import com.dhanuk.quickscanpro.ui.design.previewHeight
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CompareScanScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var left by remember { mutableStateOf<String?>(null) }
    var right by remember { mutableStateOf<String?>(null) }
    // null = idle, "A"/"B" = currently scanning for that code
    var scanningFor by remember { mutableStateOf<String?>(null) }
    var compared by remember { mutableStateOf(false) }

    val hasPerm = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    var permDenied by remember { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) permDenied = true
    }
    if (!hasPerm) {
        LaunchedEffect(Unit) { permLauncher.launch(Manifest.permission.CAMERA) }
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

            if (permDenied) {
                QsCard {
                    Text(
                        "Camera permission is needed to scan codes for comparison.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Active scanner — only when a scan button was pressed.
            val target = scanningFor
            if (target != null && hasPerm) {
                SectionLabel("Scanning Code $target — point the camera at the code")
                CameraPreviewBox(
                    onScan = { result ->
                        if (target == "A") left = result else right = result
                        scanningFor = null
                        compared = false
                    },
                    onCameraReady = {},
                    modifier = Modifier.fillMaxWidth().height(previewHeight(0.32f, 200.dp, 300.dp))
                )
                QsOutlinedButton(text = "Cancel scan", onClick = { scanningFor = null })
            }

            // Captured codes
            SectionLabel("Codes")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                CodeSlot("Code A", left, Modifier.weight(1f))
                CodeSlot("Code B", right, Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QsButton(
                    text = if (left == null) "Scan Code A" else "Re-scan A",
                    icon = Icons.Filled.QrCodeScanner,
                    modifier = Modifier.weight(1f),
                    onClick = { scanningFor = "A" }
                )
                QsButton(
                    text = if (right == null) "Scan Code B" else "Re-scan B",
                    icon = Icons.Filled.QrCodeScanner,
                    modifier = Modifier.weight(1f),
                    onClick = { scanningFor = "B" }
                )
            }

            val a = left
            val b = right
            if (a != null && b != null) {
                QsButton(
                    text = "Compare",
                    icon = Icons.Filled.CompareArrows,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { compared = true }
                )
            } else {
                Text(
                    "Scan both codes, then press Compare.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (compared && a != null && b != null) {
                CompareResultCard(a, b)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    QsOutlinedButton(
                        text = "Copy A",
                        modifier = Modifier.weight(1f),
                        onClick = { copy(context, a) }
                    )
                    QsOutlinedButton(
                        text = "Copy B",
                        modifier = Modifier.weight(1f),
                        onClick = { copy(context, b) }
                    )
                }
                QsOutlinedButton(
                    text = "Compare again",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        left = null
                        right = null
                        scanningFor = null
                        compared = false
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CompareResultCard(a: String, b: String) {
    val same = a == b
    val typeA = remember(a) { BarcodeTypeDetector.detectType(a) }
    val typeB = remember(b) { BarcodeTypeDetector.detectType(b) }
    // First differing character index (-1 when identical or prefix).
    val firstDiff = remember(a, b) {
        var i = 0
        while (i < a.length && i < b.length && a[i] == b[i]) i++
        i
    }

    val verdictColor = if (same) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    QsCard {
        // Verdict banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(verdictColor.copy(alpha = 0.10f))
                .border(1.dp, verdictColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(verdictColor.copy(alpha = 0.15f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (same) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = verdictColor
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (same) "Codes are identical" else "Codes are different",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = verdictColor
                )
                Text(
                    if (same) "Both codes contain the exact same content"
                    else "Content does not match — first difference at character ${firstDiff + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        CompareRow("Content type", "$typeA vs $typeB", typeA == typeB)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        CompareRow("Length", "${a.length} vs ${b.length} characters", a.length == b.length)
        if (!same && firstDiff < maxOf(a.length, b.length)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            CharDiffRow(a, b, firstDiff)
        }
    }
}

@Composable
private fun CompareRow(label: String, value: String, match: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (match) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun CharDiffRow(a: String, b: String, index: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("First difference", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            "char #${index + 1}: '${a.getOrNull(index) ?: '∅'}' vs '${b.getOrNull(index) ?: '∅'}'",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.error
        )
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
        Spacer(Modifier.height(8.dp))
        if (value != null) {
            Text(
                "Type: ${BarcodeTypeDetector.detectType(value)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun copy(context: Context, text: String) {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        .setPrimaryClip(ClipData.newPlainText("compare", text))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}
