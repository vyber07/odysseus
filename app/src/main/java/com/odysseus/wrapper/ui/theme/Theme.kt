package com.odysseus.wrapper.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary          = OdBlue,
    onPrimary        = OdBg,
    secondary        = OdGreen,
    onSecondary      = OdBg,
    error            = OdRed,
    background       = OdBg,
    onBackground     = OdFg,
    surface          = OdPanel,
    onSurface        = OdFg,
    surfaceVariant   = OdSurface,
    onSurfaceVariant = OdFg,
    outline          = OdBorder,
    tertiary         = OdPurple
)

private val LightColors = lightColorScheme(
    primary          = OdBlue,
    onPrimary        = OdLightFg,
    secondary        = OdGreen,
    onSecondary      = OdLightFg,
    error            = OdRed,
    background       = OdLightBg,
    onBackground     = OdLightFg,
    surface          = OdLightPanel,
    onSurface        = OdLightFg,
    surfaceVariant   = OdLightBg,
    onSurfaceVariant = OdLightFg,
    tertiary         = OdPurple
)

@Composable
fun OdysseusTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography(),
        content     = content
    )
}
