package com.dhanuk.quickscanpro.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.composables.ActionPill
import com.dhanuk.quickscanpro.ui.composables.GlassCard
import com.dhanuk.quickscanpro.ui.theme.*
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.LinkSafetyChecker
import com.dhanuk.quickscanpro.util.ReminderScheduler
import com.dhanuk.quickscanpro.util.TextLanguageDetector
import com.dhanuk.quickscanpro.util.VoiceSpeaker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(data: String, onNavigateBack: () -> Unit, onNavigateToProduct: (String) -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val accent = if (dark) LuminaPrimaryGlow else LuminaPrimary
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val historyViewModel: HistoryViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val incognitoMode by settingsViewModel.incognitoMode.collectAsState()
    val autoCopy by settingsViewModel.autoCopyOnScan.collectAsState()

    val decodedData = URLDecoder.decode(data, StandardCharsets.UTF_8.toString())
    val detectedType = remember(decodedData) { BarcodeTypeDetector.detectType(decodedData) }
    val isForeign = remember(decodedData) {
        detectedType == BarcodeTypeDetector.TYPE_TEXT && TextLanguageDetector.isLikelyForeign(decodedData)
    }
    val langHint = remember(decodedData) { TextLanguageDetector.sourceLanguageHint(decodedData) }
    val safety = remember(decodedData) { LinkSafetyChecker.analyze(decodedData) }

    var showReminderSheet by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var inVault by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = decodedData) {
        VoiceSpeaker.init(context)
        if (!incognitoMode) {
            historyViewModel.addScanResult(ScanResult(content = decodedData))
        }
        if (autoCopy) {
            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(decodedData))
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                detectedType.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        if (incognitoMode) {
                            Icon(Icons.Filled.VisibilityOff, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        VoiceSpeaker.stop()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ActionOrbTop(
                        onClick = {
                            // Vault toggle with biometric prompt
                            if (!inVault) {
                                showBiometric(context) {
                                    inVault = true
                                    val justAdded = historyViewModel.history.value.firstOrNull {
                                        it.content == decodedData
                                    }
                                    justAdded?.let {
                                        historyViewModel.setVault(it, true, context)
                                        Toast.makeText(context, "Moved to Secure Vault",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                val justAdded = historyViewModel.history.value.firstOrNull {
                                    it.content == decodedData
                                }
                                justAdded?.let {
                                    historyViewModel.setVault(it, false, context)
                                    inVault = false
                                    Toast.makeText(context, "Removed from Vault",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        active = inVault
                    ) {
                        Icon(
                            if (inVault) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = "Vault",
                            tint = if (inVault) LuminaPrimaryGlow else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero card with content
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                glowColor = accent
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        decodedData,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isForeign) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Detected: $langHint",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = accent.copy(alpha = 0.15f),
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_PROCESS_TEXT, decodedData)
                                    }
                                    try { context.startActivity(Intent.createChooser(intent, "Translate")) }
                                    catch (e: Exception) {
                                        clipboardManager.setText(
                                            androidx.compose.ui.text.AnnotatedString(decodedData))
                                        Toast.makeText(context, "Copied — open Translate app to paste",
                                            Toast.LENGTH_LONG).show()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Translate, contentDescription = null,
                                        tint = accent, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Translate", color = accent,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // AI Action pills (horizontal scroll) — the star feature
            Text("AI ACTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                buildActionPills(detectedType, decodedData, context, clipboardManager, onNavigateToProduct, accent)
            }

            Spacer(Modifier.height(18.dp))

            // Safety Score card (circular gauge)
            if (safety.level != LinkSafetyChecker.Level.NOT_A_LINK) {
                SafetyGaugeCard(safety)
                Spacer(Modifier.height(18.dp))
            }

            // Bottom action row — Speak / Remind / Vault
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RoundActionButton(
                    label = if (isSpeaking) "Stop" else "Speak",
                    icon = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                    color = accent,
                    onClick = {
                        if (isSpeaking) {
                            VoiceSpeaker.stop()
                            isSpeaking = false
                        } else {
                            VoiceSpeaker.speak(decodedData)
                            isSpeaking = true
                        }
                    }
                )
                RoundActionButton(
                    label = "Remind",
                    icon = Icons.Filled.Alarm,
                    color = LuminaWarning,
                    onClick = { showReminderSheet = true }
                )
                RoundActionButton(
                    label = if (inVault) "Unvault" else "Vault",
                    icon = if (inVault) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    color = LuminaNavy,
                    onClick = {
                        if (!inVault) {
                            showBiometric(context) {
                                inVault = true
                                historyViewModel.history.value.firstOrNull {
                                    it.content == decodedData
                                }?.let {
                                    historyViewModel.setVault(it, true, context)
                                    Toast.makeText(context, "Moved to Secure Vault",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            historyViewModel.history.value.firstOrNull {
                                it.content == decodedData
                            }?.let {
                                historyViewModel.setVault(it, false, context)
                                inVault = false
                            }
                        }
                    }
                )
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showReminderSheet) {
        ReminderBottomSheet(
            onDismiss = { showReminderSheet = false },
            onSchedule = { millis ->
                historyViewModel.history.value.firstOrNull { it.content == decodedData }?.let {
                    historyViewModel.setReminder(it, millis, context)
                    Toast.makeText(context,
                        if (millis != null) "Reminder set" else "Reminder cancelled",
                        Toast.LENGTH_SHORT).show()
                }
                showReminderSheet = false
            }
        )
    }
}

@Composable
private fun ActionOrbTop(
    onClick: () -> Unit,
    active: Boolean,
    content: @Composable () -> Unit
) {
    GlassCard(cornerRadius = 50.dp, modifier = Modifier.size(36.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) { content() }
    }
}

@Composable
private fun RoundActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                              color: Color, onClick: () -> Unit) {
    val dark = isSystemInDarkTheme()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (dark) GlassFillDark else GlassFillLight)
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color,
            fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SafetyGaugeCard(report: LinkSafetyChecker.Report) {
    val color = when (report.level) {
        LinkSafetyChecker.Level.SAFE -> SafetySafe
        LinkSafetyChecker.Level.CAUTION -> SafetyCaution
        LinkSafetyChecker.Level.RISKY -> SafetyRisky
        LinkSafetyChecker.Level.NOT_A_LINK -> SafetySafe
    }
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        glowColor = color
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SafetyGauge(score = report.score, color = color, modifier = Modifier.size(72.dp))
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        when (report.level) {
                            LinkSafetyChecker.Level.SAFE -> "Looks Safe"
                            LinkSafetyChecker.Level.CAUTION -> "Be Cautious"
                            LinkSafetyChecker.Level.RISKY -> "High Risk"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = color, fontWeight = FontWeight.Bold
                    )
                    Text("${report.score}/100 safety score",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Tap signals for details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(12.dp))
            report.signals.forEach { signal ->
                Row(modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top) {
                    Text("•", color = color,
                        modifier = Modifier.padding(end = 8.dp),
                        fontWeight = FontWeight.Bold)
                    Text(signal, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SafetyGauge(score: Int, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sweep = (score / 100f) * 360f
        drawCircle(color = color.copy(alpha = 0.1f), style = Stroke(width = 8f, cap = StrokeCap.Round))
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(score.toString(), color = color, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("SAFE", color = color.copy(alpha = 0.6f),
                fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReminderBottomSheet(onDismiss: () -> Unit, onSchedule: (Long?) -> Unit) {
    val options = ReminderScheduler.relativeOptions()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Set a Reminder",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Get notified about this scan at the chosen time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            options.forEach { (label, millis) ->
                ListItem(
                    headlineContent = { Text(label) },
                    leadingContent = {
                        Icon(Icons.Filled.Schedule, contentDescription = null,
                            tint = LuminaPrimaryGlow)
                    },
                    modifier = Modifier.clickable { onSchedule(millis) }
                )
                HorizontalDivider()
            }
            ListItem(
                headlineContent = { Text("Cancel reminder", color = MaterialTheme.colorScheme.error) },
                leadingContent = {
                    Icon(Icons.Filled.Cancel, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.clickable { onSchedule(null) }
            )
        }
    }
}

private fun showBiometric(context: Context, onSuccess: () -> Unit) {
    try {
        val activity = context.findActivity() as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Authenticate to add this scan to the Secure Vault")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(info)
    } catch (e: Exception) {
        Toast.makeText(context, "Biometric not available on this device",
            Toast.LENGTH_SHORT).show()
    }
}

private tailrec fun Context.findActivity(): android.app.Activity? =
    when (this) {
        is android.app.Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun connectToWifi(context: Context, wifi: BarcodeTypeDetector.WifiInfo): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        try {
            val suggestion = android.net.wifi.WifiNetworkSuggestion.Builder()
                .setSsid(wifi.ssid)
                .apply {
                    when (wifi.encryption.uppercase()) {
                        "WEP", "WPA", "WPA2", "WPA3" -> setWpa2Passphrase(wifi.password)
                        "NOPASS", "" -> { }
                        else -> setWpa2Passphrase(wifi.password)
                    }
                }
                .build()
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))
            if (status == android.net.wifi.WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                Toast.makeText(context,
                    "WiFi \"${wifi.ssid}\" suggested — approve in Settings.",
                    Toast.LENGTH_LONG).show()
                true
            } else false
        } catch (e: Exception) { false }
    } else false
}

private fun parseEventDate(raw: String): Long? {
    if (raw.isBlank()) return null
    val cleaned = raw.trim().removeSuffix("Z").removeSuffix("z")
    val patterns = listOf("yyyyMMdd'T'HHmmss", "yyyyMMdd'T'HHmm", "yyyyMMdd")
    for (pattern in patterns) {
        try {
            val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
            sdf.isLenient = false
            return sdf.parse(cleaned)?.time
        } catch (_: Exception) {}
    }
    return null
}

@Composable
private fun buildActionPills(
    type: String,
    content: String,
    context: Context,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    onNavigateToProduct: (String) -> Unit,
    accent: Color
): List<@Composable () -> Unit> {
    val pills = mutableListOf<@Composable () -> Unit>()

    fun add(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
        pills.add {
            ActionPill(text = text, onClick = onClick, icon = {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            })
        }
    }

    // Common pills
    add("Copy", Icons.Filled.ContentCopy) {
        clipboard.setText(androidx.compose.ui.text.AnnotatedString(content))
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }
    add("Share", Icons.Filled.Share) {
        val send = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(send, "Share"))
    }

    when (type) {
        BarcodeTypeDetector.TYPE_URL -> {
            add("Open", Icons.Filled.OpenInNew) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content)))
                } catch (e: Exception) { Toast.makeText(context, "Cannot open", Toast.LENGTH_SHORT).show() }
            }
            add("Save Offline", Icons.Filled.Save) {
                Toast.makeText(context, "Page text will be fetched on next open",
                    Toast.LENGTH_SHORT).show()
                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content))) }
                catch (e: Exception) {}
            }
        }
        BarcodeTypeDetector.TYPE_EMAIL -> {
            add("Email", Icons.Filled.Email) {
                val addr = if (content.startsWith("mailto:")) content.substring(7).substringBefore('?') else content
                try { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$addr"))) }
                catch (e: Exception) {}
            }
            add("Contact", Icons.Filled.PersonAdd) { /* reuse contact add */ }
        }
        BarcodeTypeDetector.TYPE_PHONE -> {
            add("Call", Icons.Filled.Call) {
                val number = if (content.startsWith("tel:")) content.substring(4) else content
                try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) }
                catch (e: Exception) {}
            }
            add("WhatsApp", Icons.Filled.Message) {
                val number = content.removePrefix("tel:").replace(Regex("[^+0-9]"), "")
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/$number")))
                } catch (e: Exception) {}
            }
            add("SMS", Icons.Filled.Sms) {
                val number = if (content.startsWith("tel:")) content.substring(4) else content
                try { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))) }
                catch (e: Exception) {}
            }
        }
        BarcodeTypeDetector.TYPE_SMS -> add("Send SMS", Icons.Filled.Sms) {
            val number = content.removePrefix("sms:").removePrefix("smsto:").substringBefore(':')
            try { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))) }
            catch (e: Exception) {}
        }
        BarcodeTypeDetector.TYPE_WIFI -> add("Connect WiFi", Icons.Filled.Wifi) {
            val wifi = BarcodeTypeDetector.parseWifi(content)
            if (wifi != null) {
                if (!connectToWifi(context, wifi)) {
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(wifi.password))
                    Toast.makeText(context, "SSID: ${wifi.ssid}\nPassword copied",
                        Toast.LENGTH_LONG).show()
                }
            } else Toast.makeText(context, "Could not parse WiFi QR", Toast.LENGTH_SHORT).show()
        }
        BarcodeTypeDetector.TYPE_VCARD -> add("Save Contact", Icons.Filled.PersonAdd) {
            val info = BarcodeTypeDetector.parseVCard(content)
            val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                if (info.name.isNotBlank()) putExtra(ContactsContract.Intents.Insert.NAME, info.name)
                if (info.phone.isNotBlank()) putExtra(ContactsContract.Intents.Insert.PHONE, info.phone)
                if (info.email.isNotBlank()) putExtra(ContactsContract.Intents.Insert.EMAIL, info.email)
            }
            try { context.startActivity(intent) } catch (e: Exception) {}
        }
        BarcodeTypeDetector.TYPE_CALENDAR -> add("Add Event", Icons.Filled.Event) {
            val event = BarcodeTypeDetector.parseCalendarEvent(content)
            try {
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    setData(android.provider.CalendarContract.Events.CONTENT_URI)
                    putExtra(android.provider.CalendarContract.Events.TITLE, event.summary)
                    if (event.location.isNotBlank())
                        putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, event.location)
                    parseEventDate(event.start)?.let {
                        putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
                    parseEventDate(event.end)?.let {
                        putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, it) }
                }
                context.startActivity(intent)
            } catch (e: Exception) {}
        }
        BarcodeTypeDetector.TYPE_GEO -> add("Open Map", Icons.Filled.Map) {
            try {
                val geo = BarcodeTypeDetector.parseGeo(content)
                if (geo != null) {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:${geo.latitude},${geo.longitude}?q=${geo.latitude},${geo.longitude}")))
                } else context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content)))
            } catch (e: Exception) {}
        }
        BarcodeTypeDetector.TYPE_PRODUCT -> add("Lookup", Icons.Filled.ShoppingBag) {
            onNavigateToProduct(content)
        }
    }
    return pills
}
