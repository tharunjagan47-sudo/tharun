package com.simats.diawell123.data.local

import android.content.Context
import android.content.SharedPreferences

class HealthMetricsStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "diawell_health_metrics",
        Context.MODE_PRIVATE
    )

    fun getWaterLiters(): Float {
        return prefs.getFloat(KEY_WATER_LITERS, 0f)
    }

    fun setWaterLiters(liters: Float) {
        prefs.edit().putFloat(KEY_WATER_LITERS, liters).apply()
    }

    fun addActivity(minutes: Int, steps: Int) {
        val currentMinutes = prefs.getInt(KEY_ACTIVITY_MINUTES, 0)
        val currentSteps = prefs.getInt(KEY_ACTIVITY_STEPS, 0)
        prefs.edit()
            .putInt(KEY_ACTIVITY_MINUTES, currentMinutes + minutes)
            .putInt(KEY_ACTIVITY_STEPS, currentSteps + steps)
            .apply()
    }

    companion object {
        private const val KEY_WATER_LITERS = "water_liters"
        private const val KEY_ACTIVITY_MINUTES = "activity_minutes"
        private const val KEY_ACTIVITY_STEPS = "activity_steps"
    }
}
