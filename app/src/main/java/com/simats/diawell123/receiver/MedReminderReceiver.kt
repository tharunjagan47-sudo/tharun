package com.simats.diawell123.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.simats.diawell123.util.DiaWellNotificationHelper

/**
 * BroadcastReceiver triggered when an AlarmManager clock event fires
 * or when the user interacts with the notification actions (Taken, Snooze, Skip).
 */
class MedReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medId = intent.getIntExtra("med_id", (System.currentTimeMillis() % 10000).toInt())
        val medName = intent.getStringExtra("med_name") ?: "Medication"
        val medDose = intent.getStringExtra("med_dose") ?: intent.getStringExtra("med_dosage") ?: ""
        val scheduledTime = intent.getStringExtra("scheduled_time") ?: ""

        Log.d("MedReminderReceiver", "Broadcast received: action=$action, medId=$medId, medName=$medName")

        when (action) {
            DiaWellNotificationHelper.ACTION_TAKEN -> {
                Log.d("MedReminderReceiver", "Action TAKEN received for medId=$medId")
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)
                Toast.makeText(context, "Marked as Taken ✅", Toast.LENGTH_SHORT).show()
            }

            DiaWellNotificationHelper.ACTION_SNOOZE -> {
                Log.d("MedReminderReceiver", "Action SNOOZE received for medId=$medId")
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)
                DiaWellNotificationHelper.snoozeMedicationAlarm(context, medId, medName, medDose, 5)
                Toast.makeText(context, "Snoozed for 5 minutes ⏰", Toast.LENGTH_SHORT).show()
            }

            DiaWellNotificationHelper.ACTION_SKIP,
            DiaWellNotificationHelper.ACTION_DISMISS_ALARM -> {
                Log.d("MedReminderReceiver", "Action SKIP/DISMISS received for medId=$medId")
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)
                Toast.makeText(context, "Medication Skipped", Toast.LENGTH_SHORT).show()
            }

            DiaWellNotificationHelper.ACTION_ALARM_TRIGGER,
            DiaWellNotificationHelper.ACTION_MED_REMINDER,
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("MedReminderReceiver", "Scheduled alarm time arrived for medId=$medId!")

                // Keep CPU awake for 30s to guarantee screen wake and loud audio playback
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                val wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "DiaWell:MedReminderWakeLock"
                )
                try {
                    wakeLock?.acquire(30_000L)
                } catch (e: Exception) {
                    Log.w("MedReminderReceiver", "WakeLock acquire failed: ${e.message}")
                }

                // Trigger full-screen alarm activity + loud looping audio + insistent notification
                DiaWellNotificationHelper.triggerMedicationAlarm(
                    context = context,
                    medId = medId,
                    medName = medName,
                    dosage = medDose,
                    scheduledTime = scheduledTime
                )
            }

            else -> {
                Log.w("MedReminderReceiver", "Unknown action received: $action")
            }
        }
    }
}
