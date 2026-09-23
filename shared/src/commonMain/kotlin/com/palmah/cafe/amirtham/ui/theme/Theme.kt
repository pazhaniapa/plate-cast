package com.palmah.cafe.amirtham.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmirthamLightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = WarmGray,
    onSecondary = Color.White,
    background = CreamBackground,
    onBackground = Charcoal,
    surface = CreamBackground,
    onSurface = Charcoal,
    surfaceVariant = TanSurfaceVariant,
    onSurfaceVariant = WarmGray,
    outline = BorderTan,
    surfaceContainerLow = OffWhiteSurfaceContainer,
)

private val AmirthamDarkColorScheme = darkColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = OnTerracottaContainer,
    onPrimaryContainer = TerracottaContainer,
    secondary = WarmGray,
    onSecondary = Charcoal,
    background = Charcoal,
    onBackground = CreamBackground,
    surface = Charcoal,
    onSurface = CreamBackground,
    surfaceVariant = WarmGray,
    onSurfaceVariant = TanSurfaceVariant,
    outline = WarmGray,
    surfaceContainerLow = CharcoalElevated,
)

@Composable
fun AmirthamTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) AmirthamDarkColorScheme else AmirthamLightColorScheme,
        content = content,
    )
}
