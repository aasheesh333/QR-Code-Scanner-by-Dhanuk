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

enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System default"),
    AMOLED("Pure black (AMOLED)")
}

private val LightScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = CobaltPrimaryContainer,
    onPrimaryContainer = CobaltOnPrimaryContainer,
    secondary = CobaltSecondary,
    onSecondary = Color.White,
    secondaryContainer = CobaltSecondaryContainer,
    onSecondaryContainer = CobaltOnSecondaryContainer,
    tertiary = CobaltTertiary,
    onTertiary = Color.White,
    tertiaryContainer = CobaltTertiaryContainer,
    onTertiaryContainer = CobaltOnTertiaryContainer,
    background = CanvasCool,
    onBackground = InkDark,
    surface = CanvasCool,
    onSurface = InkDark,
    surfaceVariant = CanvasTinted,
    onSurfaceVariant = InkMedium,
    surfaceTint = CobaltPrimary,
    inverseSurface = Onyx,
    inverseOnSurface = TextNight,
    inversePrimary = CobaltPrimary,
    surfaceContainerLowest = CardWhite,
    surfaceContainerLow = CanvasCool,
    surfaceContainer = CanvasTinted,
    surfaceContainerHigh = CanvasRaised,
    surfaceContainerHighest = CanvasSunken,
    surfaceBright = CardWhite,
    surfaceDim = CanvasSunken,
    outline = LineStrong,
    outlineVariant = LineFaint,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerSoft,
    onErrorContainer = Color(0xFF7A1408),
    scrim = Color(0x66000000)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF9DBBFF),
    onPrimary = Color(0xFF0F3B8F),
    primaryContainer = Color(0xFF1E4FBB),
    onPrimaryContainer = Color(0xFFDCE8FD),
    secondary = Color(0xFFA8B6CC),
    onSecondary = Color(0xFF1F2B42),
    secondaryContainer = Color(0xFF33415C),
    onSecondaryContainer = Color(0xFFDCE4F2),
    tertiary = Color(0xFF6FD8C8),
    onTertiary = Color(0xFF0A4A42),
    tertiaryContainer = Color(0xFF14645A),
    onTertiaryContainer = Color(0xFFCFF4EC),
    background = Graphite,
    onBackground = TextNight,
    surface = Graphite,
    onSurface = TextNight,
    surfaceVariant = GraphiteMid,
    onSurfaceVariant = TextNightSoft,
    surfaceTint = Color(0xFF9DBBFF),
    inverseSurface = CardWhite,
    inverseOnSurface = InkDark,
    inversePrimary = CobaltPrimary,
    surfaceContainerLowest = Onyx,
    surfaceContainerLow = GraphiteLow,
    surfaceContainer = GraphiteMid,
    surfaceContainerHigh = GraphiteHigh,
    surfaceContainerHighest = GraphiteHighest,
    surfaceBright = GraphiteHigh,
    surfaceDim = Graphite,
    outline = GraphiteLine,
    outlineVariant = GraphiteLine,
    error = Color(0xFFFC9A90),
    onError = Color(0xFF7A1408),
    errorContainer = Color(0xFF912018),
    onErrorContainer = Color(0xFFFBE5E2),
    scrim = Color(0x99000000)
)

private val AmoledScheme = DarkScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0E18),
    surfaceContainer = Color(0xFF10151F),
    surfaceContainerHigh = Color(0xFF161D2C),
    surfaceContainerHighest = Color(0xFF1C2434),
    surfaceBright = Color(0xFF1C2434),
    surfaceDim = Color.Black
)

@Composable
fun QuickScanProTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
        ThemeMode.SYSTEM -> darkTheme
    }

    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        themeMode == ThemeMode.AMOLED -> AmoledScheme
        dark -> DarkScheme
        else -> LightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = scheme.background.toArgb()
            window.navigationBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        content = content
    )
}
