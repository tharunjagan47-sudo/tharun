package com.simats.diawell123.ui.theme

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

enum class ThemeMode(val title: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

object ThemeManager {
    private const val PREFS_NAME = "diawell_theme_prefs"
    private const val KEY_THEME_MODE = "saved_theme_mode"

    private val _themeMode = mutableStateOf(ThemeMode.DARK)
    val themeMode: State<ThemeMode> = _themeMode
    private val _isSystemDark = mutableStateOf(false)
    var isSystemDark: Boolean
        get() = _isSystemDark.value
        set(value) {
            if (_isSystemDark.value != value) {
                _isSystemDark.value = value
            }
        }

    val isDark: Boolean
        get() = when (_themeMode.value) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> _isSystemDark.value
        }

    fun init(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedName = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
            val mode = try { ThemeMode.valueOf(savedName) } catch (_: Exception) { ThemeMode.DARK }
            _themeMode.value = mode
        } catch (_: Exception) {
            _themeMode.value = ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode, context: Context? = null) {
        _themeMode.value = mode
        if (context != null) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
            } catch (_: Exception) {}
        }
    }

    fun toggleTheme(context: Context? = null) {
        val nextMode = when (_themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(nextMode, context)
    }
}
