package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// QuickScan Pro v4 — matches Stitch "Lumina Utility" design system:
//   • Accent primary = blue #2563EB (Stitch T40)
//   • Secondary = emerald #10B981 (Stitch T40)
//   • Tertiary = amber #BC4800 (Stitch T40)
//   • Neutral = #111827 (Stitch T0)
//   • Surface scale: white → #F9FAFB → #F3F4F6 (Stitch neutral)
// Same look on every screen in the Stitch project
// ─────────────────────────────────────────────────────────────

// Stitch Lumina Utility — Primary blue scale (#2563EB)
val AccentPrimary = Color(0xFF2563EB)
val AccentPrimaryDeep = Color(0xFF1D4ED8)
val AccentPrimarySoft = Color(0xFFDBEAFE)

// Stitch Lumina Utility — Secondary emerald (#10B981)
val AccentSecondary = Color(0xFF10B981)
val AccentSecondarySoft = Color(0xFFD1FAE5)

// Stitch Lumina Utility — Tertiary amber (#BC4800)
val AccentTertiary = Color(0xFFBC4800)
val AccentTertiarySoft = Color(0xFFFED7AA)

// Ink scale (Stitch Neutral #111827)
val InkPrimary = Color(0xFF111827)
val InkSecondary = Color(0xFF4B5563)
val InkTertiary = Color(0xFF6B7280)

// Surfaces — Stitch neutral light scale: white → very subtle gray
val SurfaceLowest = Color(0xFFFFFFFF)
val SurfaceLow = Color(0xFFFFFFFF)
val SurfaceMid = Color(0xFFF9FAFB)
val SurfaceHigh = Color(0xFFF3F4F6)
val SurfaceHighest = Color(0xFFE5E7EB)

// Camera / dark surfaces
val CameraBg = Color(0xFF000000)
val CameraOverlayScrim = Color(0xCC000000)  // 80% black
val CameraFrame = Color(0xFFFFFFFF)
val CameraButtonShadow = Color(0x33000000)

// Outlines & dividers (Stitch-style neutral grays)
val OutlineStrong = Color(0xFFD1D5DB)
val OutlineFaint = Color(0xFFE5E7EB)
val Divider = Color(0xFFF3F4F6)

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

// Semantic (only when warnings are needed)
val SemanticSafe = Color(0xFF16A34A)
val SemanticSafeSoft = Color(0xFFE8F5ED)
val SemanticWarn = Color(0xFFF59E0B)
val SemanticWarnSoft = Color(0xFFFEF3C7)
val SemanticDanger = Color(0xFFDC2626)
val SemanticDangerSoft = Color(0xFFFDE8E8)
val SemanticInfo = Color(0xFF2563EB)
val SemanticInfoSoft = Color(0xFFDBEAFE)

// Safety check traffic-light palette
val SafetySafe = Color(0xFF10B981)   // matches Stitch secondary
val SafetyWarn = Color(0xFFF59E0B)
val SafetyRisky = Color(0xFFEF4444)
