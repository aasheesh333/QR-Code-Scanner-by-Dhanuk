package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// QuickScan Pro — Fresh Material 3 Indigo (Stitch v3 design)
//   primary       #4456BA (indigo, tonal spot from #3F51B5 seed)
//   surface       #FAF8FD (off-white)
//   onSurface     #303239
//   cards         #FFFFFF (surface-container-lowest)
//   tonal circles #E1E0F9 (secondary-container)
// ─────────────────────────────────────────────────────────────

// Primary — indigo
val AccentPrimary = Color(0xFF4456BA)
val AccentPrimaryBrand = Color(0xFF3F51B5)      // seed / brand
val AccentPrimaryContainer = Color(0xFF8596FF)  // primaryContainer
val AccentPrimarySoft = Color(0xFFE1E0F9)       // tonal container for chips/circles
val AccentPrimaryDim = Color(0xFF3749AD)
val InkOnPrimaryContainer = Color(0xFF001367)

// Secondary — neutral slate
val AccentSecondary = Color(0xFF5C5D72)
val AccentSecondaryContainer = Color(0xFFE1E0F9)
val InkOnSecondaryContainer = Color(0xFF4F5065)

// Tertiary — muted mauve (rarely used)
val AccentTertiary = Color(0xFF79546C)
val AccentTertiaryContainer = Color(0xFFFFCFEC)
val InkOnTertiaryContainer = Color(0xFF67435B)

// Ink scale
val InkPrimary = Color(0xFF303239)          // onSurface
val InkSecondary = Color(0xFF5D5F66)        // onSurfaceVariant
val InkTertiary = Color(0xFF797A82)         // outline
val InkOnPrimary = Color(0xFFFAF8FF)

// Surfaces — off-white hierarchy
val SurfaceBright = Color(0xFFFAF8FD)       // background / surface
val SurfaceLowest = Color(0xFFFFFFFF)       // cards
val SurfaceLow = Color(0xFFF4F3F9)
val SurfaceMid = Color(0xFFEEEDF4)
val SurfaceHigh = Color(0xFFE8E7EF)
val SurfaceHighest = Color(0xFFE2E2EB)
val SurfaceDim = Color(0xFFD9D9E2)

// Outlines & dividers
val OutlineStrong = Color(0xFF797A82)
val OutlineFaint = Color(0xFFB1B1BA)
val Divider = Color(0xFFE2E2EB)

// Camera / scanner
val CameraBg = Color(0xFF0D0E11)
val CameraOverlayScrim = Color(0xCC000000)
val CameraFrame = Color(0xFFFFFFFF)
val CameraButtonShadow = Color(0x33000000)

// Dark theme — derived from same indigo seed
val DarkBg = Color(0xFF0D0E11)
val DarkSurface = Color(0xFF131418)
val DarkSurfaceLow = Color(0xFF17181D)
val DarkSurfaceHigh = Color(0xFF1F2026)
val DarkSurfaceHighest = Color(0xFF26272E)
val DarkOutline = Color(0xFF45464E)
val DarkOnBg = Color(0xFFE2E2EB)
val DarkOnSurfaceVariant = Color(0xFFB1B1BA)
val DarkAccent = Color(0xFF8596FF)

// AMOLED
val AmoledBg = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0A0C)

// Semantic
val SemanticSafe = Color(0xFF16A34A)
val SemanticSafeSoft = Color(0xFFE8F5ED)
val SemanticWarn = Color(0xFFF59E0B)
val SemanticWarnSoft = Color(0xFFFEF3C7)
val SemanticDanger = Color(0xFFA8364B)
val SemanticDangerSoft = Color(0xFFF97386)
val SemanticInfo = Color(0xFF4456BA)
val SemanticInfoSoft = Color(0xFFE1E0F9)

// Safety check traffic-light palette
val SafetySafe = Color(0xFF10B981)
val SafetyWarn = Color(0xFFF59E0B)
val SafetyRisky = Color(0xFFA8364B)
