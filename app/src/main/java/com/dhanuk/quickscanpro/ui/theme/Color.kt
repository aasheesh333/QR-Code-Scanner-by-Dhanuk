package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------
// Clean Professional design system — white, minimal, one
// neutral brand accent (indigo). No glows, no gradients.
// Names kept stable so existing references keep compiling.
// ---------------------------------------------------------

// Brand accent — a calm, professional indigo (used sparingly)
val LuminaPrimary = Color(0xFF1F3A8A)          // deep indigo for buttons/accents
val LuminaPrimaryBright = Color(0xFF2563EB)    // slightly brighter for emphasis
val LuminaPrimarySoft = Color(0xFFDBEAFE)      // very light indigo tint (badges)
val LuminaPrimaryGlow = Color(0xFF2563EB)      // kept name → plain accent, no glow
val LuminaPrimaryFaint = Color(0xFFEFF4FF)     // faint tint for selected rows

// Dark theme base (still supported, but neutral)
val LuminaNavy = Color(0xFF111827)
val LuminaInk = Color(0xFF111827)
val LuminaBackgroundDark = Color(0xFF111827)
val LuminaSurfaceDark = Color(0xFF1F2937)
val LuminaSurfaceHighDark = Color(0xFF374151)
val LuminaOnBackgroundDark = Color(0xFFF3F4F6)
val LuminaOnSurfaceVariantDark = Color(0xFF9CA3AF)
val LuminaOutlineDark = Color(0xFF4B5563)

// Glass tints → now plain surfaces. "GlassCard" renders a clean white
// card in light mode and a slightly raised neutral card in dark mode.
val GlassFillDark = Color(0xFF1F2937)
val GlassBorderDark = Color(0xFF374151)
val GlassFillLight = Color(0xFFFFFFFF)
val GlassBorderLight = Color(0xFFE5E7EB)      // subtle gray stroke

// Light theme — pure whites and light grays
val LuminaBackgroundLight = Color(0xFFFFFFFF)
val LuminaSurfaceLight = Color(0xFFFFFFFF)
val LuminaSurfaceHighLight = Color(0xFFF3F4F6) // soft gray for raised areas
val LuminaOnBackgroundLight = Color(0xFF111827)
val LuminaOnSurfaceVariantLight = Color(0xFF6B7280)
val LuminaOutlineLight = Color(0xFFD1D5DB)

// "Liquid" background is disabled → flat clean colors
val LiquidDarkA = Color(0xFF111827)
val LiquidDarkB = Color(0xFF111827)
val LiquidDarkC = Color(0xFF111827)
val LiquidLightA = Color(0xFFFFFFFF)
val LiquidLightB = Color(0xFFFFFFFF)
val LiquidLightC = Color(0xFFFFFFFF)

// Semantic
val LuminaError = Color(0xFFEF4444)
val LuminaErrorLight = Color(0xFFDC2626)
val LuminaSuccess = Color(0xFF16A34A)
val LuminaWarning = Color(0xFFF59E0B)

// Safety score colors (Link Safety Check feature)
val SafetySafe = Color(0xFF16A34A)
val SafetyCaution = Color(0xFFF59E0B)
val SafetyRisky = Color(0xFFEF4444)

// Legacy aliases kept so old references still compile
val DhanukPrimary = LuminaPrimary
val DhanukPrimaryVariant = LuminaPrimaryBright
val DhanukSecondary = LuminaPrimaryBright
val DhanukOnPrimary = Color(0xFFFFFFFF)
val DhanukOnSecondary = Color(0xFF111827)
val DhanukBackgroundDark = LuminaBackgroundDark
val DhanukSurfaceDark = LuminaSurfaceDark
val DhanukAmoledBlack = Color(0xFF000000)
val DhanukAccent = LuminaPrimaryBright
val DhanukError = LuminaErrorLight
val DhanukOnBackgroundDark = LuminaOnBackgroundDark

// Gradients are now the same solid color → renders flat wherever referenced
val GradientStart = LuminaPrimary
val GradientEnd = LuminaPrimary
val GradientMid = LuminaPrimary
