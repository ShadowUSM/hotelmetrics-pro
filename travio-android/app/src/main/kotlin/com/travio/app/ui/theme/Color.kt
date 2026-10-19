package com.travio.app.ui.theme

import androidx.compose.ui.graphics.Color

// Map-First Explorer — akcent marki (limonka) wspólny dla obu motywów.
val Lime = Color(0xFFC8FF4D)

// Jasny motyw — trasa/akcent funkcjonalny to głęboki teal (limonka na jasnym tle
// miałaby za słaby kontrast), limonka zostaje jako akcent drugorzędny (CTA, kropka).
object LightPalette {
    val background = Color(0xFFF2F0E6)
    val surface = Color(0xFFFFFFFF)
    val surfaceVariant = Color(0xFFEDEAD9)
    val onBackground = Color(0xFF1E2420)
    val onSurfaceVariant = Color(0xFF4D564E)
    val muted = Color(0xFF6B7468)
    val primary = Color(0xFF0E7A6E)
    val onPrimary = Color(0xFFF2F0E6)
    val outline = Color(0xFFE6E1D2)
    val accent = Lime
}

// Ciemny motyw — limonka jest głównym akcentem (jak w koncepcie), na ciemnym tle ma
// pełny kontrast.
object DarkPalette {
    val background = Color(0xFF0B0F10)
    val surface = Color(0xFF151F1A)
    val surfaceVariant = Color(0xFF23302A)
    val onBackground = Color(0xFFEDEFEE)
    val onSurfaceVariant = Color(0xFFC7CCC9)
    val muted = Color(0xFF9AA39C)
    val primary = Lime
    val onPrimary = Color(0xFF10140F)
    val outline = Color(0x14FFFFFF)
    val accent = Lime
}
