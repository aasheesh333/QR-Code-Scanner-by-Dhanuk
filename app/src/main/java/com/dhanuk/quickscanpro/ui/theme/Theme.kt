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
    primaryContainer = AccentPrimaryContainer,
    onPrimaryContainer = InkOnPrimaryContainer,
    secondary = AccentSecondary,
    onSecondary = Color.White,
    secondaryContainer = AccentSecondaryContainer,
    onSecondaryContainer = InkOnSecondaryContainer,
    tertiary = AccentTertiary,
    onTertiary = Color.White,
    tertiaryContainer = AccentTertiaryContainer,
    onTertiaryContainer = InkOnTertiaryContainer,
    background = SurfaceBright,
    onBackground = InkPrimary,
    surface = SurfaceBright,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceHighest,
    onSurfaceVariant = InkSecondary,
    surfaceTint = AccentPrimary,
    inverseSurface = Color(0xFF0D0E11),
    inverseOnSurface = Color(0xFF9D9CA1),
    inversePrimary = AccentPrimaryContainer,
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
    onErrorContainer = Color(0xFF6E0523)
)

private val AppDarkScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color(0xFF001367),
    primaryContainer = AccentPrimaryDim,
    onPrimaryContainer = Color(0xFFE1E0F9),
    secondary = Color(0xFFC5C4DC),
    onSecondary = Color(0xFF2E2F42),
    secondaryContainer = Color(0xFF444559),
    onSecondaryContainer = Color(0xFFE1E0F9),
    tertiary = Color(0xFFE8BAD5),
    onTertiary = Color(0xFF46273C),
    tertiaryContainer = Color(0xFF5F3D53),
    onTertiaryContainer = Color(0xFFFFCFEC),
    background = DarkBg,
    onBackground = DarkOnBg,
    surface = DarkBg,
    onSurface = DarkOnBg,
    surfaceVariant = DarkSurfaceHighest,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkAccent,
    inverseSurface = Color(0xFFE2E2EB),
    inverseOnSurface = Color(0xFF303239),
    inversePrimary = AccentPrimary,
    surfaceContainerLowest = Color(0xFF08090C),
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurfaceHigh,
    surfaceContainerHigh = DarkSurfaceHighest,
    surfaceContainerHighest = Color(0xFF31323A),
    surfaceBright = Color(0xFF33343C),
    surfaceDim = DarkBg,
    outline = Color(0xFF8F909A),
    outlineVariant = DarkOutline,
    error = Color(0xFFFFB3C0),
    onError = Color(0xFF6E0523),
    errorContainer = Color(0xFF8F2438),
    onErrorContainer = Color(0xFFFFD9DE)
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
