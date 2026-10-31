package com.simats.diawell123.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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
    val successText: Color
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFFCFAFC),
    secondaryBackground = Color(0xFFF7F2F7),
    cardBackground = Color(0xFFFFFFFF),
    cardElevated = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFFDEEF7),
    inputBackground = Color(0xFFF9F5F9),
    inputBorder = Color(0xFFE5D8E5),
    border = Color(0xFFE8DCE8),
    textPrimary = Color(0xFF1E1E22),
    textSecondary = Color(0xFF5E5E65),
    textMuted = Color(0xFF8E8E95),
    primaryAccent = Color(0xFFB81D85),
    success = Color(0xFF10B981),
    successBg = Color(0xFFECFDF5),
    successText = Color(0xFF047857)
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0F172A),
    secondaryBackground = Color(0xFF1E293B),
    cardBackground = Color(0xFF1E293B),
    cardElevated = Color(0xFF2D3748),
    elevatedSurface = Color(0xFF1E293B),
    inputBackground = Color(0xFF1E293B),
    inputBorder = Color(0xFF334155),
    border = Color(0xFF334155),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    primaryAccent = Color(0xFFF472B6),
    success = Color(0xFF10B981),
    successBg = Color(0xFF064E3B),
    successText = Color(0xFF34D399)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        get() = if (ThemeManager.isDark) DarkAppColors else LightAppColors
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB81D85),
    secondary = Color(0xFF7A1C7D),
    tertiary = Color(0xFFFF5722),
    background = Color(0xFFFCFAFC),
    surface = Color.White,
    onBackground = Color(0xFF1E1E22),
    onSurface = Color(0xFF1E1E22),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF472B6),
    secondary = Color(0xFFB81D85),
    tertiary = Color(0xFFFF5722),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
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
    content: @Composable () -> Unit
) {
    ThemeManager.isSystemDark = isSystemInDarkTheme()
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
