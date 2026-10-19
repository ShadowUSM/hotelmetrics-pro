package com.travio.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LightScheme = lightColorScheme(
    primary = LightPalette.primary,
    onPrimary = LightPalette.onPrimary,
    secondary = LightPalette.accent,
    background = LightPalette.background,
    onBackground = LightPalette.onBackground,
    surface = LightPalette.surface,
    onSurface = LightPalette.onBackground,
    surfaceVariant = LightPalette.surfaceVariant,
    onSurfaceVariant = LightPalette.onSurfaceVariant,
    outline = LightPalette.outline
)

private val DarkScheme = darkColorScheme(
    primary = DarkPalette.primary,
    onPrimary = DarkPalette.onPrimary,
    secondary = DarkPalette.accent,
    background = DarkPalette.background,
    onBackground = DarkPalette.onBackground,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.onBackground,
    surfaceVariant = DarkPalette.surfaceVariant,
    onSurfaceVariant = DarkPalette.onSurfaceVariant,
    outline = DarkPalette.outline
)

@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun TravioTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = isDarkTheme(mode)
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = TravioTypography,
        content = content
    )
}
