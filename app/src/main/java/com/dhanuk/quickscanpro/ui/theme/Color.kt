package com.dhanuk.quickscanpro.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// QuickScan Pro v3 — matches top Play Store QR apps
// (Gamma Scan 500M+, QRbot, TeaCapps). Design language:
//
//   • Camera screens: dark/black overlay with white scan frame
//     and white scan button — so the QR stands out
//   • All non-camera screens: pure white surfaces, near-black
//     ink text, light-gray dividers, minimal accents
//   • One single accent color (deep indigo) used ONLY for
//     primary CTAs and selected tab indicators — not as decoration
//   • No gradients, no glassmorphism, no pastel fills
// ─────────────────────────────────────────────────────────────

// Single accent — used sparingly per top-app pattern
val AccentPrimary = Color(0xFF1F3A8A)      // deep indigo (CTA, selected)
val AccentPrimaryDeep = Color(0xFF1E40AF)  // pressed state
val AccentPrimarySoft = Color(0xFFE0E7FF)  // very faint selected fill

// Ink scale — text and primary button content
val InkPrimary = Color(0xFF111111)
val InkSecondary = Color(0xFF6B7280)
val InkTertiary = Color(0xFF9CA3AF)

// White surfaces — for all non-camera screens
val SurfaceLowest = Color(0xFFFFFFFF)
val SurfaceLow = Color(0xFFFFFFFF)
val SurfaceMid = Color(0xFFF6F6F6)
val SurfaceHigh = Color(0xFFEEEEEE)
val SurfaceHighest = Color(0xFFE5E5E5)

// Camera / dark surfaces
val CameraBg = Color(0xFF000000)
val CameraOverlayScrim = Color(0xCC000000)  // 80% black
val CameraFrame = Color(0xFFFFFFFF)
val CameraButtonShadow = Color(0x33000000)

// Outlines & dividers
val OutlineStrong = Color(0xFFD4D4D4)
val OutlineFaint = Color(0xFFEAEAEA)
val Divider = Color(0xFFEEEEEE)

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
val SemanticWarn = Color(0xFFD97706)
val SemanticWarnSoft = Color(0xFFFEF3C7)
val SemanticDanger = Color(0xFFDC2626)
val SemanticDangerSoft = Color(0xFFFDE8E8)
val SemanticInfo = Color(0xFF2563EB)
val SemanticInfoSoft = Color(0xFFE5EEFF)

// Safety check traffic-light palette
val SafetySafe = Color(0xFF22C55E)
val SafetyWarn = Color(0xFFF59E0B)
val SafetyRisky = Color(0xFFEF4444)
