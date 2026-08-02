package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// QuickScan Pro — simple white + indigo theme (Stitch design system)
//   Background / Surface = pure white #FFFFFF
//   Primary              = indigo #004AC6
//   primaryContainer     = bright indigo #2563EB
//   Surfaces             = white / light gray #F5F5F5
//   Ink                  = near black #141B2B
//   Outlines             = soft gray #E5E7EB / #9CA3AF
// ─────────────────────────────────────────────────────────────

// Primary — indigo
val AccentPrimary = Color(0xFF004AC6)            // M3 primary
val AccentPrimaryBrand = Color(0xFF2563EB)       // primaryContainer
val AccentPrimarySoft = Color(0xFFDBEAFE)        // primaryFixed
val AccentPrimaryDeep = Color(0xFF003A9E)

// Neutral secondary/tertiary (simple gray-blue, not emerald/amber)
val AccentSecondary = Color(0xFF475569)
val AccentSecondaryContainer = Color(0xFFE2E8F0)
val AccentSecondarySoft = Color(0xFFF1F5F9)

val AccentTertiary = Color(0xFF64748B)
val AccentTertiaryBrand = Color(0xFF94A3B8)
val AccentTertiarySoft = Color(0xFFF8FAFC)

// Ink scale
val InkPrimary = Color(0xFF141B2B)
val InkSecondary = Color(0xFF434655)
val InkTertiary = Color(0xFF737686)
val InkOnPrimary = Color(0xFFFFFFFF)
val InkOnPrimaryContainer = Color(0xFF00174B)
val InkOnSecondaryContainer = Color(0xFF334155)
val InkOnTertiaryContainer = Color(0xFF475569)

// Surfaces — clean white / light gray
val SurfaceLowest = Color(0xFFFFFFFF)         // white
val SurfaceLow = Color(0xFFF1F3FF)            // surfaceContainerLow
val SurfaceMid = Color(0xFFE9EDFF)            // surfaceContainer
val SurfaceHigh = Color(0xFFE1E8FD)           // surfaceContainerHigh
val SurfaceHighest = Color(0xFFDCE2F7)        // surfaceContainerHighest
val SurfaceBright = Color(0xFFF9F9FF)         // Stitch background/surface
val SurfaceDim = Color(0xFFD3DAEF)

// Camera / dark surfaces
val CameraBg = Color(0xFF000000)
val CameraOverlayScrim = Color(0xCC000000)
val CameraFrame = Color(0xFFFFFFFF)
val CameraButtonShadow = Color(0x33000000)

// Outlines & dividers
val OutlineStrong = Color(0xFF737686)
val OutlineFaint = Color(0xFFC3C6D7)
val Divider = Color(0xFFC3C6D7)

// Dark theme (kept neutral)
val DarkBg = Color(0xFF0F0F0F)
val DarkSurface = Color(0xFF1A1A1A)
val DarkSurfaceHigh = Color(0xFF262626)
val DarkOutline = Color(0xFF404040)
val DarkOnBg = Color(0xFFE5E5E5)
val DarkOnSurfaceVariant = Color(0xFF9CA3AF)
val DarkDivider = Color(0xFF262626)
val DarkAccent = Color(0xFF60A5FA)

// AMOLED
val AmoledBg = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0A0A)

// Semantic (simple, not loud)
val SemanticSafe = Color(0xFF16A34A)
val SemanticSafeSoft = Color(0xFFE8F5ED)
val SemanticWarn = Color(0xFFF59E0B)
val SemanticWarnSoft = Color(0xFFFEF3C7)
val SemanticDanger = Color(0xFFBA1A1A)
val SemanticDangerSoft = Color(0xFFFFDAD6)
val SemanticInfo = Color(0xFF004AC6)
val SemanticInfoSoft = Color(0xFFDBEAFE)

// Safety check traffic-light palette (kept for safety UIs)
val SafetySafe = Color(0xFF10B981)
val SafetyWarn = Color(0xFFF59E0B)
val SafetyRisky = Color(0xFFBA1A1A)
