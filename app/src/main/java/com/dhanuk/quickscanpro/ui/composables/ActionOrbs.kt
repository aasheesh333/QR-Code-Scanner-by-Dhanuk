package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanuk.quickscanpro.ui.theme.*

/**
 * Heptagon-style "action orb" used throughout the redesigned UI:
 * a translucent round glass disc with an optional glowing ring,
 * a primary-tinted core, and (optionally) a small label below it.
 *
 * Use [ActionOrb] for labelled icon buttons (torch, gallery, scan…),
 * and [ActionPill] for horizontally-scrolling labelled buttons.
 */
@Composable
fun ActionOrb(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    size: Dp = 56.dp,
    glow: Boolean = false,
    active: Boolean = false,
    contentDescription: String? = null,
) {
    val dark = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "orb_pulse")
    val glowAlpha by transition.animateFloat(
        initialValue = if (glow || active) 0.30f else 0.10f,
        targetValue = if (glow || active) 0.75f else 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (dark) GlassFillDark else GlassFillLight)
            .border(
                width = if (active) 2.dp else 1.dp,
                color = if (active) LuminaPrimaryGlow else
                    if (dark) GlassBorderDark else GlassBorderLight,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (glow || active) {
            Canvas(modifier = androidx.compose.ui.Modifier.size(size)) {
                drawCircle(
                    color = LuminaPrimaryGlow.copy(alpha = glowAlpha),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
        }
        icon()
    }
}

/** Pill-shaped labelled button, used for AI actions row on the result screen. */
@Composable
fun ActionPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    selected: Boolean = false,
) {
    val dark = isSystemInDarkTheme()
    val bg by animateColorAsState(
        targetValue = if (selected) LuminaPrimary.copy(alpha = if (dark) 0.55f else 0.95f)
        else if (dark) GlassFillDark else GlassFillLight,
        animationSpec = tween(200), label = "pill_bg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200), label = "pill_fg"
    )
    val borderColor = if (dark) GlassBorderDark else GlassBorderLight
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
    ) {
        icon?.let { it() }
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Hexagonal-corner (six-sided) viewfinder — different from the square
 * brackets in [ScanOverlay]. The hex marks glow in the Lumina purple and
 * a horizontal scanning beam sweeps across.
 */
@Composable
fun HexScanFrame(
    modifier: Modifier = Modifier,
    color: Color = LuminaPrimaryGlow
) {
    val transition = rememberInfiniteTransition(label = "hex_scan")
    val beamProgress by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beam"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 5f
        val hexCorner = (w.coerceAtMost(h)) * 0.10f
        val c = color.copy(alpha = glowAlpha)
        // Six small rotated-segment corner accents (one near each logical
        // vertex of an inscribed rounded hexagon — drawn as 4 corner + 2 side).
        // Top-Left (two segments forming an L)
        drawLine(c, androidx.compose.ui.geometry.Offset(0f, hexCorner),
                 androidx.compose.ui.geometry.Offset(0f, 0f), sw, StrokeCap.Round)
        drawLine(c, androidx.compose.ui.geometry.Offset(0f, 0f),
                 androidx.compose.ui.geometry.Offset(hexCorner, 0f), sw, StrokeCap.Round)
        // Top-Right
        drawLine(c, androidx.compose.ui.geometry.Offset(w - hexCorner, 0f),
                 androidx.compose.ui.geometry.Offset(w, 0f), sw, StrokeCap.Round)
        drawLine(c, androidx.compose.ui.geometry.Offset(w, 0f),
                 androidx.compose.ui.geometry.Offset(w, hexCorner), sw, StrokeCap.Round)
        // Bottom-Right
        drawLine(c, androidx.compose.ui.geometry.Offset(w, h - hexCorner),
                 androidx.compose.ui.geometry.Offset(w, h), sw, StrokeCap.Round)
        drawLine(c, androidx.compose.ui.geometry.Offset(w, h),
                 androidx.compose.ui.geometry.Offset(w - hexCorner, h), sw, StrokeCap.Round)
        // Bottom-Left
        drawLine(c, androidx.compose.ui.geometry.Offset(hexCorner, h),
                 androidx.compose.ui.geometry.Offset(0f, h), sw, StrokeCap.Round)
        drawLine(c, androidx.compose.ui.geometry.Offset(0f, h),
                 androidx.compose.ui.geometry.Offset(0f, h - hexCorner), sw, StrokeCap.Round)
        // Side accents at the visual middle (hexagonal illusion)
        drawLine(c.copy(alpha = glowAlpha * 0.6f),
                 androidx.compose.ui.geometry.Offset(0f, h / 2f - hexCorner / 3f),
                 androidx.compose.ui.geometry.Offset(0f, h / 2f + hexCorner / 3f), sw * 0.7f, StrokeCap.Round)
        drawLine(c.copy(alpha = glowAlpha * 0.6f),
                 androidx.compose.ui.geometry.Offset(w, h / 2f - hexCorner / 3f),
                 androidx.compose.ui.geometry.Offset(w, h / 2f + hexCorner / 3f), sw * 0.7f, StrokeCap.Round)
        // Sweeping beam
        val beamY = beamProgress * h
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.7f),
                    color.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                startY = (beamY - 80f).coerceAtLeast(0f),
                endY = (beamY + 80f).coerceAtMost(h)
            ),
            topLeft = androidx.compose.ui.geometry.Offset(0f, (beamY - 80f).coerceAtLeast(0f)),
            size = androidx.compose.ui.geometry.Size(w, 160f)
        )
    }
}

/** Small floating-mode "pill" used for Auto/Batch/Compare selectors. */
@Composable
fun ModePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val bg by animateColorAsState(
        targetValue = if (selected) LuminaPrimary else if (dark) GlassFillDark else GlassFillLight,
        animationSpec = tween(180), label = "mode_bg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(180), label = "mode_fg"
    )
    val borderColor = if (dark) GlassBorderDark else GlassBorderLight
    Text(
        text = label,
        color = fg,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
            .background(bg)
            .border(
                if (selected) 0.dp else 1.dp,
                if (selected) Color.Transparent else borderColor,
                androidx.compose.foundation.shape.RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 7.dp)
    )
}
