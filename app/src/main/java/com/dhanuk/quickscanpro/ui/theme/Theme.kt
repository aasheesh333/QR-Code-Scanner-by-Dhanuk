package com.dhanuk.quickscanpro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DhanukPrimary,
    onPrimary = DhanukOnPrimary,
    secondary = DhanukSecondary,
    onSecondary = DhanukOnSecondary,
    tertiary = DhanukAccent,
    background = DhanukBackgroundDark,
    surface = DhanukSurfaceDark,
    onBackground = DhanukOnBackgroundDark,
    onSurface = DhanukOnBackgroundDark,
    error = DhanukError
)

private val LightColorScheme = lightColorScheme(
    primary = DhanukPrimary,
    onPrimary = Color.White,
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    tertiary = DhanukAccent,
    background = Color(0xFFF6F9FF),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0A1929),
    onSurface = Color(0xFF0A1929),
    error = Color(0xFFB00020)
)

private val AmoledColorScheme = darkColorScheme(
    primary = DhanukPrimary,
    onPrimary = DhanukOnPrimary,
    secondary = DhanukSecondary,
    onSecondary = Color.Black,
    tertiary = DhanukAccent,
    background = DhanukAmoledBlack,
    surface = Color(0xFF0A0A0A),
    onBackground = DhanukOnBackgroundDark,
    onSurface = DhanukOnBackgroundDark,
    error = DhanukError
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM, AMOLED
}

@Composable
fun QuickScanProTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(LocalContext.current)
        } else DarkColorScheme
        ThemeMode.AMOLED -> AmoledColorScheme
        ThemeMode.SYSTEM -> when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
