package com.simats.diawell123.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Default Material Palette Tokens
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// DiaWell Brand Identity Colors
val DiawellMagenta = Color(0xFFB81D85)
val DiawellDarkPurple = Color(0xFF6B156E)
val DiawellOrange = Color(0xFFFF5722)
val DiawellDeepPurple = Color(0xFF7A1C7D)
val DiawellViolet = Color(0xFF5B1761)

/**
 * Semantic Theme Tokens for DiaWell.
 * Every screen, card, button, text, and dialog references these tokens
 * to guarantee 100% consistent styling across both Light and Dark themes.
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val secondaryBackground: Color,
    val cardBackground: Color,
    val elevatedSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val borderSubtle: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val cardElevated: Color,
    val success: Color,
    val successBg: Color,
    val successText: Color,
    val warning: Color,
    val warningBg: Color,
    val warningText: Color,
    val error: Color,
    val errorBg: Color,
    val errorText: Color,
    val infoBg: Color,
    val infoText: Color,
    val liquidGlassBgTop: Color,
    val liquidGlassBgBottom: Color,
    val liquidGlassBorder: Color,
    val dialogBg: Color,
)

// Light Mode Tokens (Preserves 100% of DiaWell's original clean aesthetic)
val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFFCFAFC),
    secondaryBackground = Color(0xFFF8F7FA),
    cardBackground = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFFDEEF7),
    textPrimary = Color(0xFF1E1E22),
    textSecondary = Color(0xFF6E6877),
    textMuted = Color(0xFF8A8595),
    border = Color(0xFFEFECEF),
    borderSubtle = Color(0xFFF6F3F6),
    primaryAccent = Color(0xFFB81D85),
    secondaryAccent = Color(0xFF7A1C7D),
    inputBackground = Color(0xFFFFFFFF),
    inputBorder = Color(0xFFEFECEF),
    cardElevated = Color(0xFFFFFFFF),
    success = Color(0xFF2E7D32),
    successBg = Color(0xFFE8F5E9),
    successText = Color(0xFF2E7D32),
    warning = Color(0xFFE65100),
    warningBg = Color(0xFFFFF3E0),
    warningText = Color(0xFFE65100),
    error = Color(0xFFC62828),
    errorBg = Color(0xFFFFEBEE),
    errorText = Color(0xFFC62828),
    infoBg = Color(0xFFE3F2FD),
    infoText = Color(0xFF1565C0),
    liquidGlassBgTop = Color.White.copy(alpha = 0.92f),
    liquidGlassBgBottom = Color(0xFFF2E6F5).copy(alpha = 0.88f),
    liquidGlassBorder = Color.White.copy(alpha = 0.95f),
    dialogBg = Color.White,
)

// Dark Mode Tokens (Production-Quality Premium Deep Palette matching prompt specs)
val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0B1020),
    secondaryBackground = Color(0xFF111827),
    cardBackground = Color(0xFF182235),
    elevatedSurface = Color(0xFF202C40),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF94A3B8),
    border = Color(0xFF334155),
    borderSubtle = Color(0xFF1E293B),
    primaryAccent = Color(0xFFD439A3),
    secondaryAccent = Color(0xFFC026D3),
    inputBackground = Color(0xFF111827),
    inputBorder = Color(0xFF334155),
    cardElevated = Color(0xFF202C40),
    success = Color(0xFF4ADE80),
    successBg = Color(0xFF064E3B).copy(alpha = 0.45f),
    successText = Color(0xFF86EFAC),
    warning = Color(0xFFFBBF24),
    warningBg = Color(0xFF78350F).copy(alpha = 0.45f),
    warningText = Color(0xFFFDE68A),
    error = Color(0xFFF87171),
    errorBg = Color(0xFF7F1D1D).copy(alpha = 0.45f),
    errorText = Color(0xFFFCA5A5),
    infoBg = Color(0xFF0C4A6E).copy(alpha = 0.45f),
    infoText = Color(0xFF7DD3FC),
    liquidGlassBgTop = Color(0xFF1E293B).copy(alpha = 0.92f),
    liquidGlassBgBottom = Color(0xFF0F172A).copy(alpha = 0.88f),
    liquidGlassBorder = Color(0xFF475569).copy(alpha = 0.70f),
    dialogBg = Color(0xFF182235),
)
