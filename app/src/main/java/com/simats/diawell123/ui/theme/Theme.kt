package com.simats.diawell123.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/**
 * Access point for DiaWell theme colors: AppTheme.colors.background, AppTheme.colors.cardBackground, etc.
 */
object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

@Composable
fun Diawell123Theme(
    darkTheme: Boolean = ThemeManager.isDark.value,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = appColors.primaryAccent,
            secondary = appColors.secondaryAccent,
            tertiary = DiawellOrange,
            background = appColors.background,
            surface = appColors.cardBackground,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.elevatedSurface,
            outline = appColors.border,
            error = appColors.error
        )
    } else {
        lightColorScheme(
            primary = appColors.primaryAccent,
            secondary = appColors.secondaryAccent,
            tertiary = DiawellOrange,
            background = appColors.background,
            surface = appColors.cardBackground,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.elevatedSurface,
            outline = appColors.border,
            error = appColors.error
        )
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
