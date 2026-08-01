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
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.ui.composables.SecondaryButton
import com.dhanuk.quickscanpro.ui.theme.SafetyWarn
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
    var savedId by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(data) {
        if (savedId == null) {
            val type = BarcodeTypeDetector.detectType(data)
            val exists = historyVm.history.value.any {
                it.content == data && it.type == type &&
                    System.currentTimeMillis() - it.timestamp < 60_000
            }
            if (!exists) {
                savedId = 1
                historyVm.addScanResult(ScanResult(content = data))
            }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(typePillColor(type))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = typeIcon(type),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = typeIconTint(type)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            typeLabel(type),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = typeIconTint(type)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { Spacer(Modifier.width(40.dp)) },
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
                .padding(horizontal = 20.dp)
        ) {
            when (type) {
                BarcodeTypeDetector.TYPE_WIFI -> WifiVariant(data, onNavigateBack)
                BarcodeTypeDetector.TYPE_VCARD -> VCardVariant(data, onNavigateBack)
                else -> UrlVariant(data, type, safetyReport, onNavigateBack)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun WifiVariant(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val parts = remember(data) { parseWifi(data) }
    var showPass by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = parts.first.ifEmpty { "Wi-Fi Network" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (parts.second.isNotEmpty()) (if (showPass) parts.second else "\u2022".repeat(parts.second.length.coerceAtMost(12))) else "No password stored",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { showPass = !showPass }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Show password",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PillChip("Copy Password", Icons.Filled.ContentCopy) {
                    copyToClipboard(context, parts.second)
                }
                PillChip("Share", Icons.Filled.Share) {
                    shareText(context, data)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    val wifiInfo = remember(data) { parseWifiInfo(data) }
    val security = wifiInfo.first
    WifiSafetySection(security = security, ssid = parts.first)

    Spacer(Modifier.height(24.dp))

    PrimaryButton(
        text = "Connect to Network",
        onClick = { connectToWifi(context, parts.first, parts.second, security) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    SecondaryButton(
        text = "Done",
        onClick = onNavigateBack,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun WifiSafetySection(security: String, ssid: String) {
    val score = when {
        security.equals("WPA3", ignoreCase = true) -> 95
        security.equals("WPA2", ignoreCase = true) -> 85
        security.equals("WPA", ignoreCase = true) -> 70
        security.equals("WEP", ignoreCase = true) -> 40
        security.equals("nopass", ignoreCase = true) || security.isEmpty() -> 25
        else -> 50
    }
    val (tint, bg) = when {
        score >= 80 -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
        score < 50 -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
        else -> SafetyWarn to MaterialTheme.colorScheme.surfaceVariant
    }
    val label = when {
        score >= 80 -> "Safe"
        score < 50 -> "Insecure"
        else -> "Caution"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.GppGood,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = bg,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = "$score/100",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = tint,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Local network${if (ssid.isNotEmpty()) " \"$ssid\"" else ""}. $security security.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(0.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Check password leak",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Verify against known breaches",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun VCardVariant(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember(data) { parseVCard(data) }
    val initials = remember(fields) {
        val n = fields["N"] ?: fields["FN"]
        val parts = n?.split(" ") ?: emptyList()
        parts.take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }.ifEmpty { "VC" }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = fields["FN"] ?: fields["N"] ?: "Contact",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = fields["TITLE"] ?: "Contact",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            fields["TEL"]?.let { VCardFieldRow("Mobile", it, Icons.Filled.Call) }
            fields["EMAIL"]?.let { VCardFieldRow("Work Email", it, Icons.Filled.Mail) }
            fields["ADR"]?.let { VCardFieldRow("Address", it, Icons.Filled.LocationOn) }
        }
    }

    Spacer(Modifier.height(8.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VCardActionChip("Dial", Icons.Filled.Call) { openUrl(context, "tel:${fields["TEL"]}") }
        VCardActionChip("Email", Icons.Filled.Mail) { openUrl(context, "mailto:${fields["EMAIL"]}") }
        VCardActionChip("Copy", Icons.Filled.ContentCopy) { copyToClipboard(context, data) }
        VCardActionChip("Share", Icons.Filled.Share) { shareText(context, data) }
    }

    Spacer(Modifier.height(16.dp))

    VCardSafetySection(score = 98)

    Spacer(Modifier.height(24.dp))
    PrimaryButton(
        text = "Save Contact",
        onClick = { saveVCardContact(context, fields, data) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Save Contact")
    }
    Spacer(Modifier.height(8.dp))
    SecondaryButton(
        text = "Done",
        onClick = onNavigateBack,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun VCardFieldRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.VCardActionChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun VCardSafetySection(score: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Safe Content",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Standard contact format",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun UrlVariant(
    data: String,
    type: String,
    safetyReport: LinkSafetyChecker.Report?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showRiskConfirm by rememberSaveable { mutableStateOf(false) }
    val isRisky = safetyReport?.level == LinkSafetyChecker.Level.RISKY

    fun tryOpenUrl() {
        if (isRisky) showRiskConfirm = true else openUrl(context, data)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = data,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Scanned just now",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UrlPillChip("Copy", Icons.Filled.ContentCopy) { copyToClipboard(context, data) }
        UrlPillChip("Share", Icons.Filled.Share) { shareText(context, data) }
        if (type == BarcodeTypeDetector.TYPE_URL) {
            UrlPillChip("Open", Icons.Filled.OpenInNew) { tryOpenUrl() }
        }
    }

    Spacer(Modifier.height(16.dp))

    UrlSafetySection(score = safetyReport?.score ?: 95, report = safetyReport)

    Spacer(Modifier.height(24.dp))
    PrimaryButton(
        text = "Open Link",
        onClick = { tryOpenUrl() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Open Link")
    }
    Spacer(Modifier.height(8.dp))
    SecondaryButton(
        text = "Done",
        onClick = onNavigateBack,
        modifier = Modifier.fillMaxWidth()
    )

    if (showRiskConfirm) {
        AlertDialog(
            onDismissRequest = { showRiskConfirm = false },
            title = { Text("Warning: Unsafe Link") },
            text = {
                Text(
                    "This link has been flagged as potentially dangerous.\n\n" +
                    safetyReport?.signals?.joinToString("\n")?.let { "$it\n\n" } +
                    "Are you sure you want to open it?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRiskConfirm = false
                    openUrl(context, data)
                }) {
                    Text("Open anyway", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRiskConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun UrlSafetySection(score: Int, report: LinkSafetyChecker.Report?) {
    val result = when (report?.level) {
        LinkSafetyChecker.Level.SAFE ->
            arrayOf(
                "Safe", report.signals.firstOrNull() ?: "No threats detected",
                MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
        LinkSafetyChecker.Level.CAUTION ->
            arrayOf(
                "Caution", report.signals.firstOrNull() ?: "Potential risk",
                SafetyWarn, SafetyWarn.copy(alpha = 0.1f)
            )
        LinkSafetyChecker.Level.RISKY ->
            arrayOf(
                "Danger", report.signals.firstOrNull() ?: "Unsafe link",
                MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            )
        else ->
            arrayOf(
                "Unknown", "Could not analyze link",
                MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
    }
    val label = result[0] as String
    val signal = result[1] as String
    val tint = result[2] as Color
    val bg = result[3] as Color


    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.GppGood,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = signal,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Safety Score",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$score/100",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.PillChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.UrlPillChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
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

@Composable
private fun typePillColor(type: String): Color = when (type) {
    BarcodeTypeDetector.TYPE_WIFI -> MaterialTheme.colorScheme.surfaceContainerHigh
    BarcodeTypeDetector.TYPE_VCARD -> MaterialTheme.colorScheme.surfaceContainerHigh
    BarcodeTypeDetector.TYPE_URL -> MaterialTheme.colorScheme.surfaceVariant
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun typeIconTint(type: String): Color = when (type) {
    BarcodeTypeDetector.TYPE_URL -> MaterialTheme.colorScheme.onSurfaceVariant
    else -> MaterialTheme.colorScheme.primary
}

private fun parseWifi(data: String): Pair<String, String> {
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

private fun parseWifiInfo(data: String): Pair<String, String> {
    var security = ""
    var hidden = ""
    data.removePrefix("WIFI:").split(";").forEach { seg ->
        val kv = seg.split(":", limit = 2)
        if (kv.size == 2) {
            when (kv[0].uppercase()) {
                "T" -> security = kv[1]
                "H" -> hidden = kv[1]
            }
        }
    }
    return security to hidden
}

private fun connectToWifi(context: Context, ssid: String, password: String, security: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("wifi://connect")
    }
    try {
        context.startActivity(intent)
        Toast.makeText(context, "Open Wi-Fi settings to connect", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        val fallback = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
        try {
            context.startActivity(fallback)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open Wi-Fi settings", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun saveVCardContact(context: Context, fields: Map<String, String>, raw: String) {
    val name = fields["FN"] ?: fields["N"] ?: ""
    val phone = fields["TEL"]
    val email = fields["EMAIL"]
    val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
        type = ContactsContract.Contacts.CONTENT_TYPE
        putExtra(ContactsContract.Intents.Insert.NAME, name)
        phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
        email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        shareText(context, raw)
    }
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
