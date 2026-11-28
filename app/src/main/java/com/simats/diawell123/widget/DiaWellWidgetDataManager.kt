package com.simats.diawell123.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.simats.diawell123.data.local.MedicationAlarmStorage
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.DashboardData
import com.simats.diawell123.data.model.MedicationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fast, offline-first data manager for the DiaWell Glance App Widget.
 * Persists latest glucose readings and upcoming medication reminders in SharedPreferences
 * and triggers immediate widget updates whenever health data is modified.
 */
object DiaWellWidgetDataManager {

    private const val TAG = "DiaWellWidgetData"
    private const val PREFS_NAME = "diawell_home_widget_prefs"

    // Glucose keys
    private const val KEY_GLUCOSE_VALUE = "widget_glucose_value"
    private const val KEY_GLUCOSE_UNIT = "widget_glucose_unit"
    private const val KEY_GLUCOSE_STATUS = "widget_glucose_status"
    private const val KEY_GLUCOSE_TIME = "widget_glucose_time"
    private const val KEY_HAS_GLUCOSE = "widget_has_glucose"

    // Medication keys
    private const val KEY_MED_NAME = "widget_med_name"
    private const val KEY_MED_DOSE = "widget_med_dose"
    private const val KEY_MED_TIME = "widget_med_time"
    private const val KEY_HAS_MED = "widget_has_med"
    private const val KEY_MED_ALL_CAUGHT_UP = "widget_med_all_caught_up"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Reads current widget display data.
     * Includes automated fallback to local MedicationAlarmStorage and SessionManager
     * so newly placed widgets render actual data immediately.
     */
    fun getWidgetData(context: Context): DiaWellWidgetData {
        val prefs = getPrefs(context)

        var hasGlucose = prefs.getBoolean(KEY_HAS_GLUCOSE, false)
        var glucoseValue = prefs.getString(KEY_GLUCOSE_VALUE, "") ?: ""
        var glucoseUnit = prefs.getString(KEY_GLUCOSE_UNIT, "mg/dL") ?: "mg/dL"
        var glucoseStatus = prefs.getString(KEY_GLUCOSE_STATUS, "Normal") ?: "Normal"
        var glucoseTime = prefs.getString(KEY_GLUCOSE_TIME, "") ?: ""

        // Fallback to SessionManager user session if no recent glucose logged in widget prefs
        if (!hasGlucose || glucoseValue.isBlank()) {
            try {
                SessionManager.init(context.applicationContext)
                val userSession = SessionManager.getInstance().getUserSession()
                val currentGlucose = userSession?.currentGlucoseMgDl
                if (currentGlucose != null && currentGlucose > 0f) {
                    hasGlucose = true
                    glucoseValue = currentGlucose.toInt().toString()
                    glucoseUnit = SessionManager.getInstance().getGlucoseUnit()
                    glucoseStatus = resolveGlucoseStatus(currentGlucose, "Fasting")
                    glucoseTime = "Latest"
                }
            } catch (_: Exception) {}
        }

        var hasMed = prefs.getBoolean(KEY_HAS_MED, false)
        var medName = prefs.getString(KEY_MED_NAME, "") ?: ""
        var medDose = prefs.getString(KEY_MED_DOSE, "") ?: ""
        var medTime = prefs.getString(KEY_MED_TIME, "") ?: ""
        var isCaughtUp = prefs.getBoolean(KEY_MED_ALL_CAUGHT_UP, false)

        // Fallback to active alarms stored in MedicationAlarmStorage
        if (!hasMed || medName.isBlank()) {
            val upcomingAlarm = findNextUpcomingAlarm(context)
            if (upcomingAlarm != null) {
                hasMed = true
                medName = upcomingAlarm.first
                medDose = upcomingAlarm.second
                medTime = upcomingAlarm.third
                isCaughtUp = false
            } else if (!isCaughtUp) {
                isCaughtUp = true
            }
        }

        return DiaWellWidgetData(
            glucoseValue = glucoseValue,
            glucoseUnit = glucoseUnit,
            glucoseStatus = glucoseStatus,
            glucoseTime = glucoseTime,
            hasGlucoseReading = hasGlucose,
            nextMedName = medName,
            nextMedDose = medDose,
            nextMedTime = medTime,
            hasUpcomingMed = hasMed,
            isMedAllCaughtUp = isCaughtUp
        )
    }

    /**
     * Saves a newly logged glucose reading and triggers widget refresh.
     */
    fun saveGlucoseReading(
        context: Context,
        value: Float,
        readingType: String = "Fasting",
        dateStr: String = "",
        timeStr: String = ""
    ) {
        try {
            val status = resolveGlucoseStatus(value, readingType)
            val timeLabel = formatRelativeTime(dateStr, timeStr)
            val unit = try {
                SessionManager.init(context.applicationContext)
                SessionManager.getInstance().getGlucoseUnit()
            } catch (_: Exception) { "mg/dL" }

            getPrefs(context).edit()
                .putBoolean(KEY_HAS_GLUCOSE, true)
                .putString(KEY_GLUCOSE_VALUE, value.toInt().toString())
                .putString(KEY_GLUCOSE_UNIT, unit)
                .putString(KEY_GLUCOSE_STATUS, status)
                .putString(KEY_GLUCOSE_TIME, timeLabel)
                .apply()

            Log.d(TAG, "Saved glucose reading for widget: $value $unit, status=$status, time=$timeLabel")
            updateWidget(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving glucose for widget: ${e.message}", e)
        }
    }

    /**
     * Saves glucose reading from Home Dashboard metric card.
     */
    fun saveGlucoseFromMetric(
        context: Context,
        valueStr: String,
        statusText: String,
        subtitle: String
    ) {
        try {
            if (valueStr.isBlank()) return
            val cleanValue = valueStr.replace("mg/dL", "", ignoreCase = true).trim()
            val cleanStatus = statusText.ifBlank { "Normal" }

            getPrefs(context).edit()
                .putBoolean(KEY_HAS_GLUCOSE, true)
                .putString(KEY_GLUCOSE_VALUE, cleanValue)
                .putString(KEY_GLUCOSE_UNIT, "mg/dL")
                .putString(KEY_GLUCOSE_STATUS, cleanStatus)
                .putString(KEY_GLUCOSE_TIME, subtitle)
                .apply()

            updateWidget(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving glucose metric for widget: ${e.message}", e)
        }
    }

    /**
     * Saves upcoming medication details and triggers widget refresh.
     */
    fun saveNextMedication(
        context: Context,
        name: String,
        dose: String = "",
        time: String = ""
    ) {
        try {
            getPrefs(context).edit()
                .putBoolean(KEY_HAS_MED, true)
                .putBoolean(KEY_MED_ALL_CAUGHT_UP, false)
                .putString(KEY_MED_NAME, name)
                .putString(KEY_MED_DOSE, dose)
                .putString(KEY_MED_TIME, time)
                .apply()

            Log.d(TAG, "Saved next medication for widget: $name ($dose) at $time")
            updateWidget(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving next medication for widget: ${e.message}", e)
        }
    }

    /**
     * Updates widget medication state from full medication list.
     * Automatically identifies the next upcoming dose or marks all caught up.
     */
    fun saveMedsList(context: Context, medications: List<MedicationItem>) {
        try {
            val upcoming = medications.firstOrNull {
                it.status.equals("Upcoming", ignoreCase = true) ||
                it.status.equals("Scheduled", ignoreCase = true)
            }

            if (upcoming != null) {
                saveNextMedication(
                    context = context,
                    name = upcoming.name,
                    dose = upcoming.dose,
                    time = upcoming.reminderTime
                )
            } else {
                // If there were medications and none are upcoming, all doses today are caught up
                val hasAnyMed = medications.isNotEmpty()
                getPrefs(context).edit()
                    .putBoolean(KEY_HAS_MED, false)
                    .putBoolean(KEY_MED_ALL_CAUGHT_UP, hasAnyMed)
                    .apply()

                updateWidget(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving meds list for widget: ${e.message}", e)
        }
    }

    /**
     * Refreshes medication state from MedicationAlarmStorage when status changes.
     */
    fun refreshFromAlarmStorage(context: Context) {
        try {
            val upcomingAlarm = findNextUpcomingAlarm(context)
            if (upcomingAlarm != null) {
                saveNextMedication(
                    context = context,
                    name = upcomingAlarm.first,
                    dose = upcomingAlarm.second,
                    time = upcomingAlarm.third
                )
            } else {
                getPrefs(context).edit()
                    .putBoolean(KEY_HAS_MED, false)
                    .putBoolean(KEY_MED_ALL_CAUGHT_UP, true)
                    .apply()
                updateWidget(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing from alarm storage: ${e.message}", e)
        }
    }

    /**
     * Syncs both glucose and medication state from Home Dashboard data.
     */
    fun syncFromDashboard(context: Context, data: DashboardData) {
        try {
            data.metrics?.glucose?.let { g ->
                saveGlucoseFromMetric(
                    context = context,
                    valueStr = g.value,
                    statusText = g.statusText,
                    subtitle = g.subtitle
                )
            }
            refreshFromAlarmStorage(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from dashboard: ${e.message}", e)
        }
    }

    /**
     * Dispatches an asynchronous widget refresh via Glance.
     */
    fun updateWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DiaWellWidget().updateAll(context.applicationContext)
                Log.d(TAG, "DiaWellWidget updateAll executed successfully")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update widget: ${e.message}")
            }
        }
    }

    private fun resolveGlucoseStatus(value: Float, readingType: String): String {
        return when {
            value < 70f -> "Low"
            readingType.contains("Post", ignoreCase = true) && value > 180f -> "High"
            !readingType.contains("Post", ignoreCase = true) && value > 140f -> "High"
            else -> "Normal"
        }
    }

    private fun formatRelativeTime(dateStr: String, timeStr: String): String {
        val nowTime = if (timeStr.isNotBlank()) timeStr else SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return if (dateStr.isBlank() || dateStr.equals(todayDate, ignoreCase = true) || dateStr.contains("Today", ignoreCase = true)) {
            "Today, $nowTime"
        } else {
            nowTime
        }
    }

    private fun findNextUpcomingAlarm(context: Context): Triple<String, String, String>? {
        return try {
            val reminders = MedicationAlarmStorage.getAllActiveDailyReminders(context)
            if (reminders.isEmpty()) return null

            val nowCal = Calendar.getInstance()
            val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

            // Look for next reminder today
            val nextToday = reminders
                .filter { (it.hour * 60 + it.minute) >= currentMinutes }
                .minByOrNull { it.hour * 60 + it.minute }

            val target = nextToday ?: reminders.minByOrNull { it.hour * 60 + it.minute }
            if (target != null) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, target.hour)
                    set(Calendar.MINUTE, target.minute)
                }
                val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
                Triple(target.medName, target.dosage, formattedTime)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
