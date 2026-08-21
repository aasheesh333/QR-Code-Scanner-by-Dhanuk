package com.dhanuk.quickscanpro.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.qrgenerator.QRCodeGenerator
import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import com.dhanuk.quickscanpro.util.WifiShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WifiShareScreen(
    onNavigateBack: () -> Unit,
    onShareReady: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Detection may succeed only after the user grants permission / enables
    // location / connects to Wi-Fi — so re-check whenever we resume this screen.
    var current by remember { mutableStateOf(WifiShareHelper.getCurrentWifi(context)) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                current = WifiShareHelper.getCurrentWifi(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var ssid by rememberSaveable { mutableStateOf(current?.ssid ?: "") }
    var password by rememberSaveable { mutableStateOf("") }
    var security by rememberSaveable { mutableStateOf("WPA") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var generating by remember { mutableStateOf(false) }
    var autoFilledSsid by remember { mutableStateOf<String?>(null) }

    // Auto-fill the SSID when detection succeeds, but never overwrite what the
    // user has typed. Only fill when the field is empty or still showing the
    // previously auto-filled value.
    androidx.compose.runtime.LaunchedEffect(current) {
        val c = current
        if (c != null && (ssid.isBlank() || autoFilledSsid == ssid)) {
            ssid = c.ssid
            autoFilledSsid = c.ssid
        }
    }

    val locationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fineGranted = grants[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = grants[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            when {
                !WifiShareHelper.isLocationServicesOn(context) -> {
                    Toast.makeText(context, "Please turn on location so the network name can be detected", Toast.LENGTH_LONG).show()
                    WifiShareHelper.openLocationSettings(context)
                }
                else -> {
                    val info = WifiShareHelper.getCurrentWifi(context)
                    current = info
                    if (info != null) {
                        if (ssid.isBlank() || autoFilledSsid == ssid) {
                            ssid = info.ssid
                            autoFilledSsid = info.ssid
                        }
                        Toast.makeText(context, "Using connected network: ${info.ssid}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Network not detected — type the network name below", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Location permission is needed to read the connected network", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Share Wi-Fi", style = MaterialTheme.typography.titleLarge) },
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
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            val net = current
            val wifiEnabled = WifiShareHelper.isWifiEnabled(context)

            QsCard {
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.Wifi)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                when {
                                    net != null -> "Connected to ${net.ssid}"
                                    else -> "No network detected"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                when {
                                    net != null ->
                                        "Fill in the password once, then your guests can scan the QR to join instantly."
                                    !WifiShareHelper.hasLocationPermission(context) ->
                                        "Detecting your network needs location permission — the network password itself is never readable by any app, so type it once below."
                                    !WifiShareHelper.isLocationServicesOn(context) ->
                                        "Turn on location services to auto-detect your network (Android requires it to reveal the network name)."
                                    !wifiEnabled ->
                                        "Wi-Fi is off or nothing is connected. Type the network name below instead."
                                    else ->
                                        "Your device hides network names from apps. Type the network name below instead — it's printed on your router's sticker."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    QsOutlinedButton(
                        text = "Use connected network",
                        icon = Icons.Filled.Wifi,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (!WifiShareHelper.hasLocationPermission(context)) {
                                locationLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else if (!WifiShareHelper.isLocationServicesOn(context)) {
                                Toast.makeText(context, "Please turn on location so the network name can be detected", Toast.LENGTH_LONG).show()
                                WifiShareHelper.openLocationSettings(context)
                            } else {
                                val info = WifiShareHelper.getCurrentWifi(context)
                                current = info
                                if (info != null) {
                                    if (ssid.isBlank() || autoFilledSsid == ssid) {
                                        ssid = info.ssid
                                        autoFilledSsid = info.ssid
                                    }
                                    Toast.makeText(context, "Using connected network: ${info.ssid}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Network not detected — type the network name below", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }

            Column {
                SectionLabel("Network details")
                QsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ssid,
                            onValueChange = { ssid = it },
                            label = { Text("Network name (SSID)") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            supportingText = if (security != "NOPASS" && password.isBlank()) {
                                { Text("Required for secured networks") }
                            } else null,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        var securityMenuOpen by remember { mutableStateOf(false) }
                        val securityLabel = when (security) {
                            "WEP" -> "WEP"
                            "NOPASS" -> "None (open)"
                            else -> "WPA/WPA2/WPA3"
                        }
                        OutlinedTextField(
                            value = securityLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Security") },
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { securityMenuOpen = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose security type")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = securityMenuOpen,
                            onDismissRequest = { securityMenuOpen = false }
                        ) {
                            listOf("WPA/WPA2/WPA3", "WEP", "None (open)").forEach { label ->
                                val value = when (label) {
                                    "WEP" -> "WEP"
                                    "None (open)" -> "NOPASS"
                                    else -> "WPA"
                                }
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        security = value
                                        securityMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            QsButton(
                text = if (generating) "Generating…" else "Generate Wi-Fi QR",
                icon = Icons.Filled.QrCode2,
                enabled = ssid.isNotBlank() && !generating &&
                    (security == "NOPASS" || password.isNotBlank()),
                onClick = {
                    val content = QRContentBuilder.buildWifi(ssid, password, security)
                    generating = true
                    scope.launch {
                        val bmp = withContext(Dispatchers.IO) {
                            QRCodeGenerator.generate(
                                content = content,
                                size = 512,
                                foregroundColor = 0xFF000000.toInt(),
                                backgroundColor = 0xFFFFFFFF.toInt()
                            )
                        }
                        generating = false
                        if (bmp != null) {
                            bitmap = bmp
                            onShareReady(content)
                        } else {
                            bitmap = null
                            Toast.makeText(context, "Could not generate the QR — please retry", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            val bmp = bitmap
            if (bmp != null) {
                QsCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Wi-Fi QR code",
                                modifier = Modifier.fillMaxSize().padding(14.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Guests scan this to join \"$ssid\"",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QsOutlinedButton(
                        text = "Save",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val safeName = ssid.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40).ifBlank { "network" }
                            scope.launch {
                                val ok = withContext(Dispatchers.IO) {
                                    QRCodeGenerator.saveToGallery(context, bmp, "wifi_${safeName}_${System.currentTimeMillis()}")
                                }
                                Toast.makeText(context, if (ok) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    QsOutlinedButton(
                        text = "Share",
                        modifier = Modifier.weight(1f),
                        onClick = { QRCodeGenerator.shareQrBitmap(context, bmp) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

