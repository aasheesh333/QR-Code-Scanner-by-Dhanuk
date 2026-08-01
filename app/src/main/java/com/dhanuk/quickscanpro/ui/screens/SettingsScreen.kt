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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.BuildConfig
import com.dhanuk.quickscanpro.config.AppConfig
import com.dhanuk.quickscanpro.ui.theme.ThemeMode
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import com.dhanuk.quickscanpro.viewmodel.ThemeViewModel

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
    val defaultAction by settingsVm.defaultAction.collectAsState()
    var pushEnabled by rememberSaveable { mutableStateOf(false) }
    var showDefaultActionDialog by rememberSaveable { mutableStateOf(false) }
    var showClearConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SettingsHeader(onNavigateBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            SettingsGroup("General") {
                SettingsRow(Icons.Filled.RocketLaunch, "Default action after scan", trailing = defaultActionLabel(defaultAction), onClick = { showDefaultActionDialog = true })
                GroupDivider()
                SettingsRow(Icons.Filled.Palette, "Theme", trailing = themeMode.label, onClick = { showThemeDialog = true })
                GroupDivider()
                ToggleRow(Icons.Filled.VolumeUp, "Beep on scan", sound, onChange = { enabled -> settingsVm.setSound(enabled) })
                GroupDivider()
                ToggleRow(Icons.Filled.Vibration, "Vibrate on scan", vibrate, onChange = { enabled -> settingsVm.setVibrate(enabled) })
                GroupDivider()
                ToggleRow(Icons.Filled.Save, "Auto-save scans", autoSave, onChange = { enabled -> settingsVm.setAutoCopy(enabled) })
            }
            SettingsGroup("Privacy & Security") {
                ToggleRow(Icons.Filled.Fingerprint, "Lock app with biometrics", biometrics, onChange = { enabled -> settingsVm.setBiometricLock(enabled) })
            }
            SettingsGroup("Data") {
                DangerRow(Icons.Filled.DeleteForever, "Clear all history", onClick = { showClearConfirmDialog = true })
            }
            SettingsGroup("Notifications") {
                ToggleRow(Icons.Filled.Notifications, "Push notifications", pushEnabled, subtitle = "Powered by OneSignal", onChange = { enabled -> pushEnabled = enabled })
            }
            SettingsGroup("Support") {
                SettingsRow(Icons.Filled.Info, "About", onClick = onNavigateToAbout)
                GroupDivider()
                SettingsRow(Icons.Filled.Palette, "Theme Studio", onClick = onNavigateToThemeStudio)
                GroupDivider()
                SettingsRow(Icons.Filled.Star, "Rate App", onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PLAY_STORE_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { Toast.makeText(context, "No app to open Play Store", Toast.LENGTH_SHORT).show() }
                })
                GroupDivider()
                SettingsRow(Icons.Filled.Share, "Share App", onClick = {
                    try { val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Check out QuickScan Pro: ${AppConfig.PLAY_STORE_URL}") }; context.startActivity(Intent.createChooser(i, "Share via")) } catch (_: Exception) { Toast.makeText(context, "No app to share", Toast.LENGTH_SHORT).show() }
                })
                GroupDivider()
                SettingsRow(Icons.Filled.Shield, "Privacy Policy", onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.Legal.PRIVACY_POLICY)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { Toast.makeText(context, "No browser", Toast.LENGTH_SHORT).show() }
                })
                GroupDivider()
                SettingsRow(Icons.Filled.Description, "Terms of Use", onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.Legal.TERMS)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { Toast.makeText(context, "No browser", Toast.LENGTH_SHORT).show() }
                })
                GroupDivider()
                SettingsRow(Icons.Filled.ContactPage, "Contact Us", onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.Legal.CONTACT_US)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { Toast.makeText(context, "No browser", Toast.LENGTH_SHORT).show() }
                })
            }
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.W500, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDefaultActionDialog) DefaultActionDialog(defaultAction, { settingsVm.setDefaultAction(it) }, { showDefaultActionDialog = false })
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear all history?") },
            text = { Text("This will permanently delete all scan results.") },
            confirmButton = { TextButton(onClick = { historyVm.deleteAll(); showClearConfirmDialog = false }) { Text("Clear", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showClearConfirmDialog = false }) { Text("Cancel") } }
        )
    }
    if (showThemeDialog) ThemeModeDialog(themeMode, { themeVm.setThemeMode(it); showThemeDialog = false }, { showThemeDialog = false })
}

@Composable
private fun SettingsHeader(onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary) }
            Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun SettingsGroup(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp))
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) { Column { content() } }
    }
}

@Composable
private fun GroupDivider() { HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceContainer) }

@Composable
private fun ToggleRow(icon: ImageVector, title: String, checked: Boolean, onChange: (Boolean) -> Unit, subtitle: String? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500, fontSize = 16.sp, lineHeight = 24.sp), color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.W500, lineHeight = 16.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, trailing: String? = null, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500, fontSize = 16.sp, lineHeight = 24.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        if (trailing != null) { Text(trailing, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp), color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(8.dp)) }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun DangerRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500, fontSize = 16.sp, lineHeight = 24.sp), color = MaterialTheme.colorScheme.error)
    }
}

private fun defaultActionLabel(action: String) = when (action) {
    "show_result" -> "Show result only"; "open_url" -> "Open URL"; "copy_clipboard" -> "Copy"; "share" -> "Share"
    else -> "Show result only"
}

@Composable
private fun DefaultActionDialog(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val options = listOf("show_result" to "Show result only", "open_url" to "Open URL", "copy_clipboard" to "Copy", "share" to "Share")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Default action") },
        text = { Column { options.forEach { (key, label) -> Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(key); onDismiss() }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(current == key, { onSelect(key); onDismiss() }); Spacer(Modifier.width(12.dp)); Text(label, style = MaterialTheme.typography.bodyMedium) } } } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ThemeModeDialog(current: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Theme") },
        text = { Column { ThemeMode.entries.forEach { mode -> Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(mode) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(current == mode, { onSelect(mode) }); Spacer(Modifier.width(12.dp)); Text(mode.label, style = MaterialTheme.typography.bodyMedium) } } } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
