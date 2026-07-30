package com.dhanuk.quickscanpro.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
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

/**
 * Result screen — clean professional layout:
 *  - Top bar with back, type badge, vault icon
 *  - White content card with the scanned data and translate hint
 *  - Horizontal scroll of contextual action pills
 *  - Link safety card with linear progress + signal list
 *  - Bottom action bar: Speak, Remind, Vault
 * All 15 unique features preserved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(data: String, onNavigateBack: () -> Unit, onNavigateToProduct: (String) -> Unit) {
    val context = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary
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
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                detectedType.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (incognitoMode) {
                            Icon(Icons.Filled.VisibilityOff, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
                    IconButton(onClick = { toggleVault(context, decodedData, historyViewModel, inVault) { inVault = !inVault } }) {
                        Icon(
                            if (inVault) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = "Vault",
                            tint = if (inVault) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
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
            // Content card
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        decodedData,
                        style = MaterialTheme.typography.bodyLarge,
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
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_PROCESS_TEXT, decodedData)
                                    }
                                    try {
                                        context.startActivity(Intent.createChooser(intent, "Translate"))
                                    } catch (e: Exception) {
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

            Spacer(Modifier.height(18.dp))

            // Actions
            Text("QUICK ACTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                buildActionPills(detectedType, decodedData, context, clipboardManager, onNavigateToProduct, accent)
            }

            Spacer(Modifier.height(18.dp))

            // Safety card
            if (safety.level != LinkSafetyChecker.Level.NOT_A_LINK) {
                SafetyCard(safety)
                Spacer(Modifier.height(18.dp))
            }

            // Bottom action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (isSpeaking) {
                            VoiceSpeaker.stop()
                            isSpeaking = false
                        } else {
                            VoiceSpeaker.speak(decodedData)
                            isSpeaking = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isSpeaking) "Stop" else "Speak")
                }
                OutlinedButton(
                    onClick = { showReminderSheet = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Alarm, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Remind")
                }
                Button(
                    onClick = { toggleVault(context, decodedData, historyViewModel, inVault) { inVault = !inVault } },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (inVault) Icons.Filled.LockOpen else Icons.Filled.Lock, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (inVault) "Unvault" else "Vault")
                }
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

private fun toggleVault(
    context: Context,
    decodedData: String,
    historyViewModel: HistoryViewModel,
    currentlyInVault: Boolean,
    onComplete: () -> Unit
) {
    val target = historyViewModel.history.value.firstOrNull { it.content == decodedData }
    if (target == null) return
    if (!currentlyInVault) {
        showBiometric(context) {
            historyViewModel.setVault(target, true, context)
            onComplete()
            Toast.makeText(context, "Moved to Secure Vault", Toast.LENGTH_SHORT).show()
        }
    } else {
        historyViewModel.setVault(target, false, context)
        onComplete()
        Toast.makeText(context, "Removed from Vault", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            Spacer(Modifier.height(12.dp))
            options.forEach { (label, millis) ->
                ListItem(
                    headlineContent = { Text(label) },
                    leadingContent = {
                        Icon(Icons.Filled.Schedule, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
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

@Composable
private fun SafetyCard(report: LinkSafetyChecker.Report) {
    val color = when (report.level) {
        LinkSafetyChecker.Level.SAFE -> SafetySafe
        LinkSafetyChecker.Level.CAUTION -> SafetyCaution
        LinkSafetyChecker.Level.RISKY -> SafetyRisky
        LinkSafetyChecker.Level.NOT_A_LINK -> SafetySafe
    }
    val statusText = when (report.level) {
        LinkSafetyChecker.Level.SAFE -> "Safe"
        LinkSafetyChecker.Level.CAUTION -> "Caution"
        LinkSafetyChecker.Level.RISKY -> "High Risk"
        else -> ""
    }
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(statusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = color, fontWeight = FontWeight.Bold)
                    Text("${report.score}/100 safety score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { report.score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(12.dp))
            report.signals.forEach { signal ->
                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                    Text("•", color = color, modifier = Modifier.padding(end = 8.dp),
                        fontWeight = FontWeight.Bold)
                    Text(signal, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
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

    add("Copy", Icons.Filled.ContentCopy) {
        clipboard.setText(androidx.compose.ui.text.AnnotatedString(content))
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }
    add("Share", Icons.Filled.Share) {
        val send = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, content)
            setType("text/plain")
        }
        context.startActivity(Intent.createChooser(send, "Share"))
    }

    when (type) {
        BarcodeTypeDetector.TYPE_URL -> {
            add("Open", Icons.Filled.OpenInNew) {
                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content))) }
                catch (e: Exception) { Toast.makeText(context, "Cannot open", Toast.LENGTH_SHORT).show() }
            }
        }
        BarcodeTypeDetector.TYPE_EMAIL -> {
            add("Email", Icons.Filled.Email) {
                val addr = if (content.startsWith("mailto:")) content.substring(7).substringBefore('?') else content
                try { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$addr"))) }
                catch (e: Exception) {}
            }
        }
        BarcodeTypeDetector.TYPE_PHONE -> {
            add("Call", Icons.Filled.Call) {
                val number = if (content.startsWith("tel:")) content.substring(4) else content
                try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) }
                catch (e: Exception) {}
            }
            add("WhatsApp", Icons.Filled.Message) {
                val number = content.removePrefix("tel:").replace(Regex("[^+0-9]"), "")
                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number"))) }
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
                    parseEventDate(event.start)?.let { putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
                    parseEventDate(event.end)?.let { putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, it) }
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
