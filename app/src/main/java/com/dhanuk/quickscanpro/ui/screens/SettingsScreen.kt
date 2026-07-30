package com.dhanuk.quickscanpro.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.BuildConfig
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit,
    onNavigateToThemeStudio: () -> Unit
) {
    val settingsVm: SettingsViewModel = viewModel()
    val sound by settingsVm.soundEnabled.collectAsState()
    val vibrate by settingsVm.vibrateEnabled.collectAsState()
    val autoSave by settingsVm.autoCopyOnScan.collectAsState()
    val biometrics = remember { mutableStateOf(false) }
    val pushEnabled = remember { mutableStateOf(false) }

    AppBackground()
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsHeader()
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
                        trailing = "Show result only",
                        onClick = {}
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
                        checked = biometrics.value,
                        onChange = { biometrics.value = it }
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.LockClock,
                        title = "Auto-lock vault (minutes)",
                        trailing = "5",
                        onClick = {}
                    )
                    val error = MaterialTheme.colorScheme.error
                    val errorContainer = MaterialTheme.colorScheme.errorContainer
                    GroupDivider()
                    DangerRow(
                        icon = Icons.Filled.DeleteForever,
                        title = "Clear all history",
                        error = error,
                        errorContainer = errorContainer,
                        onClick = {}
                    )
                }
                Spacer(Modifier.height(24.dp))
                SettingsGroup(label = "Notifications") {
                    ToggleRow(
                        icon = Icons.Filled.Notifications,
                        title = "Push notifications",
                        subtitle = "Powered by OneSignal",
                        checked = pushEnabled.value,
                        onChange = { enabled ->
                            pushEnabled.value = enabled
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
                        onClick = {}
                    )
                    GroupDivider()
                    SubvalueRow(
                        icon = Icons.Filled.Share,
                        title = "Share App",
                        onClick = {}
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
}

private var oneSignalInitStubbed = false

private fun initOneSignalStub(enabled: Boolean) {
    if (enabled && !oneSignalInitStubbed) {
        oneSignalInitStubbed = true
    }
}

@Composable
private fun SettingsHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {}) {
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceBright,
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
        modifier = Modifier.padding(start = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
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
    var local by remember(checked) { mutableStateOf(checked) }
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
            checked = local,
            onCheckedChange = {
                local = it
                onChange(it)
            }
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
            tint = MaterialTheme.colorScheme.outline,
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
