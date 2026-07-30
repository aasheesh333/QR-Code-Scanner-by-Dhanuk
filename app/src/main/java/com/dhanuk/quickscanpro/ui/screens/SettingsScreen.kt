package com.dhanuk.quickscanpro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.BuildConfig
import com.dhanuk.quickscanpro.ui.theme.DhanukAccent
import com.dhanuk.quickscanpro.ui.theme.ThemeMode
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import com.dhanuk.quickscanpro.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateToAbout: () -> Unit, onNavigateToThemeStudio: () -> Unit = {}) {
    val svm: SettingsViewModel = viewModel()
    val tvm: ThemeViewModel = viewModel()
    val context = LocalContext.current

    val vibrateEnabled by svm.vibrateEnabled.collectAsState()
    val soundEnabled by svm.soundEnabled.collectAsState()
    val incognitoMode by svm.incognitoMode.collectAsState()
    val autoCopyOnScan by svm.autoCopyOnScan.collectAsState()
    val themeMode by tvm.themeMode.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader("Appearance")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = themeMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (mode) {
                                        ThemeMode.LIGHT -> Icons.Filled.LightMode
                                        ThemeMode.DARK -> Icons.Filled.DarkMode
                                        ThemeMode.SYSTEM -> Icons.Filled.SettingsBrightness
                                        ThemeMode.AMOLED -> Icons.Filled.DoNotDisturb
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = when (mode) {
                                        ThemeMode.LIGHT -> "Light"
                                        ThemeMode.DARK -> "Dark"
                                        ThemeMode.SYSTEM -> "System default"
                                        ThemeMode.AMOLED -> "AMOLED black"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { tvm.setThemeMode(mode) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Scanning")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsSwitchItem(
                        icon = Icons.Filled.Vibration,
                        title = "Vibrate on scan",
                        subtitle = "Haptic feedback when a code is scanned",
                        checked = vibrateEnabled,
                        onCheckedChange = { svm.setVibrate(it) }
                    )
                    SettingsSwitchItem(
                        icon = Icons.Filled.VolumeUp,
                        title = "Sound on scan",
                        subtitle = "Play a beep when a code is scanned",
                        checked = soundEnabled,
                        onCheckedChange = { svm.setSound(it) }
                    )
                    SettingsSwitchItem(
                        icon = Icons.Filled.ContentCopy,
                        title = "Auto-copy",
                        subtitle = "Automatically copy scanned content to clipboard",
                        checked = autoCopyOnScan,
                        onCheckedChange = { svm.setAutoCopy(it) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Privacy")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                SettingsSwitchItem(
                    icon = Icons.Filled.VisibilityOff,
                    title = "Incognito mode",
                    subtitle = "Scans will not be saved to history",
                    checked = incognitoMode,
                    onCheckedChange = { svm.setIncognito(it) }
                )
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("More")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    InfoRowItem(
                        icon = Icons.Filled.Palette,
                        title = "Theme Studio",
                        subtitle = "Custom accent, glass intensity, AMOLED & fonts",
                        onClick = onNavigateToThemeStudio
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRowItem(
                        icon = Icons.Filled.Info,
                        title = "About",
                        subtitle = "Privacy, terms, contact us",
                        onClick = onNavigateToAbout
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRowItem(
                        icon = Icons.Filled.Star,
                        title = "Rate us",
                        subtitle = "Enjoying QuickScan Pro? Leave a review",
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                    )
                                )
                            } catch (_: Exception) {}
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRowItem(
                        icon = Icons.Filled.Share,
                        title = "Share app",
                        subtitle = "Tell your friends about QuickScan Pro",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out QuickScan Pro on Play Store! https://play.google.com/store/apps/details?id=${context.packageName}"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Share QuickScan Pro"))
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})\nQuickScan Pro by Dhanuk",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DhanukAccent
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        IconButton(onClick = onClick, modifier = Modifier.padding(start = 4.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
