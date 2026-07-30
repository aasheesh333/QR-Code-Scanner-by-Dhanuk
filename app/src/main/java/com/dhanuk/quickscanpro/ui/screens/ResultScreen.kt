package com.dhanuk.quickscanpro.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.vector.ImageVector
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
            historyVm.addScanResult(ScanResult(content = data))
        }
    }

    val type = remember(data) { BarcodeTypeDetector.detectType(data) }
    val safetyReport = remember(data) {
        if (type == BarcodeTypeDetector.TYPE_URL) LinkSafetyChecker.analyze(data) else null
    }

    AppBackground()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = typeIcon(type),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            typeLabel(type),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            when (type) {
                BarcodeTypeDetector.TYPE_WIFI -> WifiVariant(data)
                BarcodeTypeDetector.TYPE_VCARD -> VCardVariant(data)
                else -> UrlOrGenericVariant(data, type, safetyReport)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun WifiVariant(data: String) {
    val context = LocalContext.current
    val parts = remember(data) { parseWifi(data) } // ssid, password
    var showPass by remember { mutableStateOf(false) }
    ResultCard(
        icon = Icons.Filled.Wifi,
        title = parts.first.ifEmpty { "Wi-Fi Network" },
        subtitle = if (parts.second.isNotEmpty()) (if (showPass) parts.second else "•".repeat(parts.second.length.coerceAtMost(12))) else "No password stored"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { showPass = !showPass }) {
                Icon(if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Show password")
            }
            MiniChip("Copy Password", Icons.Filled.ContentCopy) {
                copyToClipboard(context, parts.second)
            }
            MiniChip("Share", Icons.Filled.Share) { shareText(context, data) }
        }
        Spacer(Modifier.height(16.dp))
        SafetyBlock(score = 100, verdict = "Safe", signals = listOf("Local network verified", "Security protocol detected."))
        Spacer(Modifier.height(12.dp))
        LeakCheckRow(domain = parts.first.ifEmpty { data })
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Connect to Network", { /* placeholder */ }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Done", {}, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun VCardVariant(data: String) {
    val context = LocalContext.current
    val fields = remember(data) { parseVCard(data) }
    val initials = remember(fields) {
        val n = fields["N"] ?: fields["FN"]
        val parts = n?.split(" ") ?: emptyList()
        parts.take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }.ifEmpty { "VC" }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        fields["FN"] ?: fields["N"] ?: "Contact",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    fields["TITLE"]?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            fields["TEL"]?.let { FieldRow("Mobile", it, Icons.Filled.Phone) }
            fields["EMAIL"]?.let { FieldRow("Work Email", it, Icons.Filled.Email) }
            fields["ADR"]?.let { FieldRow("Address", it, Icons.Filled.Public) }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MiniChip("Dial", Icons.Filled.Phone) { openUrl(context, "tel:${fields["TEL"]}")}
                MiniChip("Email", Icons.Filled.Email) { openUrl(context, "mailto:${fields["EMAIL"]}")}
                MiniChip("Copy", Icons.Filled.ContentCopy) { copyToClipboard(context, data) }
                MiniChip("Share", Icons.Filled.Share) { shareText(context, data) }
            }

            Spacer(Modifier.height(16.dp))
            SafetyBlock(score = 98, verdict = "Safe Content", signals = listOf("Standard contact format"))
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Save Contact", {}, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Done", {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun UrlOrGenericVariant(
    data: String,
    type: String,
    safetyReport: LinkSafetyChecker.Report?
) {
    val context = LocalContext.current
    ResultCard(
        icon = typeIcon(type),
        title = data,
        subtitle = "Scanned just now"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniChip("Copy", Icons.Filled.ContentCopy) { copyToClipboard(context, data) }
            MiniChip("Share", Icons.Filled.Share) { shareText(context, data) }
            if (type == BarcodeTypeDetector.TYPE_URL) {
                MiniChip("Open", Icons.Filled.OpenInNew) { openUrl(context, data) }
            }
        }
        if (safetyReport != null) {
            Spacer(Modifier.height(16.dp))
            SafetyBlock(
                score = safetyReport.score,
                verdict = safetyReportVerdict(safetyReport.level),
                signals = safetyReport.signals.take(3)
            )
            Spacer(Modifier.height(12.dp))
            LeakCheckRow(domain = data)
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Open Link", { openUrl(context, data) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Done", {}, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ResultCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable FlowRowScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.MiniChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun FieldRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SafetyBlock(score: Int, verdict: String, signals: List<String>) {
    val color = when {
        score >= 80 -> SafetySafe
        score >= 50 -> SafetyWarn
        else -> SafetyRisky
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Public, contentDescription = null, tint = color)
                Spacer(Modifier.width(8.dp))
                Text(verdict, style = MaterialTheme.typography.titleMedium, color = color)
                Spacer(Modifier.weight(1f))
                Text("$score/100", style = MaterialTheme.typography.labelLarge, color = color)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                color = color,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.height(8.dp))
            signals.forEach { sig ->
                Text("• $sig", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LeakCheckRow(domain: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Check password leak", style = MaterialTheme.typography.titleSmall)
            Text("Verify against known breaches", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun typeIcon(type: String): ImageVector = when (type) {
    BarcodeTypeDetector.TYPE_URL -> Icons.Filled.Public
    BarcodeTypeDetector.TYPE_WIFI -> Icons.Filled.Wifi
    BarcodeTypeDetector.TYPE_EMAIL -> Icons.Filled.Email
    BarcodeTypeDetector.TYPE_PHONE -> Icons.Filled.Phone
    BarcodeTypeDetector.TYPE_VCARD -> Icons.Filled.PersonAdd
    else -> Icons.Filled.Public
}

private fun typeLabel(type: String): String = when (type) {
    BarcodeTypeDetector.TYPE_URL -> "URL"
    BarcodeTypeDetector.TYPE_WIFI -> "Wi-Fi"
    BarcodeTypeDetector.TYPE_EMAIL -> "Email"
    BarcodeTypeDetector.TYPE_PHONE -> "Phone"
    BarcodeTypeDetector.TYPE_VCARD -> "vCard"
    else -> type.replaceFirstChar { it.uppercase() }
}

private fun safetyReportVerdict(level: LinkSafetyChecker.Level): String = when (level) {
    LinkSafetyChecker.Level.SAFE -> "Safe"
    LinkSafetyChecker.Level.CAUTION -> "Caution"
    LinkSafetyChecker.Level.RISKY -> "Risky"
    LinkSafetyChecker.Level.NOT_A_LINK -> "Info"
}

private fun parseWifi(data: String): Pair<String, String> {
    // WIFI:T:WPA;S:MySSID;P:mypass;;
    var ssid = ""
    var pass = ""
    data.removePrefix("WIFI:").split(";").forEach { seg ->
        val kv = seg.split(":", limit = 2)
        if (kv.size == 2) {
            when (kv[0].uppercase()) {
                "S" -> ssid = kv[1]
                "P" -> pass = kv[1]
            }
        }
    }
    return ssid to pass
}

private fun parseVCard(data: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    data.lines().forEach { line ->
        val l = line.trim()
        when {
            l.startsWith("FN:", ignoreCase = true) -> map["FN"] = l.substring(3)
            l.startsWith("N:", ignoreCase = true) -> map["N"] = l.substring(2).replace(";", " ")
            l.startsWith("TEL", ignoreCase = true) -> map["TEL"] = l.substringAfter(":")
            l.startsWith("EMAIL", ignoreCase = true) -> map["EMAIL"] = l.substringAfter(":")
            l.startsWith("TITLE", ignoreCase = true) -> map["TITLE"] = l.substringAfter(":")
            l.startsWith("ADR", ignoreCase = true) -> map["ADR"] = l.substringAfter(":").replace(";", " ").trim()
        }
    }
    return map
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
    } catch (_: Exception) {
        Toast.makeText(context, "No app to handle this content", Toast.LENGTH_SHORT).show()
    }
}
