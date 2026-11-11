package com.simats.diawell123.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.repository.ActivityRepositoryImpl
import com.simats.diawell123.util.SmartNudgeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles 1-tap interactive actions on Swiggy/Zomato-style Smart Health Nudge notifications
 * as well as scheduled daily nudge window triggers.
 */
class NudgeActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val nudgeId = intent.getIntExtra("nudge_id", -1)

        if (nudgeId > 0) {
            try {
                NotificationManagerCompat.from(context).cancel(nudgeId)
            } catch (_: Exception) {}
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        when (action) {
            ACTION_QUICK_LOG_WATER -> {
                val waterKey = "water_ml_$todayKey"
                val glassKey = "water_glasses_$todayKey"
                val newMl = prefs.getInt(waterKey, 0) + 250
                val newGlasses = prefs.getInt(glassKey, 0) + 1
                prefs.edit()
                    .putInt(waterKey, newMl)
                    .putInt(glassKey, newGlasses)
                    .apply()

                Log.d(TAG, "Smart Nudge Water logged: +250ml (today total=${newMl}ml, glasses=$newGlasses)")
                Toast.makeText(
                    context,
                    "💧 +250ml Water logged via Smart Nudge!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            ACTION_QUICK_LOG_WALK -> {
                val minsKey = "walk_mins_$todayKey"
                val kcalKey = "walk_kcal_$todayKey"
                val newMins = prefs.getInt(minsKey, 0) + 15
                val newKcal = prefs.getInt(kcalKey, 0) + 65
                prefs.edit()
                    .putInt(minsKey, newMins)
                    .putInt(kcalKey, newKcal)
                    .apply()

                Toast.makeText(
                    context,
                    "🚶 15-min Walk logged! Great job controlling your sugar!",
                    Toast.LENGTH_SHORT
                ).show()

                // Non-blocking backend sync if user session is active
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        SessionManager.init(context.applicationContext)
                        if (SessionManager.getInstance().isLoggedIn()) {
                            ActivityRepositoryImpl().logActivity(
                                activityType = "Walking",
                                durationMins = 15,
                                distanceKm = 1.2f,
                                caloriesKcal = 65,
                                notes = "15-min brisk walk logged via Smart Nudge"
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Background walk sync skipped: ${e.message}")
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_DISMISS_NUDGE -> {
                Log.d(TAG, "Smart Nudge acknowledged/dismissed for nudgeId=$nudgeId")
            }

            ACTION_TRIGGER_SCHEDULED_NUDGE -> {
                val slotIndex = intent.getIntExtra("nudge_slot_index", -1)
                if (slotIndex >= 0) {
                    SmartNudgeEngine.triggerScheduledSlotAndReschedule(context, slotIndex)
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                SmartNudgeEngine.scheduleDailyNudges(context)
            }
        }
    }

    companion object {
        private const val TAG = "NudgeActionReceiver"
        const val PREFS_NAME = "diawell_smart_nudges_prefs"
        const val ACTION_QUICK_LOG_WATER = "com.simats.diawell123.ACTION_QUICK_LOG_WATER"
        const val ACTION_QUICK_LOG_WALK = "com.simats.diawell123.ACTION_QUICK_LOG_WALK"
        const val ACTION_DISMISS_NUDGE = "com.simats.diawell123.ACTION_DISMISS_NUDGE"
        const val ACTION_TRIGGER_SCHEDULED_NUDGE = "com.simats.diawell123.ACTION_TRIGGER_SCHEDULED_NUDGE"
    }
}

