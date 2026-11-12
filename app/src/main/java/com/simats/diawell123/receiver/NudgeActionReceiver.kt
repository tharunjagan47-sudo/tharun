package com.simats.diawell123.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.simats.diawell123.data.local.HealthMetricsStore
import com.simats.diawell123.util.SmartNudgeEngine

class NudgeActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_QUICK_LOG_WATER = "com.simats.diawell123.ACTION_QUICK_LOG_WATER"
        const val ACTION_QUICK_LOG_WALK = "com.simats.diawell123.ACTION_QUICK_LOG_WALK"
        const val ACTION_DISMISS_NUDGE = "com.simats.diawell123.ACTION_DISMISS_NUDGE"
        const val ACTION_TRIGGER_SCHEDULED_NUDGE = "com.simats.diawell123.ACTION_TRIGGER_SCHEDULED_NUDGE"
        const val ACTION_TRIGGER_PERIODIC_NUDGE = "com.simats.diawell123.ACTION_TRIGGER_PERIODIC_NUDGE"

        const val EXTRA_NUDGE_ID = "extra_nudge_id"
        const val EXTRA_SLOT_ID = "slot_id"
        private const val WATER_GLASS_LITERS = 0.25f
        private const val QUICK_WALK_MINUTES = 15
        private const val QUICK_WALK_STEPS = 1500
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val nudgeId = intent.getIntExtra(EXTRA_NUDGE_ID, -1)

        when (action) {
            ACTION_QUICK_LOG_WATER -> {
                val store = HealthMetricsStore(context.applicationContext)
                val updated = store.getWaterLiters() + WATER_GLASS_LITERS
                store.setWaterLiters(updated)
                dismissNotification(context, nudgeId)
                Toast.makeText(
                    context.applicationContext,
                    "💧 +250ml Water Logged! Keep it up!",
                    Toast.LENGTH_LONG,
                ).show()
            }

            ACTION_QUICK_LOG_WALK -> {
                val store = HealthMetricsStore(context.applicationContext)
                store.addActivity(QUICK_WALK_MINUTES, QUICK_WALK_STEPS)
                dismissNotification(context, nudgeId)
                Toast.makeText(
                    context.applicationContext,
                    "🏃 15-min Walk Logged (+1,500 steps)! Super da!",
                    Toast.LENGTH_LONG,
                ).show()
            }

            ACTION_DISMISS_NUDGE -> {
                dismissNotification(context, nudgeId)
                Toast.makeText(
                    context.applicationContext,
                    "👍 Staying healthy! Keep your streak going!",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            ACTION_TRIGGER_SCHEDULED_NUDGE -> {
                val slotId = intent.getIntExtra(EXTRA_SLOT_ID, -1)
                if (slotId != -1) {
                    SmartNudgeEngine.triggerDailySlotAndReschedule(context.applicationContext, slotId)
                }
            }

            ACTION_TRIGGER_PERIODIC_NUDGE -> {
                SmartNudgeEngine.triggerPeriodicEngagementPulseAndReschedule(context.applicationContext)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                SmartNudgeEngine.scheduleAllDailyNudges(context.applicationContext)
            }
        }
    }

    private fun dismissNotification(context: Context, notificationId: Int) {
        if (notificationId <= 0) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(notificationId)
    }
}
