package com.dhanuk.quickscanpro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.BuildConfig
import com.dhanuk.quickscanpro.config.AppConfig
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import com.dhanuk.quickscanpro.ui.design.SettingInfoRow
import com.dhanuk.quickscanpro.ui.design.SettingNavRow
import com.dhanuk.quickscanpro.ui.design.SettingToggleRow
import com.dhanuk.quickscanpro.util.VaultAuth
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToThemeStudio: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToContact: () -> Unit = {}
) {
    val settingsVm: SettingsViewModel = viewModel()
    val historyVm: HistoryViewModel = viewModel()
    val context = LocalContext.current

    val sound by settingsVm.soundEnabled.collectAsState()
    val vibrate by settingsVm.vibrateEnabled.collectAsState()
    val autoCopy by settingsVm.autoCopyOnScan.collectAsState()
    val vaultLockMode by settingsVm.vaultLockMode.collectAsState()
    val defaultAction by settingsVm.defaultAction.collectAsState()
    val scanHistory by settingsVm.scanHistory.collectAsState()
    val incognito by settingsVm.incognitoMode.collectAsState()

    var showActionDialog by rememberSaveable { mutableStateOf(false) }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var showVaultLockDialog by rememberSaveable { mutableStateOf(false) }
    var showNoDeviceLockDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            Column {
                SectionLabel("Scanning")
                Group {
                    SettingNavRow(
                        Icons.Filled.SwapVert,
                        "Default action after scan",
                        subtitle = actionLabel(defaultAction)
                    ) { showActionDialog = true }
                    DividerLine()
                    SettingToggleRow(Icons.Filled.VolumeUp, "Scan sound", checked = sound) { settingsVm.setSound(it) }
                    DividerLine()
                    SettingToggleRow(Icons.Filled.Vibration, "Vibration", checked = vibrate) { settingsVm.setVibrate(it) }
                }
            }

            Column {
                SectionLabel("Productivity")
                Group {
                    SettingToggleRow(
                        Icons.Filled.Save,
                        "Auto-copy scans",
                        subtitle = "Copy every result to the clipboard instantly",
                        checked = autoCopy
                    ) { settingsVm.setAutoCopy(it) }
                    DividerLine()
                    SettingToggleRow(
                        Icons.Filled.History,
                        "Keep scan history",
                        subtitle = "Turn off to never store scans",
                        checked = scanHistory
                    ) { settingsVm.setScanHistory(it) }
                    DividerLine()
                SettingToggleRow(
                    Icons.Filled.VisibilityOff,
                    "Incognito mode",
                        subtitle = "Don't log this session",
                        checked = incognito
                    ) { settingsVm.setIncognito(it) }
                }
            }

            Column {
                SectionLabel("Privacy & security")
                Group {
                    SettingNavRow(
                        Icons.Filled.Fingerprint,
                        "Vault lock",
                        subtitle = vaultLockLabel(vaultLockMode)
                    ) { showVaultLockDialog = true }
                    DividerLine()
                    SettingNavRow(Icons.Filled.Lock, "Secure vault") { onNavigateToVault() }
                    DividerLine()
                    SettingNavRow(Icons.Filled.Shield, "Permissions explained") { onNavigateToPermissions() }
                    DividerLine()
                    SettingNavRow(
                        Icons.Filled.DeleteForever,
                        "Clear all history",
                        danger = true
                    ) { showClearDialog = true }
                }
            }

            Column {
                SectionLabel("Appearance")
                Group {
                    SettingNavRow(
                        Icons.Filled.DarkMode,
                        "Theme Studio",
                        subtitle = "Light, dark or AMOLED"
                    ) { onNavigateToThemeStudio() }
                }
            }

            Column {
                SectionLabel("Support & legal")
                Group {
                    SettingNavRow(Icons.Filled.Policy, "Privacy policy") { onNavigateToPrivacy() }
                    DividerLine()
                    SettingNavRow(Icons.Filled.Description, "Terms of use") { onNavigateToTerms() }
                    DividerLine()
                    SettingNavRow(
                        Icons.Filled.Email,
                        "Contact us",
                        subtitle = AppConfig.SUPPORT_EMAIL
                    ) { onNavigateToContact() }
                    DividerLine()
                    SettingNavRow(Icons.Filled.Star, "Rate QuickScan Pro") {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PLAY_STORE_URL)))
                        }
                    }
                    DividerLine()
                    SettingNavRow(Icons.Filled.Share, "Share the app") {
                        runCatching {
                            context.startActivity(Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "QuickScan Pro — ${AppConfig.PLAY_STORE_URL}")
                                },
                                "Share via"
                            ))
                        }
                    }
                    DividerLine()
                    SettingNavRow(Icons.Filled.Info, "About QuickScan Pro") { onNavigateToAbout() }
                }
            }

            SettingInfoRow(Icons.Filled.Info, "Version", BuildConfig.VERSION_NAME)
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showActionDialog) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Default action after scan") },
            text = {
                Column {
                    ACTIONS.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsVm.setDefaultAction(key)
                                    showActionDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = defaultAction == key, onClick = {
                                settingsVm.setDefaultAction(key)
                                showActionDialog = false
                            })
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showActionDialog = false }) { Text("Cancel") } }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all history?") },
            text = { Text("All saved scans will be permanently deleted. Items locked in your vault are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    historyVm.deleteAll()
                    showClearDialog = false
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }

    if (showVaultLockDialog) {
        AlertDialog(
            onDismissRequest = { showVaultLockDialog = false },
            title = { Text("Vault lock method") },
            text = {
                Column {
                    listOf(
                        "none" to ("No lock" to null),
                        "device" to ("Phone lock" to "Fingerprint, face, PIN or pattern")
                    ).forEach { (value, labelInfo) ->
                        val (label, subtitle) = labelInfo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (value == "device" && !VaultAuth.hasDeviceLock(context)) {
                                        showVaultLockDialog = false
                                        showNoDeviceLockDialog = true
                                    } else {
                                        settingsVm.setVaultLockMode(value)
                                        showVaultLockDialog = false
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = vaultLockMode == value, onClick = {
                                if (value == "device" && !VaultAuth.hasDeviceLock(context)) {
                                    showVaultLockDialog = false
                                    showNoDeviceLockDialog = true
                                } else {
                                    settingsVm.setVaultLockMode(value)
                                    showVaultLockDialog = false
                                }
                            })
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(label)
                                if (subtitle != null) {
                                    Text(
                                        subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showVaultLockDialog = false }) { Text("Cancel") } }
        )
    }

    if (showNoDeviceLockDialog) {
        AlertDialog(
            onDismissRequest = { showNoDeviceLockDialog = false },
            title = { Text("No screen lock on this device") },
            text = { Text("To protect your vault, your phone needs a screen lock (PIN, pattern, password or biometrics). Please set one in your device's security settings, then try again.") },
            confirmButton = {
                TextButton(onClick = {
                    showNoDeviceLockDialog = false
                    VaultAuth.openSecuritySettings(context)
                }) { Text("Open settings") }
            },
            dismissButton = { TextButton(onClick = { showNoDeviceLockDialog = false }) { Text("Cancel") } }
        )
    }
}

private fun vaultLockLabel(mode: String): String = when (mode) {
    "device" -> "Phone lock"
    else -> "No lock"
}

private val ACTIONS = listOf(
    "show_result" to "Show result page",
    "open_url" to "Open URL / act instantly",
    "copy_clipboard" to "Copy to clipboard",
    "share" to "Share"
)

private fun actionLabel(action: String) = ACTIONS.firstOrNull { it.first == action }?.second ?: "Show result page"

@Composable
private fun Group(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        content()
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp),
        thickness = 0.6.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
