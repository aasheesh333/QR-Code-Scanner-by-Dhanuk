package com.dhanuk.quickscanpro.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.BuildConfig
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.ui.composables.SecondaryButton
import com.dhanuk.quickscanpro.ui.composables.SettingRow
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import com.dhanuk.quickscanpro.viewmodel.ThemeViewModel

@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit,
    onNavigateToThemeStudio: () -> Unit
) {
    val settingsVm: SettingsViewModel = viewModel()
    val themeVm: ThemeViewModel = viewModel()
    val themeMode by themeVm.themeMode.collectAsState()
    val vibrate by settingsVm.vibrateEnabled.collectAsState()
    val sound by settingsVm.soundEnabled.collectAsState()
    val incognito by settingsVm.incognitoMode.collectAsState()
    val autoCopy by settingsVm.autoCopyOnScan.collectAsState()

    AppBackground()
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Section("Appearance")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(Modifier.padding(8.dp)) {
                    ThemeModeButton("Light", Icons.Filled.LightMode, themeMode == com.dhanuk.quickscanpro.ui.theme.ThemeMode.LIGHT) { themeVm.setThemeMode(com.dhanuk.quickscanpro.ui.theme.ThemeMode.LIGHT) }
                    ThemeModeButton("Dark", Icons.Filled.DarkMode, themeMode == com.dhanuk.quickscanpro.ui.theme.ThemeMode.DARK) { themeVm.setThemeMode(com.dhanuk.quickscanpro.ui.theme.ThemeMode.DARK) }
                    ThemeModeButton("AMOLED", Icons.Filled.DarkMode, themeMode == com.dhanuk.quickscanpro.ui.theme.ThemeMode.AMOLED) { themeVm.setThemeMode(com.dhanuk.quickscanpro.ui.theme.ThemeMode.AMOLED) }
                    ThemeModeButton("Follow system", Icons.Filled.AutoMode, themeMode == com.dhanuk.quickscanpro.ui.theme.ThemeMode.SYSTEM) { themeVm.setThemeMode(com.dhanuk.quickscanpro.ui.theme.ThemeMode.SYSTEM) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                SecondaryButton(
                    text = "Theme Studio",
                    onClick = onNavigateToThemeStudio,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            Section("Scanning")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(Modifier.padding(8.dp)) {
                    ToggleRow(Icons.Filled.Vibration, "Vibration", vibrate) { settingsVm.setVibrate(it) }
                    ToggleRow(Icons.Filled.VolumeUp, "Beep on scan", sound) { settingsVm.setSound(it) }
                    ToggleRow(Icons.Filled.VerifiedUser, "Incognito (no history)", incognito) { settingsVm.setIncognito(it) }
                    ToggleRow(Icons.Filled.AutoMode, "Auto-copy content", autoCopy) { settingsVm.setAutoCopy(it) }
                }
            }

            Spacer(Modifier.height(20.dp))
            Section("More")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(Modifier.padding(8.dp)) {
                    SettingRow(Icons.Filled.Info, "About", null, onNavigateToAbout)
                    SettingRow(Icons.Filled.Lock, "Privacy", null, {})
                    SettingRow(Icons.Filled.Notifications, "Notifications", null, {})
                    SettingRow(Icons.Filled.BugReport, "Report a bug", null, {})
                    SettingRow(Icons.Filled.Share, "Share app", null, {})
                    SettingRow(Icons.Filled.Star, "Rate on Play Store", null, {})
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "QuickScan Pro v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun ToggleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ThemeModeButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (selected) {
            Text(
                "Selected",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).padding(2.dp)
        )
    }
}
