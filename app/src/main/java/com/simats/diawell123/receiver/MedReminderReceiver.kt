package com.simats.diawell123.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.simats.diawell123.util.DiaWellNotificationHelper
import com.simats.diawell123.util.MedicationStatusUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        val userId = intent.getIntExtra("user_id", -1)

        Log.d("MedReminderReceiver", "Broadcast received: action=$action, medId=$medId, medName=$medName, userId=$userId")

        when (action) {
            DiaWellNotificationHelper.ACTION_TAKEN -> {
                Log.d("MedReminderReceiver", "Action TAKEN received for medId=$medId")
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)
                DiaWellNotificationHelper.cancelMedicationAlarm(context, medId)
                DiaWellNotificationHelper.showGeneralNotification(
                    context = context,
                    title = "✅ Dose Completed",
                    message = "Great job taking $medName on time!"
                )
                Toast.makeText(context, "Marked as Taken ✅", Toast.LENGTH_SHORT).show()

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        MedicationStatusUpdater.updateStatusAndNotify(
                            context = context.applicationContext,
                            medId = medId,
                            medName = medName,
                            status = "Taken",
                            explicitUserId = userId
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            DiaWellNotificationHelper.ACTION_SNOOZE -> {
                Log.d("MedReminderReceiver", "Action SNOOZE received for medId=$medId")
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)
                DiaWellNotificationHelper.snoozeMedicationAlarm(context, medId, medName, medDose, 5, userId)
                DiaWellNotificationHelper.showGeneralNotification(
                    context = context,
                    title = "⏰ Medication Snoozed",
                    message = "We will remind you to take $medName again in 5 minutes."
                )
                Toast.makeText(context, "Snoozed for 5 minutes ⏰", Toast.LENGTH_SHORT).show()

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        MedicationStatusUpdater.updateStatusAndNotify(
                            context = context.applicationContext,
                            medId = medId,
                            medName = medName,
                            status = "Snoozed",
                            explicitUserId = userId,
                            snoozeMinutes = 5
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            DiaWellNotificationHelper.ACTION_SKIP -> {
                Log.d("MedReminderReceiver", "Action SKIP received for medId=$medId")
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)
                DiaWellNotificationHelper.cancelMedicationAlarm(context, medId)
                DiaWellNotificationHelper.showGeneralNotification(
                    context = context,
                    title = "⏭️ Dose Skipped",
                    message = "Marked $medName as skipped for this schedule."
                )
                Toast.makeText(context, "Medication Skipped", Toast.LENGTH_SHORT).show()

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        MedicationStatusUpdater.updateStatusAndNotify(
                            context = context.applicationContext,
                            medId = medId,
                            medName = medName,
                            status = "Skipped",
                            explicitUserId = userId
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            DiaWellNotificationHelper.ACTION_DISMISS_ALARM -> {
                Log.d("MedReminderReceiver", "Action DISMISS received for medId=$medId")
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("MedReminderReceiver", "Device rebooted (BOOT_COMPLETED received). Rescheduling daily routine reminders.")
                DiaWellNotificationHelper.scheduleDailyHealthReminders(context)
            }

            DiaWellNotificationHelper.ACTION_DAILY_ROUTINE_REMINDER -> {
                val notifId = intent.getIntExtra("reminder_id", 8001)
                val title = intent.getStringExtra("reminder_title") ?: "Health Routine Check"
                val message = intent.getStringExtra("reminder_message") ?: "Time for your routine health check-in."
                val hour = intent.getIntExtra("hour", 8)
                val minute = intent.getIntExtra("minute", 0)

                Log.d("MedReminderReceiver", "Daily routine reminder triggered: $title")
                DiaWellNotificationHelper.showGeneralNotification(context, title, message, notifId)

                // Reschedule for next day at the same time
                DiaWellNotificationHelper.scheduleSingleRoutineReminder(context, notifId, hour, minute, title, message)
            }

            DiaWellNotificationHelper.ACTION_ALARM_TRIGGER,
            DiaWellNotificationHelper.ACTION_MED_REMINDER -> {
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
                    scheduledTime = scheduledTime,
                    userId = userId
                )
            }

            else -> {
                Log.w("MedReminderReceiver", "Unknown action received: $action")
            }
        }
    }
}
