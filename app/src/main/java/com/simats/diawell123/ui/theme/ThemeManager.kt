package com.simats.diawell123.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

enum class ThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * Centralized Theme Manager for DiaWell.
 * Provides instant app-wide theme switching without page reloads,
 * persistent preferences per user (and global fallback),
 * and automatic synchronization with Android system dark mode.
 */
object ThemeManager {
    private const val PREFS_NAME = "diawell_theme_prefs"
    private const val KEY_THEME_GLOBAL = "theme_mode_global"
    private const val KEY_THEME_PREFIX = "theme_mode_user_"

    private var appContext: Context? = null
    private var currentUserId: Int? = null

    private val _themeMode = mutableStateOf(ThemeMode.SYSTEM)
    val themeMode: State<ThemeMode> = _themeMode

    private val _isDark = mutableStateOf(false)
    val isDark: State<Boolean> = _isDark

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getKeyForUser(userId: Int?): String {
        return if (userId != null && userId > 0) {
            "$KEY_THEME_PREFIX$userId"
        } else {
            KEY_THEME_GLOBAL
        }
    }

    /**
     * Initializes ThemeManager on application / activity launch.
     */
    fun init(context: Context, isSystemDark: Boolean, userId: Int? = null) {
        appContext = context.applicationContext
        currentUserId = userId
        val prefs = getPrefs(context)
        val key = getKeyForUser(userId)
        val savedName = prefs.getString(key, null) ?: prefs.getString(KEY_THEME_GLOBAL, ThemeMode.SYSTEM.name)
        val mode = try {
            ThemeMode.valueOf(savedName ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        _themeMode.value = mode
        _isDark.value = when (mode) {
            ThemeMode.SYSTEM -> isSystemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }

    /**
     * Called when active user changes (e.g. login, switch account, logout).
     */
    fun onUserChanged(userId: Int?, isSystemDark: Boolean) {
        currentUserId = userId
        val ctx = appContext ?: return
        val prefs = getPrefs(ctx)
        val key = getKeyForUser(userId)
        val savedName = prefs.getString(key, null)
        val mode = if (savedName != null) {
            try {
                ThemeMode.valueOf(savedName)
            } catch (_: Exception) {
                _themeMode.value
            }
        } else {
            _themeMode.value
        }
        _themeMode.value = mode
        _isDark.value = when (mode) {
            ThemeMode.SYSTEM -> isSystemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }

    /**
     * Synchronizes with system dark mode changes when mode is SYSTEM.
     */
    fun syncWithSystem(isSystemDark: Boolean) {
        if (_themeMode.value == ThemeMode.SYSTEM) {
            _isDark.value = isSystemDark
        }
    }

    /**
     * Sets the theme mode explicitly (SYSTEM, LIGHT, or DARK).
     */
    fun setThemeMode(mode: ThemeMode, isSystemDark: Boolean = false) {
        _themeMode.value = mode
        _isDark.value = when (mode) {
            ThemeMode.SYSTEM -> isSystemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        appContext?.let { ctx ->
            val prefs = getPrefs(ctx)
            val key = getKeyForUser(currentUserId)
            prefs.edit().putString(key, mode.name).apply()
            // Also store as global default fallback
            prefs.edit().putString(KEY_THEME_GLOBAL, mode.name).apply()
        }
    }

    /**
     * Quick toggle between Light and Dark mode.
     */
    fun toggleTheme(isSystemDark: Boolean = false) {
        val newMode = if (_isDark.value) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(newMode, isSystemDark)
    }
}
