package com.dhanuk.quickscanpro.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.ui.composables.SecondaryButton
import com.dhanuk.quickscanpro.ui.theme.SafetySafe
import com.dhanuk.quickscanpro.ui.theme.SafetyWarn
import com.dhanuk.quickscanpro.ui.theme.SafetyRisky
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.LinkSafetyChecker
import com.dhanuk.quickscanpro.util.PasswordLeakChecker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    data: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val historyVm: HistoryViewModel = viewModel()

    var savedId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(data) {
        if (savedId == null) {
            val sr = ScanResult(content = data)
            historyVm.addScanResult(sr)
        }
    }

    val detectedType = remember(data) { BarcodeTypeDetector.detectType(data) }
    val safetyReport = remember(data) {
        if (detectedType == BarcodeTypeDetector.TYPE_URL) LinkSafetyChecker.analyze(data)
        else null
    }

    AppBackground()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(detectedType.uppercase(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Result card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "Scanned content",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = data,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Row of action chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChip("Copy", Icons.Filled.ContentCopy) { copyToClipboard(context, data) }
                ActionChip("Share", Icons.Filled.Share) { shareText(context, data) }
                when (detectedType) {
                    BarcodeTypeDetector.TYPE_URL -> {
                        ActionChip("Open", Icons.Filled.OpenInNew) { openUrl(context, data) }
                    }
                    BarcodeTypeDetector.TYPE_EMAIL -> {
                        ActionChip("Email", Icons.Filled.Email) { openUrl(context, data) }
                    }
                    BarcodeTypeDetector.TYPE_PHONE -> {
                        ActionChip("Dial", Icons.Filled.Phone) { openUrl(context, data) }
                    }
                }
            }

            // Link-safety report
            if (safetyReport != null) {
                Spacer(Modifier.height(20.dp))
                SafetyCard(report = safetyReport!!)
            }

            // Password leak quick check (URL)
            if (detectedType == BarcodeTypeDetector.TYPE_URL) {
                Spacer(Modifier.height(20.dp))
                LeakCheckCard(rawUrl = data)
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Open / Use",
                onClick = { openUrl(context, data) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SecondaryButton(
                text = "Done",
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.ActionChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SafetyCard(report: LinkSafetyChecker.Report) {
    val color = when (report.level) {
        LinkSafetyChecker.Level.SAFE -> SafetySafe
        LinkSafetyChecker.Level.CAUTION -> SafetyWarn
        LinkSafetyChecker.Level.RISKY -> SafetyRisky
        LinkSafetyChecker.Level.NOT_A_LINK -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Link Safety", style = MaterialTheme.typography.titleMedium, color = color)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { report.score / 100f },
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            report.signals.take(3).forEach { sig ->
                Text("• $sig", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LeakCheckCard(rawUrl: String) {
    val report = remember(rawUrl) { PasswordLeakChecker.check(rawUrl) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "Breach history for ${report.domain}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            if (report.leaked) {
                Text(
                    " martin.shipped: ${report.breachCount} recent breach ${if (report.firstSeenYear > 0) "since ${report.firstSeenYear}" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SafetyRisky
                )
            } else {
                Text(
                    "No public breach records for this domain.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (report.signals.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                report.signals.take(3).forEach { sig ->
                    Text("• $sig", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clip.setPrimaryClip(ClipData.newPlainText("scan", text))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share scan"))
}

private fun openUrl(context: Context, content: String) {
    val lower = content.trim().lowercase()
    val target = if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("mailto:")
        || lower.startsWith("tel:") || lower.startsWith("smsto:") || lower.startsWith("sms:")
        || lower.startsWith("geo:")) content else "https://$content"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, "No app to handle this content", Toast.LENGTH_SHORT).show()
    }
}
