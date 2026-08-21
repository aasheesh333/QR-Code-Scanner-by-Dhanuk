package com.dhanuk.quickscanpro.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LinearProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.LinkSafetyChecker
import com.dhanuk.quickscanpro.util.TextLanguageDetector
import com.dhanuk.quickscanpro.util.VoiceSpeaker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    data: String,
    onNavigateBack: () -> Unit,
    fromHistory: Boolean = false,
    onOpenProductLookup: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val historyVm: HistoryViewModel = viewModel()
    val settingsVm: SettingsViewModel = viewModel()
    val historyEnabled by settingsVm.scanHistory.collectAsState()
    val incognito by settingsVm.incognitoMode.collectAsState()
    val canSaveHistory = historyEnabled && !incognito
    val history by historyVm.history.collectAsState()
    var savedOnce by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(data) {
        if (!fromHistory && !savedOnce) {
            val type = BarcodeTypeDetector.detectType(data)
            val exists = history.any {
                it.content == data && System.currentTimeMillis() - it.timestamp < 60_000
            }
            if (!exists) {
                if (canSaveHistory) {
                    historyVm.addScanResult(ScanResult(content = data, type = type))
                } else {
                    Toast.makeText(
                        context,
                        "History saving is off — enable it in Settings to keep scans",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            savedOnce = true
        }
    }

    val type = remember(data) { BarcodeTypeDetector.detectType(data) }
    val scanForContent = history.firstOrNull { it.content == data }
    val done: () -> Unit = { VoiceSpeaker.stop(); onNavigateBack() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Scan Result", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = done) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { shareText(context, data) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (type) {
                BarcodeTypeDetector.TYPE_WIFI -> WifiResult(data, done)
                BarcodeTypeDetector.TYPE_VCARD -> ContactResult(data, done)
                BarcodeTypeDetector.TYPE_CALENDAR -> EventResult(data, done)
                else -> GenericResult(
                    data = data,
                    type = type,
                    isVault = scanForContent?.isVault == true,
                    onToggleVault = {
                        val target = scanForContent
                        when {
                            target != null -> historyVm.setVault(target, !target.isVault, context)
                            canSaveHistory -> historyVm.saveAsVaulted(data)
                            else -> Toast.makeText(
                                context,
                                "History saving is off — enable it in Settings to use the vault",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onNavigateBack = done,
                    onOpenProductLookup = onOpenProductLookup
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

// ───────────── Shared helpers ─────────────

private fun copyToClipboard(context: Context, text: String) {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        .setPrimaryClip(ClipData.newPlainText("scan", text))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    runCatching {
        context.startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            "Share scan"
        ))
    }
}

private fun openUrl(context: Context, content: String) {
    // If the content already carries a scheme (http, mailto, tel, ftp, geo, market, …)
    // pass it through untouched; otherwise assume a bare domain and prefix https://.
    val hasScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:").containsMatchIn(content.trim())
    val target = if (hasScheme) content.trim() else "https://${content.trim()}"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        Toast.makeText(context, "No app to open this", Toast.LENGTH_SHORT).show()
    }
}

private fun openTarget(type: String, data: String): String? = when (type) {
    BarcodeTypeDetector.TYPE_URL -> data
    BarcodeTypeDetector.TYPE_EMAIL -> if (data.startsWith("mailto:", ignoreCase = true)) data else "mailto:$data"
    BarcodeTypeDetector.TYPE_PHONE -> if (data.startsWith("tel:", ignoreCase = true)) data else "tel:$data"
    BarcodeTypeDetector.TYPE_SMS -> if (data.startsWith("sms:", ignoreCase = true) || data.startsWith("smsto:", ignoreCase = true)) data else "sms:$data"
    BarcodeTypeDetector.TYPE_GEO -> BarcodeTypeDetector.parseGeo(data)?.let { "geo:${it.latitude},${it.longitude}" }
    else -> null
}

private fun openLabel(type: String) = when (type) {
    BarcodeTypeDetector.TYPE_PHONE -> "Call"
    BarcodeTypeDetector.TYPE_EMAIL -> "Email"
    BarcodeTypeDetector.TYPE_SMS -> "Message"
    BarcodeTypeDetector.TYPE_GEO -> "Open map"
    else -> "Open"
}

private fun typeLabel(type: String) = when (type) {
    BarcodeTypeDetector.TYPE_URL -> "Web link"
    BarcodeTypeDetector.TYPE_WIFI -> "Wi-Fi network"
    BarcodeTypeDetector.TYPE_EMAIL -> "Email"
    BarcodeTypeDetector.TYPE_PHONE -> "Phone number"
    BarcodeTypeDetector.TYPE_SMS -> "Text message"
    BarcodeTypeDetector.TYPE_VCARD -> "Contact"
    BarcodeTypeDetector.TYPE_CALENDAR -> "Calendar event"
    BarcodeTypeDetector.TYPE_GEO -> "Location"
    BarcodeTypeDetector.TYPE_PRODUCT -> "Product barcode"
    else -> "Text"
}

private fun typeIcon(type: String): ImageVector = when (type) {
    BarcodeTypeDetector.TYPE_URL -> Icons.Filled.OpenInNew
    BarcodeTypeDetector.TYPE_WIFI -> Icons.Filled.Wifi
    BarcodeTypeDetector.TYPE_EMAIL -> Icons.Filled.Email
    BarcodeTypeDetector.TYPE_PHONE -> Icons.Filled.Call
    BarcodeTypeDetector.TYPE_SMS -> Icons.Filled.Email
    BarcodeTypeDetector.TYPE_VCARD -> Icons.Filled.PersonAdd
    BarcodeTypeDetector.TYPE_CALENDAR -> Icons.Filled.CalendarMonth
    BarcodeTypeDetector.TYPE_GEO -> Icons.Filled.LocationOn
    BarcodeTypeDetector.TYPE_PRODUCT -> Icons.Filled.Storefront
    else -> Icons.Filled.Language
}

// ───────────── Generic / URL variant ─────────────

@Composable
private fun GenericResult(
    data: String,
    type: String,
    isVault: Boolean,
    onToggleVault: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenProductLookup: (String) -> Unit
) {
    val context = LocalContext.current
    val safety = remember(data) { LinkSafetyChecker.analyze(data) }
    var showSafety by remember { mutableStateOf(false) }

    QsCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconBadge(typeIcon(type), size = 56.dp)
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
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
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }

    if (safety.level != LinkSafetyChecker.Level.NOT_A_LINK) {
        SafetyBanner(safety) { showSafety = true }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val openTarget = openTarget(type, data)
        if (type == BarcodeTypeDetector.TYPE_PRODUCT) {
            ActionTile(
                label = "Look up",
                icon = Icons.Filled.OpenInNew,
                filled = true,
                modifier = Modifier.weight(1f)
            ) { onOpenProductLookup(data) }
        } else if (openTarget != null) {
            ActionTile(
                label = openLabel(type),
                icon = Icons.Filled.OpenInNew,
                filled = true,
                modifier = Modifier.weight(1f)
            ) { openUrl(context, openTarget) }
        }
        ActionTile("Copy", Icons.Filled.ContentCopy, filled = false, modifier = Modifier.weight(1f)) {
            copyToClipboard(context, data)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionTile("Share", Icons.Filled.Share, filled = false, modifier = Modifier.weight(1f)) {
            shareText(context, data)
        }
        ActionTile(
            label = if (isVault) "Vaulted" else "Vault",
            icon = if (isVault) Icons.Outlined.LockOpen else Icons.Filled.Lock,
            filled = false,
            modifier = Modifier.weight(1f),
            onClick = onToggleVault
        )
    }

    QsCard(contentPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(enabled = type == BarcodeTypeDetector.TYPE_PRODUCT) { if (type == BarcodeTypeDetector.TYPE_PRODUCT) onOpenProductLookup(data) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(Icons.Filled.VolumeUp, size = 38.dp)
            Spacer(Modifier.width(12.dp))
            Text("Speak aloud", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = {
                VoiceSpeaker.init(context)
                VoiceSpeaker.speak(data)
            }) { Text("Play") }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(Icons.Filled.Translate, size = 38.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Translate", style = MaterialTheme.typography.bodyLarge)
                if (TextLanguageDetector.isLikelyForeign(data)) {
                    Text(
                        "Looks like ${TextLanguageDetector.sourceLanguageHint(data)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = {
                runCatching {
                    context.startActivity(Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://translate.google.com/?text=${Uri.encode(data)}")
                    ))
                }
            }) { Text("Open") }
        }
        if (safety.level != LinkSafetyChecker.Level.NOT_A_LINK) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth().clickable { showSafety = true }.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(Icons.Filled.GppGood, size = 38.dp)
                Spacer(Modifier.width(12.dp))
                Text("Full safety report", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Text("${safety.score}/100", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showSafety) SafetyDialog(safety) { showSafety = false }

    QsOutlinedButton("Done", onClick = onNavigateBack)
}

// ───────────── WiFi variant ─────────────

@Composable
private fun WifiResult(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val info = remember(data) { BarcodeTypeDetector.parseWifi(data) }
    var showPass by rememberSaveable { mutableStateOf(false) }

    QsCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            IconBadge(Icons.Filled.Wifi, size = 56.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                info?.ssid ?: "Wi-Fi network",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when (info?.encryption?.uppercase()) {
                    "WPA", "WPA2", "WPA3" -> "Secured (WPA)"
                    "WEP" -> "Secured (WEP)"
                    "NOPASS" -> "Open network"
                    else -> info?.encryption?.takeIf { it.isNotBlank() } ?: "Network"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (info == null || info.password.isEmpty()) "No password stored"
                    else if (showPass) info.password else "•".repeat(info.password.length.coerceAtMost(14)),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (info != null && info.password.isNotEmpty()) {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionTile(
            label = "Copy password",
            icon = Icons.Filled.ContentCopy,
            filled = false,
            modifier = Modifier.weight(1f)
        ) {
            val pass = info?.password.orEmpty()
            if (pass.isEmpty()) {
                Toast.makeText(context, "No password stored in this code", Toast.LENGTH_SHORT).show()
            } else {
                copyToClipboard(context, pass)
            }
        }
        ActionTile("Share", Icons.Filled.Share, filled = false, modifier = Modifier.weight(1f)) {
            shareText(context, data)
        }
    }

    QsButton(
        text = "Connect to network",
        icon = Icons.Filled.Wifi,
        onClick = {
            runCatching { context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)) }
                .onFailure { Toast.makeText(context, "Could not open Wi-Fi settings", Toast.LENGTH_SHORT).show() }
        }
    )
    QsOutlinedButton("Done", onClick = onNavigateBack)
}

// ───────────── vCard variant ─────────────

@Composable
private fun ContactResult(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val contact = remember(data) { BarcodeTypeDetector.parseVCard(data) }
    val initials = remember(contact) {
        val parts = contact.name.split(" ").filter { it.isNotBlank() }
        if (parts.isEmpty()) "VC"
        else parts.take(2).joinToString("") { it.first().uppercase() }
    }

    QsCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                contact.name.ifEmpty { "Contact card" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (contact.org.isNotBlank()) {
                Text(contact.org, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            if (contact.phone.isNotBlank()) ContactField("Phone", contact.phone, Icons.Filled.Call)
            if (contact.email.isNotBlank()) ContactField("Email", contact.email, Icons.Filled.Email)
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionTile("Copy", Icons.Filled.ContentCopy, filled = false, modifier = Modifier.weight(1f)) {
            copyToClipboard(context, data)
        }
        ActionTile("Share", Icons.Filled.Share, filled = false, modifier = Modifier.weight(1f)) {
            shareText(context, data)
        }
    }

    QsButton(
        text = "Save contact",
        icon = Icons.Filled.PersonAdd,
        onClick = {
            val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                type = ContactsContract.Contacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.NAME, contact.name)
                putExtra(ContactsContract.Intents.Insert.PHONE, contact.phone)
                putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email)
            }
            runCatching { context.startActivity(intent) }
                .onFailure { shareText(context, data) }
        }
    )
    QsOutlinedButton("Done", onClick = onNavigateBack)
}

// ───────────── Calendar variant ─────────────

@Composable
private fun EventResult(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val parsed = remember(data) { com.dhanuk.quickscanpro.util.CalendarImporter.parse(data) }

    QsCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            IconBadge(Icons.Filled.CalendarMonth, size = 56.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                parsed?.title ?: "Calendar event",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (!parsed?.location.isNullOrEmpty()) {
                Text(parsed?.location ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            if (parsed != null) {
                val fmt = java.text.SimpleDateFormat("EEE, d MMM yyyy • h:mm a", java.util.Locale.getDefault())
                Text("Starts ${fmt.format(java.util.Date(parsed.startMs))}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    val event = parsed
    QsButton(
        text = "Add to calendar",
        icon = Icons.Filled.CalendarMonth,
        enabled = event != null,
        onClick = {
            if (event == null) return@QsButton
            val id = com.dhanuk.quickscanpro.util.CalendarImporter.importToCalendar(context, event)
            Toast.makeText(
                context,
                if (id != null) "Event added to calendar" else "Could not add event (no calendar app?)",
                Toast.LENGTH_SHORT
            ).show()
            if (id != null) com.dhanuk.quickscanpro.util.CalendarImporter.openCalendar(context, event.startMs)
        }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionTile("Copy", Icons.Filled.ContentCopy, filled = false, modifier = Modifier.weight(1f)) {
            copyToClipboard(context, data)
        }
        ActionTile("Share", Icons.Filled.Share, filled = false, modifier = Modifier.weight(1f)) {
            shareText(context, data)
        }
    }
    QsOutlinedButton("Done", onClick = onNavigateBack)
}

// ───────────── Small pieces ─────────────

@Composable
private fun ContactField(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, size = 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ActionTile(
    label: String,
    icon: ImageVector,
    filled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest
    val fg = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .then(
                if (!filled) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun SafetyBanner(report: LinkSafetyChecker.Report, onClick: () -> Unit) {
    val (color, title, subtitle) = when (report.level) {
        LinkSafetyChecker.Level.SAFE -> Triple(
            com.dhanuk.quickscanpro.ui.theme.Positive,
            "Safe link",
            "No threats detected by security check"
        )
        LinkSafetyChecker.Level.CAUTION -> Triple(
            com.dhanuk.quickscanpro.ui.theme.Caution,
            "Review before opening",
            "Some caution signals were found"
        )
        LinkSafetyChecker.Level.RISKY -> Triple(
            com.dhanuk.quickscanpro.ui.theme.Danger,
            "Risky link",
            "This link may be dangerous — open with care"
        )
        LinkSafetyChecker.Level.NOT_A_LINK -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.GppGood, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = color)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("${report.score}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun SafetyDialog(report: LinkSafetyChecker.Report, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        icon = { Icon(Icons.Filled.GppGood, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Safety report") },
        text = {
            Column {
                Text("Score: ${report.score}/100", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { report.score / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                if (report.signals.isEmpty()) {
                    Text("No suspicious signals detected.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Signals", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    report.signals.forEach { signal ->
                        Text("• $signal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(3.dp))
                    }
                }
            }
        }
    )
}
