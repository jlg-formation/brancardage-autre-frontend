package com.example.huybrancardage.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrancardageLightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue700,
    secondary = Blue600,
    onSecondary = White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue700,
    tertiary = Blue600,
    onTertiary = White,
    tertiaryContainer = Blue100,
    onTertiaryContainer = Blue700,
    background = Gray50,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = Gray200,
    error = Color(0xFFDC2626),
    onError = White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun HuyBrancardageTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = BrancardageLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
