package com.simats.diawell123.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.diawell123.data.model.MedicationItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Data model for locally persisted medication alarm metadata.
 * Enables self-rescheduling daily alarm chains and restoring active daily alarms on reboot.
 */
data class PersistedMedReminder(
    val id: Int,
    val medName: String,
    val dosage: String,
    val hour: Int,
    val minute: Int,
    val isDaily: Boolean,
    val repeatType: String = if (isDaily) "Daily" else "Once",
    val isActive: Boolean = true,
    val userId: Int = -1,
    val scheduledTimeLabel: String = "",
    val userName: String = ""
)

/**
 * Persistent local storage for medication alarm metadata backed by SharedPreferences + Gson.
 * Guarantees that repeating daily alarms persist across app kills, reboots, and network offline states.
 */
object MedicationAlarmStorage {

    private const val TAG = "MedAlarmStorage"
    private const val PREFS_NAME = "diawell_medication_alarm_storage"
    private const val KEY_REMINDERS_MAP = "active_reminders_map"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun saveReminder(context: Context, reminder: PersistedMedReminder) {
        try {
            val prefs = getPrefs(context)
            val currentMap = getAllRemindersMap(context).toMutableMap()
            currentMap[reminder.id.toString()] = reminder
            prefs.edit().putString(KEY_REMINDERS_MAP, gson.toJson(currentMap)).apply()
            Log.d(TAG, "Saved persisted reminder id=${reminder.id}, name=${reminder.medName}, isDaily=${reminder.isDaily}, time=${reminder.hour}:${reminder.minute}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving reminder: ${e.message}", e)
        }
    }

    @Synchronized
    fun getReminder(context: Context, medId: Int): PersistedMedReminder? {
        val map = getAllRemindersMap(context)
        return map[medId.toString()]
    }

    @Synchronized
    fun isDaily(context: Context, medId: Int): Boolean {
        val rem = getReminder(context, medId) ?: return false
        return rem.isDaily || rem.repeatType.equals("Daily", ignoreCase = true)
    }

    @Synchronized
    fun getAllReminders(context: Context): List<PersistedMedReminder> {
        return getAllRemindersMap(context).values.toList()
    }

    @Synchronized
    fun getAllActiveDailyReminders(context: Context): List<PersistedMedReminder> {
        return getAllRemindersMap(context).values.filter {
            it.isActive && (it.isDaily || it.repeatType.equals("Daily", ignoreCase = true))
        }
    }

    @Synchronized
    fun setReminderActive(context: Context, medId: Int, isActive: Boolean) {
        try {
            val prefs = getPrefs(context)
            val currentMap = getAllRemindersMap(context).toMutableMap()
            val existing = currentMap[medId.toString()] ?: return
            currentMap[medId.toString()] = existing.copy(isActive = isActive)
            prefs.edit().putString(KEY_REMINDERS_MAP, gson.toJson(currentMap)).apply()
            Log.d(TAG, "Updated reminder id=$medId isActive=$isActive")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reminder status: ${e.message}", e)
        }
    }

    @Synchronized
    fun removeReminder(context: Context, medId: Int) {
        try {
            val prefs = getPrefs(context)
            val currentMap = getAllRemindersMap(context).toMutableMap()
            currentMap.remove(medId.toString())
            prefs.edit().putString(KEY_REMINDERS_MAP, gson.toJson(currentMap)).apply()
            Log.d(TAG, "Removed persisted reminder id=$medId")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing reminder: ${e.message}", e)
        }
    }

    @Synchronized
    fun syncFromMedicationsList(context: Context, meds: List<MedicationItem>, userId: Int = -1) {
        try {
            val currentMap = getAllRemindersMap(context).toMutableMap()
            var changed = false
            for (med in meds) {
                if (med.id <= 0) continue
                val isDaily = med.frequency.trim().equals("Daily", ignoreCase = true)
                val (hour, minute) = parseHourMinute(med.rawTime, med.reminderTime)
                val existing = currentMap[med.id.toString()]
                if (existing == null) {
                    currentMap[med.id.toString()] = PersistedMedReminder(
                        id = med.id,
                        medName = med.name,
                        dosage = med.dose,
                        hour = hour,
                        minute = minute,
                        isDaily = isDaily,
                        repeatType = if (isDaily) "Daily" else med.frequency,
                        isActive = true,
                        userId = if (userId > 0) userId else SessionManager.getInstance().getUserId() ?: -1,
                        scheduledTimeLabel = med.reminderTime
                    )
                    changed = true
                }
            }
            if (changed) {
                getPrefs(context).edit().putString(KEY_REMINDERS_MAP, gson.toJson(currentMap)).apply()
                Log.d(TAG, "Synced medications from server. Total count: ${currentMap.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing medications: ${e.message}", e)
        }
    }

    fun parseHourMinute(rawTime: String?, reminderTime: String?): Pair<Int, Int> {
        if (!rawTime.isNullOrBlank()) {
            try {
                val parts = rawTime.trim().split(":")
                if (parts.size >= 2) {
                    val h = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()
                    if (h != null && m != null && h in 0..23 && m in 0..59) {
                        return Pair(h, m)
                    }
                }
            } catch (_: Exception) {}
        }
        if (!reminderTime.isNullOrBlank()) {
            val formats = listOf("h:mm a", "hh:mm a", "h:mma", "hh:mma", "H:mm", "HH:mm")
            for (fmt in formats) {
                try {
                    val sdf = SimpleDateFormat(fmt, Locale.US)
                    sdf.isLenient = true
                    val date = sdf.parse(reminderTime.trim())
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        return Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                    }
                } catch (_: Exception) {}
            }
        }
        return Pair(8, 0)
    }

    private fun getAllRemindersMap(context: Context): Map<String, PersistedMedReminder> {
        val json = getPrefs(context).getString(KEY_REMINDERS_MAP, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, PersistedMedReminder>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Error deserializing reminders map: ${e.message}", e)
            emptyMap()
        }
    }
}
