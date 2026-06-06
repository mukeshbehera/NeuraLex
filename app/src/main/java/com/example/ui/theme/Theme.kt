package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryPurple,
    background = BackgroundDark,
    surface = CardBgDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = PrimaryTextDark,
    onSurface = PrimaryTextDark,
    outline = BorderColorDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryPurple,
    background = BackgroundLight,
    surface = CardBgLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = PrimaryTextLight,
    onSurface = PrimaryTextLight,
    outline = BorderColorLight
)

@Composable
fun NeuraLexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

