package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// QuickScan Pro v4 — exact Stitch "Lumina Utility" M3 tonal palette
// (extracted from Stitch-generated Tailwind config; consistent
//  across all 18 generated screens)
//
//   Seed blue   #2563EB  -> M3 primary container
//   M3 primary          = #004AC6  (T40 tonal)
//   M3 primaryContainer  = #2563EB  (brand blue)
//   M3 secondary        = #006C49  (emerald tonal)
//   M3 secondaryContainer = #6CF8BB
//   M3 tertiary         = #943700
//   M3 tertiaryContainer = #BC4800 (amber)
//   M3 background       = #F9F9FF (faint blue-tinted white)
//   M3 surface          = #F9F9FF
//   M3 onSurface        = #141B2B (near-black ink, blue-tinted)
//   M3 outline          = #737686
//   M3 outlineVariant   = #C3C6D7
// ─────────────────────────────────────────────────────────────

// Stitch M3 Primary blue tonal scale
val AccentPrimary = Color(0xFF004AC6)            // M3 primary (T40)
val AccentPrimaryBrand = Color(0xFF2563EB)       // brand / primaryContainer
val AccentPrimarySoft = Color(0xFFDBE1FF)        // primaryFixed
val AccentPrimaryDeep = Color(0xFF003EA8)         // onPrimaryFixedVariant

// Stitch M3 Secondary emerald tonal scale
val AccentSecondary = Color(0xFF006C49)
val AccentSecondaryContainer = Color(0xFF6CF8BB)
val AccentSecondarySoft = Color(0xFF6FFBBE)

// Stitch M3 Tertiary amber tonal scale
val AccentTertiary = Color(0xFF943700)
val AccentTertiaryBrand = Color(0xFFBC4800)
val AccentTertiarySoft = Color(0xFFFFDBCD)

// Ink scale — Stitch M3 on-surface tonal
val InkPrimary = Color(0xFF141B2B)
val InkSecondary = Color(0xFF434655)
val InkTertiary = Color(0xFF737686)
val InkOnPrimary = Color(0xFFFFFFFF)
val InkOnPrimaryContainer = Color(0xFF00174B)
val InkOnSecondaryContainer = Color(0xFF00714D)
val InkOnTertiaryContainer = Color(0xFF360F00)

// Surfaces — Stitch M3 tonal surface scale (blue-tinted whites)
val SurfaceLowest = Color(0xFFFFFFFF)
val SurfaceLow = Color(0xFFF1F3FF)         // surface-container-low
val SurfaceMid = Color(0xFFE9EDFF)         // surface-container
val SurfaceHigh = Color(0xFFE1E8FD)       // surface-container-high
val SurfaceHighest = Color(0xFFDCE2F7)    // surface-container-highest
val SurfaceBright = Color(0xFFF9F9FF)     // M3 surface/background
val SurfaceDim = Color(0xFFD3DAEF)         // surface-dim

// Camera / dark surfaces
val CameraBg = Color(0xFF000000)
val CameraOverlayScrim = Color(0xCC000000)  // 80% black
val CameraFrame = Color(0xFFFFFFFF)
val CameraButtonShadow = Color(0x33000000)

// Outlines & dividers (Stitch M3)
val OutlineStrong = Color(0xFF737686)        // M3 outline
val OutlineFaint = Color(0xFFC3C6D7)        // M3 outline-variant
val Divider = Color(0xFFE1E8FD)

// Dark theme (kept neutral — for user-toggle dark mode)
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

// Semantic
val SemanticSafe = Color(0xFF16A34A)
val SemanticSafeSoft = Color(0xFFE8F5ED)
val SemanticWarn = Color(0xFFF59E0B)
val SemanticWarnSoft = Color(0xFFFEF3C7)
val SemanticDanger = Color(0xFFBA1A1A)        // M3 error (Stitch)
val SemanticDangerSoft = Color(0xFFFFDAD6)    // M3 errorContainer (Stitch)
val SemanticInfo = Color(0xFF004AC6)
val SemanticInfoSoft = Color(0xFFDBE1FF)

// Safety check traffic-light palette (kept for safety UIs)
val SafetySafe = Color(0xFF10B981)
val SafetyWarn = Color(0xFFF59E0B)
val SafetyRisky = Color(0xFFBA1A1A)
