package com.dhanuk.quickscanpro.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.ui.composables.SecondaryButton
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.LinkSafetyChecker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel

@Composable
fun ResultScreen(
    data: String,
    onNavigateBack: () -> Unit,
    onOpenProductLookup: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val historyVm: HistoryViewModel = viewModel()
    var savedId by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(data) {
        if (savedId == null) {
            val type = BarcodeTypeDetector.detectType(data)
            val exists = historyVm.history.value.any { it.content == data && it.type == type && System.currentTimeMillis() - it.timestamp < 60_000 }
            if (!exists) { savedId = 1; historyVm.addScanResult(ScanResult(content = data)) }
        }
    }

    val type = remember(data) { BarcodeTypeDetector.detectType(data) }
    val scanForContent = historyVm.history.value.firstOrNull { it.content == data }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        StitchResultHeader(type, onNavigateBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            when (type) {
                BarcodeTypeDetector.TYPE_WIFI -> WifiVariant(data, onNavigateBack)
                BarcodeTypeDetector.TYPE_VCARD -> VCardVariant(data, onNavigateBack)
                else -> UrlVariant(
                    data = data,
                    type = type,
                    isVault = scanForContent?.isVault == true,
                    onToggleVault = {
                        scanForContent?.let { historyVm.setVault(it, !it.isVault, context) }
                    },
                    onNavigateBack = onNavigateBack,
                    onOpenProductLookup = onOpenProductLookup
                )
            }
        }
    }
}

@Composable
private fun StitchResultHeader(type: String, onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
            }
            Row(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(typeIcon(type), contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Text(typeLabel(type), style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(48.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun WifiVariant(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val parts = remember(data) { parseWifi(data) }
    var showPass by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)), shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) }
            Spacer(Modifier.height(16.dp))
            Text(parts.first.ifEmpty { "Wi-Fi Network" }, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (parts.second.isNotEmpty()) (if (showPass) parts.second else "•".repeat(parts.second.length.coerceAtMost(12))) else "No password stored", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { showPass = !showPass }, modifier = Modifier.size(24.dp)) { Icon(if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Show password", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PillChip("Copy Password", Icons.Filled.ContentCopy) { copyToClipboard(context, parts.second) }
                PillChip("Share", Icons.Filled.Share) { shareText(context, data) }
            }
        }
    }
    PrimaryButton(text = "Connect to Network", onClick = { connectToWifi(context, parts.first, parts.second, parseWifiInfo(data).first) }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    SecondaryButton(text = "Done", onClick = onNavigateBack, modifier = Modifier.fillMaxWidth())
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun VCardVariant(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember(data) { parseVCard(data) }
    val initials = remember(fields) { val n = fields["N"] ?: fields["FN"]; n?.split(" ")?.take(2)?.joinToString("") { it.firstOrNull()?.uppercase() ?: "" }?.ifEmpty { "VC" } ?: "VC" }
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceBright, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)), shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Text(initials, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(16.dp))
            Text(fields["FN"] ?: fields["N"] ?: "Contact", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(fields["TITLE"] ?: "Contact", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            fields["TEL"]?.let { VCardFieldRow("Mobile", it, Icons.Filled.Call) }
            fields["EMAIL"]?.let { VCardFieldRow("Work Email", it, Icons.Filled.Mail) }
            fields["ADR"]?.let { VCardFieldRow("Address", it, Icons.Filled.LocationOn) }
        }
    }
    Spacer(Modifier.height(8.dp))
    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        VCardActionChip("Dial", Icons.Filled.Call) { openUrl(context, "tel:${fields["TEL"]}") }
        VCardActionChip("Email", Icons.Filled.Mail) { openUrl(context, "mailto:${fields["EMAIL"]}") }
        VCardActionChip("Copy", Icons.Filled.ContentCopy) { copyToClipboard(context, data) }
        VCardActionChip("Share", Icons.Filled.Share) { shareText(context, data) }
    }
    Spacer(Modifier.height(16.dp))
    PrimaryButton(text = "Save Contact", onClick = { saveVCardContact(context, fields, data) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Save Contact") }
    Spacer(Modifier.height(8.dp))
    SecondaryButton(text = "Done", onClick = onNavigateBack, modifier = Modifier.fillMaxWidth())
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun UrlVariant(
    data: String,
    type: String,
    isVault: Boolean,
    onToggleVault: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenProductLookup: (String) -> Unit
) {
    val context = LocalContext.current
    val safety = remember(data) { LinkSafetyChecker.analyze(data) }
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)), shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp)) }
            Spacer(Modifier.height(16.dp))
            Text(data, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text("Scanned just now", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(16.dp))
    if (safety.level != LinkSafetyChecker.Level.NOT_A_LINK) {
        SafetyCard(safety)
        Spacer(Modifier.height(16.dp))
    }
    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PillChip("Copy", Icons.Filled.ContentCopy) { copyToClipboard(context, data) }
        PillChip("Share", Icons.Filled.Share) { shareText(context, data) }
        if (type == BarcodeTypeDetector.TYPE_URL) { PillChip("Open", Icons.Filled.OpenInNew) { openUrl(context, data) } }
        if (type == BarcodeTypeDetector.TYPE_PRODUCT) { PillChip("Look up", Icons.Filled.Public) { onOpenProductLookup(data) } }
        PillChip(if (isVault) "Remove vault" else "Add to vault", if (isVault) Icons.Outlined.LockOpen else Icons.Filled.Lock, onToggleVault)
    }
    Spacer(Modifier.height(24.dp))
    PrimaryButton(
        text = when (type) {
            BarcodeTypeDetector.TYPE_URL -> "Open Link"
            BarcodeTypeDetector.TYPE_PRODUCT -> "Look Up Product"
            else -> "Done"
        },
        onClick = {
            when (type) {
                BarcodeTypeDetector.TYPE_URL -> openUrl(context, data)
                BarcodeTypeDetector.TYPE_PRODUCT -> onOpenProductLookup(data)
                else -> onNavigateBack()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            when (type) {
                BarcodeTypeDetector.TYPE_URL -> "Open Link"
                BarcodeTypeDetector.TYPE_PRODUCT -> "Look Up Product"
                else -> "Done"
            }
        )
    }
    Spacer(Modifier.height(8.dp))
    SecondaryButton(text = "Done", onClick = onNavigateBack, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SafetyCard(report: LinkSafetyChecker.Report) {
    val (label, color) = when (report.level) {
        LinkSafetyChecker.Level.SAFE -> "No warning signs detected" to MaterialTheme.colorScheme.secondary
        LinkSafetyChecker.Level.CAUTION -> "Review this link before opening" to MaterialTheme.colorScheme.tertiary
        LinkSafetyChecker.Level.RISKY -> "Potentially risky link" to MaterialTheme.colorScheme.error
        LinkSafetyChecker.Level.NOT_A_LINK -> return
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.GppGood, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Safety score ${report.score}/100", style = MaterialTheme.typography.labelLarge, color = color)
            }
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            report.signals.take(2).forEach { signal ->
                Text(signal, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun VCardFieldRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable private fun FlowRowScope.VCardActionChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(modifier = Modifier.width(80.dp).height(80.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceBright).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).clickable(onClick = onClick), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)); Spacer(Modifier.height(4.dp)); Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable private fun FlowRowScope.PillChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceContainer).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(50)).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun typeIcon(type: String) = when (type) {
    BarcodeTypeDetector.TYPE_URL -> Icons.Filled.Public; BarcodeTypeDetector.TYPE_WIFI -> Icons.Filled.Wifi; BarcodeTypeDetector.TYPE_EMAIL -> Icons.Filled.Email; BarcodeTypeDetector.TYPE_PHONE -> Icons.Filled.Phone; BarcodeTypeDetector.TYPE_VCARD -> Icons.Filled.PersonAdd; else -> Icons.Filled.Public
}
private fun typeLabel(type: String) = when (type) {
    BarcodeTypeDetector.TYPE_URL -> "URL"; BarcodeTypeDetector.TYPE_WIFI -> "Wi-Fi"; BarcodeTypeDetector.TYPE_EMAIL -> "Email"; BarcodeTypeDetector.TYPE_PHONE -> "Phone"; BarcodeTypeDetector.TYPE_VCARD -> "vCard"; else -> type.replaceFirstChar { it.uppercase() }
}
private fun parseWifi(data: String): Pair<String, String> { var ssid = ""; var pass = ""; data.removePrefix("WIFI:").split(";").forEach { val kv = it.split(":", limit = 2); if (kv.size == 2) when (kv[0].uppercase()) { "S" -> ssid = kv[1]; "P" -> pass = kv[1] } }; return ssid to pass }
private fun parseWifiInfo(data: String): Pair<String, String> { var security = ""; data.removePrefix("WIFI:").split(";").forEach { val kv = it.split(":", limit = 2); if (kv.size == 2 && kv[0].uppercase() == "T") security = kv[1] }; return security to "" }
private fun connectToWifi(context: Context, ssid: String, password: String, security: String) { try { context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)) } catch (_: Exception) { Toast.makeText(context, "Could not open Wi-Fi settings", Toast.LENGTH_SHORT).show() } }
private fun parseVCard(data: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    data.lines().forEach { line -> val l = line.trim(); when {
        l.startsWith("FN:", ignoreCase = true) -> map["FN"] = l.substring(3); l.startsWith("N:", ignoreCase = true) -> map["N"] = l.substring(2).replace(";", " "); l.startsWith("TEL") -> map["TEL"] = l.substringAfter(":"); l.startsWith("EMAIL") -> map["EMAIL"] = l.substringAfter(":"); l.startsWith("TITLE") -> map["TITLE"] = l.substringAfter(":"); l.startsWith("ADR") -> map["ADR"] = l.substringAfter(":").replace(";", " ").trim()
    } }; return map
}
private fun copyToClipboard(context: Context, text: String) { (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("scan", text)); Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show() }
private fun shareText(context: Context, text: String) { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share scan")) }
private fun openUrl(context: Context, content: String) { val target = if (content.lowercase().let { it.startsWith("http") || it.startsWith("mailto:") || it.startsWith("tel:") || it.startsWith("geo:") }) content else "https://$content"; try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { Toast.makeText(context, "No app to open", Toast.LENGTH_SHORT).show() } }
private fun saveVCardContact(context: Context, fields: Map<String, String>, raw: String) { val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply { type = ContactsContract.Contacts.CONTENT_TYPE; putExtra(ContactsContract.Intents.Insert.NAME, fields["FN"] ?: fields["N"] ?: ""); fields["TEL"]?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }; fields["EMAIL"]?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) } }; try { context.startActivity(intent) } catch (_: Exception) { shareText(context, raw) } }
