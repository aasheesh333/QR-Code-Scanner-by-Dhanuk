package com.dhanuk.quickscanpro.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.composables.SmartActionCard
import com.dhanuk.quickscanpro.ui.composables.SmartActionFactory
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(data: String, onNavigateBack: () -> Unit, onNavigateToProduct: (String) -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val historyViewModel: HistoryViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val incognitoMode by settingsViewModel.incognitoMode.collectAsState()
    val autoCopy by settingsViewModel.autoCopyOnScan.collectAsState()

    val decodedData = URLDecoder.decode(data, StandardCharsets.UTF_8.toString())
    val detectedType = remember(decodedData) { BarcodeTypeDetector.detectType(decodedData) }

    LaunchedEffect(key1 = decodedData) {
        if (!incognitoMode) {
            historyViewModel.addScanResult(ScanResult(content = decodedData))
        }
        if (autoCopy) {
            clipboardManager.setText(AnnotatedString(decodedData))
        }
    }

    val smartActions = SmartActionFactory.actionsForType(
        type = detectedType,
        content = decodedData,
        onOpen = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(decodedData))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
            }
        },
        onCopy = {
            clipboardManager.setText(AnnotatedString(decodedData))
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
        },
        onShare = {
            val send = Intent(Intent.ACTION_SEND).apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, decodedData)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(send, "Share"))
        },
        onCall = {
            val number = when {
                decodedData.startsWith("tel:") -> decodedData.substring(4)
                else -> decodedData
            }
            try {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot place call", Toast.LENGTH_SHORT).show()
            }
        },
        onEmail = {
            val addr = when {
                decodedData.startsWith("mailto:") -> decodedData.substring(7).substringBefore('?')
                else -> decodedData
            }
            try {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$addr")))
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot send email", Toast.LENGTH_SHORT).show()
            }
        },
        onSMS = {
            val raw = decodedData
            val number = when {
                raw.startsWith("sms:") -> raw.substring(4).substringBefore(':')
                raw.startsWith("smsto:") -> raw.substring(6).substringBefore(':')
                else -> raw
            }
            try {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")))
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot send SMS", Toast.LENGTH_SHORT).show()
            }
        },
        onAddContact = {
            val phone: String
            val name: String
            val email: String
            if (detectedType == BarcodeTypeDetector.TYPE_VCARD) {
                val info = BarcodeTypeDetector.parseVCard(decodedData)
                name = info.name
                phone = info.phone
                email = info.email
            } else {
                name = ""
                phone = if (detectedType == BarcodeTypeDetector.TYPE_PHONE) decodedData else ""
                email = if (detectedType == BarcodeTypeDetector.TYPE_EMAIL) decodedData else ""
            }
            val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                if (name.isNotBlank()) putExtra(ContactsContract.Intents.Insert.NAME, name)
                if (phone.isNotBlank()) putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                if (email.isNotBlank()) putExtra(ContactsContract.Intents.Insert.EMAIL, email)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot add contact", Toast.LENGTH_SHORT).show()
            }
        },
        onConnectWifi = {
            val wifi = BarcodeTypeDetector.parseWifi(decodedData)
            if (wifi != null) {
                val msg = "SSID: ${wifi.ssid}\nPassword copied to clipboard"
                clipboardManager.setText(AnnotatedString(wifi.password))
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Could not parse WiFi QR", Toast.LENGTH_SHORT).show()
            }
        },
        onAddCalendar = {
            Toast.makeText(context, "Calendar event data detected. Add via Calendar app.", Toast.LENGTH_LONG).show()
        },
        onOpenMap = {
            try {
                val geo = BarcodeTypeDetector.parseGeo(decodedData)
                if (geo != null) {
                    val uri = Uri.parse("geo:${geo.latitude},${geo.longitude}?q=${geo.latitude},${geo.longitude}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(decodedData)))
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open maps", Toast.LENGTH_SHORT).show()
            }
        },
        onLookupProduct = {
            onNavigateToProduct(decodedData)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Result") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Chip(detectedType)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = decodedData,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (incognitoMode) {
                Spacer(Modifier.height(8.dp))
                Chip("Incognito", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                smartActions.forEach { action ->
                    SmartActionCard(action = action)
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
