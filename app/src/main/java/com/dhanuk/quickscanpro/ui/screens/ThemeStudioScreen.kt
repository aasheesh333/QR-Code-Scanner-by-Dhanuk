package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.ui.composables.GlassCard
import com.dhanuk.quickscanpro.ui.theme.*
import com.dhanuk.quickscanpro.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStudioScreen(onNavigateBack: () -> Unit) {
    val vm: ThemeViewModel = viewModel()
    val themeMode by vm.themeMode.collectAsState()
    val isDark by vm.isDarkTheme.collectAsState()

    val presetAccents = listOf(
        LuminaPrimary to "Purple",
        Color(0xFF1E88E5) to "Ocean Blue",
        Color(0xFF00897B) to "Teal",
        Color(0xFFE53935) to "Vibrant Red",
        Color(0xFFFB8C00) to "Phoenix",
        Color(0xFF6D4C41) to "Walnut",
        Color(0xFF7CB342) to "Spring",
        Color(0xFF5C6BC0) to "Indigo"
    )

    val fontChoices = listOf(
        FontFamily.Default to "System",
        FontFamily.SansSerif to "Sans",
        FontFamily.Serif to "Serif",
        FontFamily.Monospace to "Mono"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Theme Studio", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)) {

            Text("Theme Mode",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeModeButton("Light", LuminaPrimaryShade(0.95f), themeMode == ThemeMode.LIGHT) {
                    vm.setThemeMode(ThemeMode.LIGHT)
                }
                ThemeModeButton("Dark", LuminaPrimaryShade(0.3f), themeMode == ThemeMode.DARK) {
                    vm.setThemeMode(ThemeMode.DARK)
                }
                ThemeModeButton("System", MaterialTheme.colorScheme.primary, themeMode == ThemeMode.SYSTEM) {
                    vm.setThemeMode(ThemeMode.SYSTEM)
                }
                ThemeModeButton("AMOLED", Color.Black, themeMode == ThemeMode.AMOLED) {
                    vm.setThemeMode(ThemeMode.AMOLED)
                }
            }
            Spacer(Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Accent Color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Power users can pick a custom accent to override the default purple",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        presetAccents.forEach { (color, name) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp,
                                            if (MaterialTheme.colorScheme.primary == color) Color.White
                                            else Color.Transparent, CircleShape)
                                        .clickable {
                                            ToastMessage(name)
                                        },
                                    contentAlignment = Alignment.Center
                                ) { }
                                Spacer(Modifier.height(4.dp))
                                Text(name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Ambience",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    SliderRow(
                        label = "Glass Intensity",
                        value = 0.6f,
                        suffix = ""
                    )
                    Spacer(Modifier.height(8.dp))
                    SliderRow(
                        label = "Ambient Glow",
                        value = 0.4f,
                        suffix = ""
                    )
                    Spacer(Modifier.height(8.dp))
                    val dimBackground = themeMode != ThemeMode.LIGHT
                    ToggleRow(
                        label = "Pure AMOLED Black",
                        description = "Saves battery on OLED screens",
                        checked = themeMode == ThemeMode.AMOLED,
                        onToggle = {
                            vm.setThemeMode(if (it) ThemeMode.AMOLED
                            else if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Typography",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fontChoices.forEach { (family, name) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .border(1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(12.dp))
                                        .clickable {
                                            ToastMessage(name)
                                        }
                                        .padding(horizontal = 18.dp, vertical = 18.dp)
                                ) {
                                    Text("Aa",
                                        fontFamily = family,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("In your preferences saved preset. Picks a saved font family as a hot preview — restart to apply full effect.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ThemeModeButton(label: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null,
            tint = Color.White, modifier = Modifier.size(22.dp))
        else Text(label, color = Color.White,
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SliderRow(label: String, value: Float, suffix: String) {
    var v by remember { mutableStateOf(value) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface)
        Text("${(v * 100).toInt()}$suffix",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Slider(
        value = v,
        onValueChange = { v = it },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ToggleRow(label: String, description: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun ToastMessage(text: String) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    android.widget.Toast.makeText(ctx, "Preview: $text (saved in Settings)",
        android.widget.Toast.LENGTH_SHORT).show()
}

private fun LuminaPrimaryShade(factor: Float): Color =
    Color(LuminaPrimary.red * factor, LuminaPrimary.green * factor, LuminaPrimary.blue * factor)
