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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
        StitchResultHeader(type, data, onNavigateBack)
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
private fun StitchResultHeader(type: String, data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            "Scan Result",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = { shareText(context, data) }) {
            Icon(
                Icons.Filled.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurface
            )
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
    var showSafetyDialog by remember { mutableStateOf(false) }

    // ── Hero card ──
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                typeIcon(type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                typeLabel(type),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            data,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }

    // ── Safety status card ──
    if (safety.level != LinkSafetyChecker.Level.NOT_A_LINK) {
        Spacer(Modifier.height(16.dp))
        SafetyStatusCard(safety)
    }

    // ── 2x2 Action grid ──
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionGridButton(
            label = if (type == BarcodeTypeDetector.TYPE_PRODUCT) "Look Up" else "Open Link",
            icon = Icons.Filled.OpenInNew,
            style = ActionStyle.FILLED,
            modifier = Modifier.weight(1f)
        ) {
            when (type) {
                BarcodeTypeDetector.TYPE_URL -> openUrl(context, data)
                BarcodeTypeDetector.TYPE_PRODUCT -> onOpenProductLookup(data)
                else -> openUrl(context, data)
            }
        }
        ActionGridButton(
            label = "Copy",
            icon = Icons.Filled.ContentCopy,
            style = ActionStyle.TONAL,
            modifier = Modifier.weight(1f)
        ) { copyToClipboard(context, data) }
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionGridButton(
            label = "Share",
            icon = Icons.Filled.Share,
            style = ActionStyle.TONAL,
            modifier = Modifier.weight(1f)
        ) { shareText(context, data) }
        ActionGridButton(
            label = if (isVault) "In Vault" else "Save to Vault",
            icon = if (isVault) Icons.Outlined.LockOpen else Icons.Filled.Lock,
            style = ActionStyle.OUTLINED,
            modifier = Modifier.weight(1f),
            onClick = onToggleVault
        )
    }

    // ── Metadata card ──
    Spacer(Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(20.dp)
    ) {
        Text(
            "Metadata",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        MetadataRow("Type", typeLabel(type))
        MetadataRow("Scanned", "Just now")
        MetadataRow("Source", "Camera")
    }

    // ── Additional actions card ──
    Spacer(Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        if (type == BarcodeTypeDetector.TYPE_PRODUCT) {
            AdditionalActionRow(
                icon = Icons.Filled.Public,
                label = "Product lookup",
                onClick = { onOpenProductLookup(data) }
            )
        }
        AdditionalActionRow(
            icon = Icons.Filled.GppGood,
            label = "Safety score ${safety.score}/100",
            onClick = { showSafetyDialog = true }
        )
    }

    if (showSafetyDialog) {
        AlertDialog(
            onDismissRequest = { showSafetyDialog = false },
            icon = { Icon(Icons.Filled.GppGood, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Safety Report") },
            text = {
                Column {
                    Text("Score: ${safety.score}/100", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (safety.signals.isEmpty()) {
                        Text("No suspicious signals detected.")
                    } else {
                        Text("Findings:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        safety.signals.forEach { signal ->
                            Text("• $signal", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSafetyDialog = false }) { Text("OK") } }
        )
    }

    Spacer(Modifier.height(16.dp))
    SecondaryButton(text = "Done", onClick = onNavigateBack, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(16.dp))
}

private enum class ActionStyle { FILLED, TONAL, OUTLINED }

@Composable
private fun ActionGridButton(
    label: String,
    icon: ImageVector,
    style: ActionStyle,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = when (style) {
        ActionStyle.FILLED -> MaterialTheme.colorScheme.primary
        ActionStyle.TONAL -> MaterialTheme.colorScheme.secondaryContainer
        ActionStyle.OUTLINED -> Color.Transparent
    }
    val fg = when (style) {
        ActionStyle.FILLED -> MaterialTheme.colorScheme.onPrimary
        ActionStyle.TONAL -> MaterialTheme.colorScheme.primary
        ActionStyle.OUTLINED -> MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .then(
                if (style == ActionStyle.OUTLINED)
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AdditionalActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun SafetyStatusCard(report: LinkSafetyChecker.Report) {
    val (title, subtitle, color, icon) = when (report.level) {
        LinkSafetyChecker.Level.SAFE -> Tuple4("Safe Link", "No threats detected by security check", com.dhanuk.quickscanpro.ui.theme.SemanticSafe, Icons.Filled.GppGood)
        LinkSafetyChecker.Level.CAUTION -> Tuple4("Review Link", "Check this link before opening", com.dhanuk.quickscanpro.ui.theme.SemanticWarn, Icons.Filled.GppGood)
        LinkSafetyChecker.Level.RISKY -> Tuple4("Risky Link", "This link may be dangerous", MaterialTheme.colorScheme.error, Icons.Filled.GppGood)
        LinkSafetyChecker.Level.NOT_A_LINK -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

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
