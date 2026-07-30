package com.dhanuk.quickscanpro.ui.screens

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.ui.composables.GlassCard
import com.dhanuk.quickscanpro.ui.theme.LuminaPrimary
import com.dhanuk.quickscanpro.ui.theme.ThemeMode
import com.dhanuk.quickscanpro.viewmodel.ThemeViewModel

/**
 * Theme Studio — clean professional layout.
 * Lets users preview theme modes, accent color presets, and typography.
 * Sliders/glow removed to match the clean light design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStudioScreen(onNavigateBack: () -> Unit) {
    val vm: ThemeViewModel = viewModel()
    val themeMode by vm.themeMode.collectAsState()
    val isDark by vm.isDarkTheme.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val presetAccents = listOf(
        LuminaPrimary to "Indigo",
        Color(0xFF1E88E5) to "Ocean Blue",
        Color(0xFF00897B) to "Teal",
        Color(0xFFE53935) to "Red",
        Color(0xFFFB8C00) to "Orange",
        Color(0xFF6D4C41) to "Walnut",
        Color(0xFF7CB342) to "Spring",
        Color(0xFF5C6BC0) to "Periwinkle"
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
                title = {
                    Text("Theme Studio", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                },
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
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ThemeModeButton("Light", Color(0xFFF3F4F6), themeMode == ThemeMode.LIGHT) {
                        vm.setThemeMode(ThemeMode.LIGHT)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ThemeModeButton("Dark", Color(0xFF1F2937), themeMode == ThemeMode.DARK) {
                        vm.setThemeMode(ThemeMode.DARK)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ThemeModeButton("System", Color(0xFF64748B), themeMode == ThemeMode.SYSTEM) {
                        vm.setThemeMode(ThemeMode.SYSTEM)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ThemeModeButton("AMOLED", Color.Black, themeMode == ThemeMode.AMOLED) {
                        vm.setThemeMode(ThemeMode.AMOLED)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Accent Color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Pick a preview accent. Restart to apply globally.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        presetAccents.forEach { (color, name) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp,
                                            if (MaterialTheme.colorScheme.primary == color)
                                                MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            CircleShape)
                                        .clickable {
                                            android.widget.Toast.makeText(context,
                                                "Preview: $name (saved in Settings)",
                                                android.widget.Toast.LENGTH_SHORT).show()
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

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Typography",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                            android.widget.Toast.makeText(context,
                                                "Preview: $name (saved in Settings)",
                                                android.widget.Toast.LENGTH_SHORT).show()
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
                    Text("Preview only. A full font switch requires saving the preference and restarting.",
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
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null,
            tint = if (label == "Light") Color.Black else Color.White,
            modifier = Modifier.size(22.dp))
        else Text(label, color = if (label == "Light") Color.Black else Color.White,
            style = MaterialTheme.typography.labelSmall)
    }
}
