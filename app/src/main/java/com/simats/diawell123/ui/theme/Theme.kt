package com.simats.diawell123.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB81D85),
    secondary = Color(0xFF7A1C7D),
    tertiary = Color(0xFFFF5722),
    background = Color(0xFFFCFAFC),
    surface = Color.White,
    onBackground = Color(0xFF1E1E22),
    onSurface = Color(0xFF1E1E22),
)

@Composable
fun Diawell123Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
