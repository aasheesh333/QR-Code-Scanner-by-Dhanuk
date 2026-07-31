package com.dhanuk.quickscanpro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppLightScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = InkOnPrimary,
    primaryContainer = AccentPrimaryBrand,
    onPrimaryContainer = InkOnPrimaryContainer,
    secondary = AccentSecondary,
    onSecondary = Color.White,
    secondaryContainer = AccentSecondaryContainer,
    onSecondaryContainer = InkOnSecondaryContainer,
    tertiary = AccentTertiary,
    onTertiary = Color.White,
    tertiaryContainer = AccentTertiaryBrand,
    onTertiaryContainer = InkOnTertiaryContainer,
    background = SurfaceBright,
    onBackground = InkPrimary,
    surface = SurfaceBright,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = InkSecondary,
    surfaceTint = AccentPrimary,
    inverseSurface = Color(0xFF293040),
    inverseOnSurface = Color(0xFFEDF0FF),
    inversePrimary = Color(0xFFB4C5FF),
    surfaceContainerLowest = SurfaceLowest,
    surfaceContainerLow = SurfaceLow,
    surfaceContainer = SurfaceMid,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    outline = OutlineStrong,
    outlineVariant = OutlineFaint,
    error = SemanticDanger,
    onError = Color.White,
    errorContainer = SemanticDangerSoft,
    onErrorContainer = Color(0xFF93000A)
)

private val AppDarkScheme = darkColorScheme(
    primary = Color(0xFFB4C5FF),
    onPrimary = Color(0xFF002A5C),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF9CA3AF),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF9CA3AF),
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Color(0xFF262626),
    onTertiaryContainer = Color(0xFFE5E5E5),
    background = DarkBg,
    onBackground = DarkOnBg,
    surface = DarkSurface,
    onSurface = DarkOnBg,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = Color(0xFFB4C5FF),
    inverseSurface = Color(0xFFE5E5E5),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = AccentPrimary,
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceHigh,
    surfaceContainerHigh = Color(0xFF333333),
    surfaceContainerHighest = Color(0xFF404040),
    surfaceBright = Color(0xFF2A2A2A),
    surfaceDim = Color(0xFF0F0F0F),
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
    surfaceContainer = Color(0xFF111111),
    surfaceVariant = Color(0xFF1A1A1A)
)

enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System default"),
    AMOLED("AMOLED black")
}

@Composable
fun QuickScanProTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dark = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.AMOLED -> true
        ThemeMode.SYSTEM -> darkTheme
    }

    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == ThemeMode.AMOLED -> AppAmoledScheme
        dark -> AppDarkScheme
        else -> AppLightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = scheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = AuroraTypography,
        content = content
    )
}
