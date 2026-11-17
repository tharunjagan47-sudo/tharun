package com.simats.diawell123.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Multi-tone Glowing Purple / Neon Electric Purple to Lilac Gradient Design System
 * Deep Indigo (#1E1B4B) -> Royal Purple (#7E22CE) -> Electric Purple (#A855F7) -> Neon Lilac / Orchid (#F0ABFC)
 */
object DiaWellGradients {
    val glowingPurple = listOf(
        Color(0xFF1E1B4B), // Deep Indigo
        Color(0xFF7E22CE), // Royal Purple
        Color(0xFFA855F7), // Electric Purple
        Color(0xFFF0ABFC)  // Neon Lilac / Orchid
    )

    val glowingPurple2Tone = listOf(
        Color(0xFF1E1B4B), // Deep Indigo
        Color(0xFFA855F7)  // Electric Purple
    )

    val glowingLilac2Tone = listOf(
        Color(0xFFA855F7), // Electric Purple
        Color(0xFFF0ABFC)  // Neon Lilac
    )

    val glowingPurpleBrush = Brush.horizontalGradient(glowingPurple)
    val glowingPurpleLinear = Brush.linearGradient(glowingPurple)
}

data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val secondaryBackground: Color,
    val cardBackground: Color,
    val cardElevated: Color,
    val elevatedSurface: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryAccent: Color,
    val success: Color,
    val successBg: Color,
    val successText: Color,
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFFAF8FC),
    secondaryBackground = Color(0xFFF5F0F8),
    cardBackground = Color(0xFFFFFFFF),
    cardElevated = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFF5F3FF), // Soft Lilac Glow
    inputBackground = Color(0xFFFAF5FF),
    inputBorder = Color(0xFFE9D5FF),
    border = Color(0xFFE9D5FF),
    textPrimary = Color(0xFF1E1E22),
    textSecondary = Color(0xFF5E5E65),
    textMuted = Color(0xFF8E8E95),
    primaryAccent = Color(0xFFA855F7), // Electric Purple
    success = Color(0xFF10B981),
    successBg = Color(0xFFECFDF5),
    successText = Color(0xFF047857),
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0F0B1E), // Deep Dark Indigo/Black
    secondaryBackground = Color(0xFF1A1435),
    cardBackground = Color(0xFF1E1B4B), // Deep Indigo
    cardElevated = Color(0xFF28235C),
    elevatedSurface = Color(0xFF2E1065), // Deep Glowing Violet
    inputBackground = Color(0xFF1A1435),
    inputBorder = Color(0xFF3B0764),
    border = Color(0xFF3B0764),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF94A3B8),
    primaryAccent = Color(0xFFF0ABFC), // Neon Lilac / Orchid
    success = Color(0xFF10B981),
    successBg = Color(0xFF064E3B),
    successText = Color(0xFF34D399),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        get() = if (ThemeManager.isDark) DarkAppColors else LightAppColors
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFA855F7), // Electric Purple
    secondary = Color(0xFF7E22CE), // Royal Purple
    tertiary = Color(0xFFF0ABFC), // Neon Lilac / Orchid
    background = Color(0xFFFAF8FC),
    surface = Color.White,
    onBackground = Color(0xFF1E1E22),
    onSurface = Color(0xFF1E1E22),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF0ABFC), // Neon Lilac / Orchid
    secondary = Color(0xFFA855F7), // Electric Purple
    tertiary = Color(0xFF1E1B4B), // Deep Indigo
    background = Color(0xFF0F0B1E),
    surface = Color(0xFF1E1B4B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
)

@Composable
fun Diawell123Theme(
    darkTheme: Boolean = when (ThemeManager.themeMode.value) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit,
) {
    ThemeManager.isSystemDark = isSystemInDarkTheme()
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
