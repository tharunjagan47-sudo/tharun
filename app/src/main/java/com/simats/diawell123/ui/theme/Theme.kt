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
 * Deep Indigo (#1E1B4B) -> Royal Purple (#7C3AED) -> Electric Purple (#8B5CF6 / #A855F7) -> Neon Lilac (#F0ABFC)
 */
object DiaWellGradients {
    val glowingPurple = listOf(
        Color(0xFF1E1B4B), // Deep Indigo
        Color(0xFF7C3AED), // Royal Purple
        Color(0xFF8B5CF6), // Electric Purple
        Color(0xFFF0ABFC)  // Neon Lilac / Orchid
    )

    val glowingPurple2Tone = listOf(
        Color(0xFF1E1B4B), // Deep Indigo
        Color(0xFF8B5CF6)  // Electric Purple
    )

    val glowingLilac2Tone = listOf(
        Color(0xFF8B5CF6), // Electric Purple
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
    val accentTeal: Color = Color(0xFF38BDF8),
    val accentCoral: Color = Color(0xFFF43F5E),
    val success: Color = Color(0xFF10B981),
    val successBg: Color = Color(0xFFECFDF5),
    val successText: Color = Color(0xFF047857),
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF8FAFC),         // Clean frosted light gray-purple tint
    secondaryBackground = Color(0xFFF1F5F9),
    cardBackground = Color(0xFFFFFFFF),      // Crisp pure white cards
    cardElevated = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFF5F3FF),     // Soft Lilac Glow
    inputBackground = Color(0xFFF8FAFC),
    inputBorder = Color(0xFFE2E8F0),
    border = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF1E293B),         // Dark Slate
    textSecondary = Color(0xFF64748B),       // Subtitle
    textMuted = Color(0xFF94A3B8),
    primaryAccent = Color(0xFF7C3AED),       // Deep rich royal purple
    accentTeal = Color(0xFF0284C7),
    accentCoral = Color(0xFFE11D48),
    success = Color(0xFF10B981),
    successBg = Color(0xFFECFDF5),
    successText = Color(0xFF047857),
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0F0E17),         // Deep charcoal-purple obsidian base
    secondaryBackground = Color(0xFF171524),
    cardBackground = Color(0xFF1E1B2E),      // Rich muted dark purple elevated surface
    cardElevated = Color(0xFF25213D),
    elevatedSurface = Color(0xFF25213D),     // Subtle glowing border / container
    inputBackground = Color(0xFF171524),
    inputBorder = Color(0xFF383256),
    border = Color(0xFF383256),
    textPrimary = Color(0xFFF8FAFC),         // Pure White
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF94A3B8),           // Muted Gray
    primaryAccent = Color(0xFFA78BFA),       // Vibrant neon lavender-purple
    accentTeal = Color(0xFF38BDF8),          // High-contrast teal accent
    accentCoral = Color(0xFFF43F5E),         // Coral accent
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
    primary = Color(0xFF7C3AED),            // Deep Rich Royal Purple
    secondary = Color(0xFF6D28D9),
    tertiary = Color(0xFF38BDF8),           // Accent Teal
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA78BFA),            // Vibrant Neon Lavender-Purple
    secondary = Color(0xFF8B5CF6),
    tertiary = Color(0xFF38BDF8),           // Accent Teal
    background = Color(0xFF0F0E17),         // Deep charcoal-purple obsidian base
    surface = Color(0xFF1E1B2E),            // Rich muted dark purple
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
