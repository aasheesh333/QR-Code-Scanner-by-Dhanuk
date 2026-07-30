package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppLightScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    primaryContainer = AccentPrimarySoft,
    onPrimaryContainer = AccentPrimary,
    secondary = InkSecondary,
    onSecondary = Color.White,
    secondaryContainer = SurfaceHigh,
    onSecondaryContainer = InkPrimary,
    tertiary = InkTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SurfaceMid,
    onTertiaryContainer = InkSecondary,
    background = SurfaceLow,
    onBackground = InkPrimary,
    surface = SurfaceLowest,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = InkSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceLow,
    surfaceContainer = SurfaceMid,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    outline = OutlineStrong,
    outlineVariant = OutlineFaint,
    error = SemanticDanger,
    onError = Color.White,
    errorContainer = SemanticDangerSoft,
    onErrorContainer = SemanticDanger
)

private val AppDarkScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color(0xFF002A5C),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF9CA3AF),
    onSecondary = DarkSurfaceHigh,
    secondaryContainer = DarkSurfaceHigh,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF9CA3AF),
    onTertiary = DarkSurfaceHigh,
    tertiaryContainer = DarkSurface,
    onTertiaryContainer = DarkOnBg,
    background = DarkBg,
    onBackground = DarkOnBg,
    surface = DarkSurface,
    onSurface = DarkOnBg,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceHigh,
    surfaceContainerHigh = Color(0xFF333333),
    surfaceContainerHighest = Color(0xFF404040),
    outline = DarkOutline,
    outlineVariant = DarkDivider,
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA)
)

private val AppAmoledScheme = AppDarkScheme.copy(
    background = AmoledBg,
    surface = AmoledSurface,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF080808),
    surfaceContainer = Color(0xFF111111)
)

enum class ThemeMode { LIGHT, DARK, SYSTEM, AMOLED }

@Composable
fun QuickScanProTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = when (themeMode) {
        ThemeMode.LIGHT  -> AppLightScheme
        ThemeMode.DARK   -> AppDarkScheme
        ThemeMode.AMOLED -> AppAmoledScheme
        ThemeMode.SYSTEM -> if (darkTheme) AppDarkScheme else AppLightScheme
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = AuroraTypography,
        content = content
    )
}
