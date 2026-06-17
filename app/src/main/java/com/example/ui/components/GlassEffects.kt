package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Glass color constants for light and dark themes
private val GlassLightBg = Color(0xE6FFFFFF)        // 90% white
private val GlassLightBorder = Color(0x1A000000)     // 10% black
private val GlassLightShadow = Color(0x0A000000)     // 4% black

private val GlassDarkBg = Color(0x1AFFFFFF)          // 10% white
private val GlassDarkBorder = Color(0x26FFFFFF)      // 15% white
private val GlassDarkShadow = Color(0x4C000000)      // 30% black

// More opaque glass for cards with content that needs readability
private val GlassCardLightBg = Color(0xF2FFFFFF)     // 95% white
private val GlassCardDarkBg = Color(0x24FFFFFF)      // 14% white

/**
 * Applies glassmorphism styling to a composable: semi-transparent background,
 * subtle border, soft shadow, and rounded shape.
 *
 * This is the primary glass effect — no blur dependency, works on all API levels.
 * The frosted look comes from the interplay of translucency, border, and shadow.
 *
 * Usage:
 *   Modifier.glassBackground()
 *   Modifier.glassBackground(shape = RoundedCornerShape(28.dp), elevation = 8.dp)
 */
@Composable
fun Modifier.glassBackground(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 4.dp,
    opaque: Boolean = false
): Modifier {
    val isDark = isSystemInDarkTheme()
    val bgColor = when {
        isDark && opaque -> GlassCardDarkBg
        isDark -> GlassDarkBg
        opaque -> GlassCardLightBg
        else -> GlassLightBg
    }
    val borderColor = if (isDark) GlassDarkBorder else GlassLightBorder
    val shadowColor = if (isDark) GlassDarkShadow else GlassLightShadow

    return this
        .shadow(elevation, shape, ambientColor = shadowColor, spotColor = shadowColor)
        .clip(shape)
        .background(bgColor, shape)
        .border(0.5.dp, borderColor, shape)
}

/**
 * A glass container Box that wraps content with glassmorphism styling.
 * Use this for sections, cards, and panels that need the frosted-glass look.
 *
 * Example:
 *   GlassContainer {
 *       Text("Content")
 *   }
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 4.dp,
    opaque: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassBackground(shape = shape, elevation = elevation, opaque = opaque)
    ) {
        content()
    }
}

/**
 * A floating glass bar suitable for bottom navigation or action bars.
 * Pill-shaped by default with horizontal padding for the floating effect.
 */
@Composable
fun Modifier.glassBottomBar(
    shape: Shape = RoundedCornerShape(28.dp),
    elevation: Dp = 8.dp
): Modifier {
    return this.glassBackground(shape = shape, elevation = elevation)
}

/**
 * Glass card style with slightly more opaque background for text readability,
 * and a bit more depth via higher elevation.
 */
@Composable
fun Modifier.glassCard(): Modifier {
    return this.glassBackground(
        shape = RoundedCornerShape(22.dp),
        elevation = 4.dp,
        opaque = true
    )
}
