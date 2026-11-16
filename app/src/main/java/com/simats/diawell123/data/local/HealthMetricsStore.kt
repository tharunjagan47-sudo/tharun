package com.simats.diawell123.data.local

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthMetricsStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "diawell_health_metrics",
        Context.MODE_PRIVATE
    )

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getWaterGlasses(): Int {
        val today = getTodayDate()
        val savedDate = prefs.getString(KEY_WATER_DATE, "")
        if (savedDate != today) {
            // New day: start clean from 0
            prefs.edit()
                .putString(KEY_WATER_DATE, today)
                .putInt(KEY_WATER_GLASSES, 0)
                .putFloat(KEY_WATER_LITERS, 0f)
                .apply()
            return 0
        }
        return prefs.getInt(KEY_WATER_GLASSES, 0)
    }

    fun addWaterGlass(): Int {
        val current = getWaterGlasses()
        val updated = (current + 1).coerceAtMost(16) // Max sensible limit
        val today = getTodayDate()
        prefs.edit()
            .putString(KEY_WATER_DATE, today)
            .putInt(KEY_WATER_GLASSES, updated)
            .putFloat(KEY_WATER_LITERS, updated * 0.25f)
            .apply()
        return updated
    }

    fun resetWater() {
        val today = getTodayDate()
        prefs.edit()
            .putString(KEY_WATER_DATE, today)
            .putInt(KEY_WATER_GLASSES, 0)
            .putFloat(KEY_WATER_LITERS, 0f)
            .apply()
    }

    fun getWaterLiters(): Float {
        return getWaterGlasses() * 0.25f
    }

    fun setWaterLiters(liters: Float) {
        val glasses = (liters / 0.25f).toInt().coerceAtLeast(0)
        val today = getTodayDate()
        prefs.edit()
            .putString(KEY_WATER_DATE, today)
            .putInt(KEY_WATER_GLASSES, glasses)
            .putFloat(KEY_WATER_LITERS, liters)
            .apply()
    }

    fun addActivity(minutes: Int, steps: Int) {
        val currentMinutes = prefs.getInt(KEY_ACTIVITY_MINUTES, 0)
        val currentSteps = prefs.getInt(KEY_ACTIVITY_STEPS, 0)
        prefs.edit()
            .putInt(KEY_ACTIVITY_MINUTES, currentMinutes + minutes)
            .putInt(KEY_ACTIVITY_STEPS, currentSteps + steps)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_WATER_DATE = "water_date"
        private const val KEY_WATER_GLASSES = "water_glasses"
        private const val KEY_WATER_LITERS = "water_liters"
        private const val KEY_ACTIVITY_MINUTES = "activity_minutes"
        private const val KEY_ACTIVITY_STEPS = "activity_steps"
    }
}
