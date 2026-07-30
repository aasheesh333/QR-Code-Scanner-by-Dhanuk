package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanDarkScheme = darkColorScheme(
    primary = LuminaPrimaryBright,
    onPrimary = Color.White,
    primaryContainer = LuminaSurfaceDark,
    onPrimaryContainer = Color.White,
    secondary = LuminaOnSurfaceVariantDark,
    onSecondary = Color.White,
    secondaryContainer = LuminaSurfaceHighDark,
    onSecondaryContainer = Color.White,
    tertiary = LuminaOnSurfaceVariantDark,
    onTertiary = Color.White,
    background = LuminaBackgroundDark,
    onBackground = LuminaOnBackgroundDark,
    surface = LuminaSurfaceDark,
    onSurface = LuminaOnBackgroundDark,
    surfaceVariant = LuminaSurfaceHighDark,
    onSurfaceVariant = LuminaOnSurfaceVariantDark,
    surfaceContainerLowest = Color(0xFF0B0F19),
    surfaceContainerLow = Color(0xFF1F2937),
    surfaceContainer = LuminaSurfaceDark,
    surfaceContainerHigh = LuminaSurfaceHighDark,
    surfaceContainerHighest = Color(0xFF4B5563),
    outline = LuminaOutlineDark,
    outlineVariant = Color(0xFF374151),
    error = LuminaError,
    onError = Color.White
)

private val CleanLightScheme = lightColorScheme(
    primary = LuminaPrimary,
    onPrimary = Color.White,
    primaryContainer = LuminaPrimaryFaint,
    onPrimaryContainer = LuminaPrimary,
    secondary = LuminaPrimaryBright,
    onSecondary = Color.White,
    secondaryContainer = LuminaPrimarySoft,
    onSecondaryContainer = LuminaPrimary,
    tertiary = LuminaOnSurfaceVariantLight,
    onTertiary = Color.White,
    background = LuminaBackgroundLight,
    onBackground = LuminaOnBackgroundLight,
    surface = LuminaSurfaceLight,
    onSurface = LuminaOnBackgroundLight,
    surfaceVariant = LuminaSurfaceHighLight,
    onSurfaceVariant = LuminaOnSurfaceVariantLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color(0xFFF9FAFB),
    surfaceContainerHigh = LuminaSurfaceHighLight,
    surfaceContainerHighest = Color(0xFFE5E7EB),
    outline = LuminaOutlineLight,
    outlineVariant = Color(0xFFE5E7EB),
    error = LuminaErrorLight,
    onError = Color.White
)

private val CleanAmoledScheme = CleanDarkScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black
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
        ThemeMode.LIGHT -> CleanLightScheme
        ThemeMode.DARK -> CleanDarkScheme
        ThemeMode.AMOLED -> CleanAmoledScheme
        ThemeMode.SYSTEM -> if (darkTheme) CleanDarkScheme else CleanLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
