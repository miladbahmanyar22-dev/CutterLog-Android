package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private val CutterLogDarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryHover,
    onPrimaryContainer = Color.White,
    secondary = MediaAccentCyan,
    onSecondary = DarkBg,
    secondaryContainer = BorderDark,
    onSecondaryContainer = TextPrimary,
    tertiary = SuccessGreen,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    outlineVariant = BorderFocus,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun CutterLogTheme(
    darkTheme: Boolean = true, // Default to dark theme for CutterLog Pro
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CutterLogDarkColorScheme,
        typography = Typography
    ) {
        ProvideTextStyle(value = TextStyle(fontFamily = VazirFontFamily)) {
            content()
        }
    }
}


