package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuminaDarkScheme = darkColorScheme(
    primary = LuminaPrimaryGlow,
    onPrimary = LuminaInk,
    primaryContainer = LuminaPrimary,
    onPrimaryContainer = LuminaPrimaryFaint,
    secondary = Color(0xFFBBC5EB),
    onSecondary = LuminaNavy,
    secondaryContainer = Color(0xFF3B4665),
    onSecondaryContainer = Color(0xFFAAB4D9),
    tertiary = Color(0xFFC6C4DF),
    onTertiary = LuminaInk,
    background = LuminaBackgroundDark,
    onBackground = LuminaOnBackgroundDark,
    surface = LuminaBackgroundDark,
    onSurface = LuminaOnBackgroundDark,
    surfaceVariant = LuminaSurfaceHighDark,
    onSurfaceVariant = LuminaOnSurfaceVariantDark,
    surfaceContainerLowest = Color(0xFF0D0E0F),
    surfaceContainerLow = LuminaSurfaceDark,
    surfaceContainer = Color(0xFF1E2020),
    surfaceContainerHigh = LuminaSurfaceHighDark,
    surfaceContainerHighest = Color(0xFF333535),
    outline = LuminaOutlineDark,
    outlineVariant = Color(0xFF4E4351),
    error = LuminaError,
    onError = Color(0xFF690005)
)

private val LuminaLightScheme = lightColorScheme(
    primary = LuminaPrimary,
    onPrimary = Color.White,
    primaryContainer = LuminaPrimaryFaint,
    onPrimaryContainer = LuminaPrimary,
    secondary = Color(0xFF3B4665),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDAE1FF),
    onSecondaryContainer = Color(0xFF252F4D),
    tertiary = Color(0xFF45455B),
    onTertiary = Color.White,
    background = LuminaBackgroundLight,
    onBackground = LuminaOnBackgroundLight,
    surface = LuminaSurfaceLight,
    onSurface = LuminaOnBackgroundLight,
    surfaceVariant = LuminaSurfaceHighLight,
    onSurfaceVariant = LuminaOnSurfaceVariantLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF8FD),
    surfaceContainer = Color(0xFFF1ECF9),
    surfaceContainerHigh = LuminaSurfaceHighLight,
    surfaceContainerHighest = Color(0xFFE4DDF0),
    outline = LuminaOutlineLight,
    outlineVariant = Color(0xFFCAC4D8),
    error = LuminaErrorLight,
    onError = Color.White
)

private val LuminaAmoledScheme = LuminaDarkScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM, AMOLED
}

@Composable
fun QuickScanProTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> LuminaLightScheme
        ThemeMode.DARK -> LuminaDarkScheme
        ThemeMode.AMOLED -> LuminaAmoledScheme
        ThemeMode.SYSTEM -> if (darkTheme) LuminaDarkScheme else LuminaLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
