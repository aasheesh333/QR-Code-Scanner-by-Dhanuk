package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------
// Lumina Glass design system (from Stitch "Elite QR Scanner")
// ---------------------------------------------------------

// Brand accent — the glowing purple signature
val LuminaPrimary = Color(0xFF700B97)
val LuminaPrimaryBright = Color(0xFF8D33B3)
val LuminaPrimarySoft = Color(0xFFA94FCE)
val LuminaPrimaryGlow = Color(0xFFC56AEB)
val LuminaPrimaryFaint = Color(0xFFECB2FF)

// Deep space base colors (dark theme)
val LuminaNavy = Color(0xFF16213E)
val LuminaInk = Color(0xFF1A1A2E)
val LuminaBackgroundDark = Color(0xFF121414)
val LuminaSurfaceDark = Color(0xFF1A1C1C)
val LuminaSurfaceHighDark = Color(0xFF282A2B)
val LuminaOnBackgroundDark = Color(0xFFE2E2E2)
val LuminaOnSurfaceVariantDark = Color(0xFFD2C1D3)
val LuminaOutlineDark = Color(0xFF9B8C9C)

// Glass tints
val GlassFillDark = Color(0x333B4665)      // rgba(59,70,101,0.2)
val GlassBorderDark = Color(0x1AFFFFFF)    // rgba(255,255,255,0.10)
val GlassFillLight = Color(0x59FFFFFF)     // rgba(255,255,255,0.35)
val GlassBorderLight = Color(0x40700B97)   // rgba(112,11,151,0.25)

// Light theme (glass on soft violet mist)
val LuminaBackgroundLight = Color(0xFFF4F1FA)
val LuminaSurfaceLight = Color(0xFFFFFFFF)
val LuminaSurfaceHighLight = Color(0xFFEDE7F6)
val LuminaOnBackgroundLight = Color(0xFF1A1A2E)
val LuminaOnSurfaceVariantLight = Color(0xFF5D5C74)
val LuminaOutlineLight = Color(0xFF76758D)

// Gradient stops for the "liquid" background
val LiquidDarkA = Color(0xFF1A1A2E)
val LiquidDarkB = Color(0xFF320047)
val LiquidDarkC = Color(0xFF16213E)
val LiquidLightA = Color(0xFFF4F1FA)
val LiquidLightB = Color(0xFFE9DFF7)
val LiquidLightC = Color(0xFFDEE4F5)

// Semantic
val LuminaError = Color(0xFFFFB4AB)
val LuminaErrorLight = Color(0xFFB3261E)
val LuminaSuccess = Color(0xFF4ADE80)
val LuminaWarning = Color(0xFFFBBF24)

// Safety score colors (Link Safety Check feature)
val SafetySafe = Color(0xFF22C55E)
val SafetyCaution = Color(0xFFF59E0B)
val SafetyRisky = Color(0xFFEF4444)

// Legacy aliases kept so old references still compile during migration
val DhanukPrimary = LuminaPrimaryBright
val DhanukPrimaryVariant = LuminaPrimary
val DhanukSecondary = LuminaPrimaryGlow
val DhanukOnPrimary = Color(0xFFFFFFFF)
val DhanukOnSecondary = Color(0xFF1A1A2E)
val DhanukBackgroundDark = LuminaBackgroundDark
val DhanukSurfaceDark = LuminaSurfaceDark
val DhanukAmoledBlack = Color(0xFF000000)
val DhanukAccent = LuminaPrimaryGlow
val DhanukError = LuminaError
val DhanukOnBackgroundDark = LuminaOnBackgroundDark

val GradientStart = LuminaPrimary
val GradientEnd = LuminaNavy
val GradientMid = LuminaPrimaryBright
