package com.dhanuk.quickscanpro.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.BuildConfig
import com.dhanuk.quickscanpro.config.AppConfig
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import com.dhanuk.quickscanpro.viewmodel.ThemeViewModel
import com.dhanuk.quickscanpro.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToThemeStudio: () -> Unit
) {
    val settingsVm: SettingsViewModel = viewModel()
    val historyVm: HistoryViewModel = viewModel()
    val themeVm: ThemeViewModel = viewModel()
    val sound by settingsVm.soundEnabled.collectAsState()
    val vibrate by settingsVm.vibrateEnabled.collectAsState()
    val autoSave by settingsVm.autoCopyOnScan.collectAsState()
    val biometrics by settingsVm.biometricLock.collectAsState()
    val themeMode by themeVm.themeMode.collectAsState()
    var pushEnabled by rememberSaveable { mutableStateOf(false) }
    val defaultAction by settingsVm.defaultAction.collectAsState()
    val context = LocalContext.current
    var showDefaultActionDialog by rememberSaveable { mutableStateOf(false) }
    var showClearConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }

    AppBackground()
    Scaffold(
        topBar = { SettingsHeader(onNavigateBack = onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                SettingsGroup(label = "General") {
                    SubvalueRow(
                        icon = Icons.Filled.RocketLaunch,
                        title = "Default action after scan",
                        trailing = defaultActionLabel(defaultAction),
                        onClick = { showDefaultActionDialog = true }
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.Palette,
                        title = "Theme",
                        trailing = themeMode.label,
                        onClick = { showThemeDialog = true }
                    )
                    GroupDivider()
                    ToggleRow(
                        icon = Icons.Filled.VolumeUp,
                        title = "Beep on scan",
                        checked = sound,
                        onChange = { settingsVm.setSound(it) }
                    )
                    GroupDivider()
                    ToggleRow(
                        icon = Icons.Filled.Vibration,
                        title = "Vibrate on scan",
                        checked = vibrate,
                        onChange = { settingsVm.setVibrate(it) }
                    )
                    GroupDivider()
                    ToggleRow(
                        icon = Icons.Filled.Save,
                        title = "Auto-save scans",
                        checked = autoSave,
                        onChange = { settingsVm.setAutoCopy(it) }
                    )
                }
                Spacer(Modifier.height(24.dp))
                SettingsGroup(label = "Privacy & Security") {
                    ToggleRow(
                        icon = Icons.Filled.Fingerprint,
                        title = "Lock app with biometrics",
                        checked = biometrics,
                        onChange = { settingsVm.setBiometricLock(it) }
                    )
                }
                Spacer(Modifier.height(24.dp))
                SettingsGroup(label = "Data") {
                    val error = MaterialTheme.colorScheme.error
                    val errorContainer = MaterialTheme.colorScheme.errorContainer
                    DangerRow(
                        icon = Icons.Filled.DeleteForever,
                        title = "Clear all history",
                        error = error,
                        errorContainer = errorContainer,
                        onClick = { showClearConfirmDialog = true }
                    )
                }
                Spacer(Modifier.height(24.dp))
                SettingsGroup(label = "Notifications") {
                    ToggleRow(
                        icon = Icons.Filled.Notifications,
                        title = "Push notifications",
                        subtitle = "Powered by OneSignal",
                        checked = pushEnabled,
                        onChange = { enabled ->
                            pushEnabled = enabled
                            initOneSignalStub(enabled)
                        }
                    )
                }
                Spacer(Modifier.height(24.dp))
                SettingsGroup(label = "Support") {
                    SubvalueRow(
                        icon = Icons.Filled.Info,
                        title = "About",
                        onClick = onNavigateToAbout
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.Palette,
                        title = "Theme Studio",
                        onClick = onNavigateToThemeStudio
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.Star,
                        title = "Rate App",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PLAY_STORE_URL))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "No app to open Play Store", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.Share,
                        title = "Share App",
                        onClick = {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "QuickScan Pro")
                                    putExtra(Intent.EXTRA_TEXT, "Check out QuickScan Pro on the Play Store: ${AppConfig.PLAY_STORE_URL}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            } catch (_: Exception) {
                                Toast.makeText(context, "No app to share", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.Shield,
                        title = "Privacy Policy",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.Legal.PRIVACY_POLICY))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.Description,
                        title = "Terms of Use",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.Legal.TERMS))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.ContactPage,
                        title = "Contact Us",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.Legal.CONTACT_US))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showDefaultActionDialog) {
        DefaultActionDialog(
            current = defaultAction,
            onSelect = { settingsVm.setDefaultAction(it) },
            onDismiss = { showDefaultActionDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear all history?") },
            text = { Text("This will permanently delete all scan results. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    historyVm.deleteAll()
                    showClearConfirmDialog = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showThemeDialog) {
        ThemeModeDialog(
            current = themeMode,
            onSelect = { themeVm.setThemeMode(it); showThemeDialog = false },
            onDismiss = { showThemeDialog = false }
        )
    }
}

private fun defaultActionLabel(action: String): String = when (action) {
    "show_result" -> "Show result only"
    "open_url" -> "Open URL automatically"
    "copy_clipboard" -> "Copy to clipboard"
    "share" -> "Share result"
    else -> "Show result only"
}

@Composable
private fun DefaultActionDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        "show_result" to "Show result only",
        "open_url" to "Open URL automatically",
        "copy_clipboard" to "Copy to clipboard",
        "share" to "Share result"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Default action after scan") },
        text = {
            Column {
                options.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(key)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == key,
                            onClick = {
                                onSelect(key)
                                onDismiss()
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private var oneSignalInitStubbed = false

private fun initOneSignalStub(enabled: Boolean) {
    if (enabled && !oneSignalInitStubbed) {
        oneSignalInitStubbed = true
    }
}

@Composable
private fun ThemeModeDialog(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Theme") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(mode)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == mode,
                            onClick = { onSelect(mode) }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(mode.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SettingsHeader(onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun SettingsGroup(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange
        )
    }
}

@Composable
private fun SubvalueRow(
    icon: ImageVector,
    title: String,
    trailing: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun DangerRow(
    icon: ImageVector,
    title: String,
    error: Color,
    errorContainer: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(errorContainer.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = error
        )
    }
}
