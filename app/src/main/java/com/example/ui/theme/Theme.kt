package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.Black,
    secondary = IntenseBlue,
    onSecondary = Color.White,
    tertiary = NeonPurple,
    onTertiary = Color.Black,
    background = CosmicSlateBg,
    onBackground = TextPrimary,
    surface = CosmicDarkCard,
    onSurface = TextPrimary,
    surfaceVariant = KeyboardKeyBg,
    onSurfaceVariant = TextSecondary,
    outline = BorderGlass
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the futuristic cyber feel
    dynamicColor: Boolean = false, // Disable dynamic colors to keep our premium unified branding!
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
