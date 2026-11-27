package com.simats.diawell123.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.simats.diawell123.data.local.MedicationAlarmStorage
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.service.MedicineVoiceService
import com.simats.diawell123.util.DiaWellNotificationHelper
import com.simats.diawell123.util.MedicationStatusUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * BroadcastReceiver triggered when an AlarmManager clock event fires
 * or when the user interacts with notification actions (Taken, Snooze, Skip),
 * or when the device boots / restarts / updates.
 */
class MedReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medId = intent.getIntExtra("med_id", (System.currentTimeMillis() % 10000).toInt())
        val medName = intent.getStringExtra("med_name") ?: "Medication"
        val medDose = intent.getStringExtra("med_dose") ?: intent.getStringExtra("med_dosage") ?: ""
        val scheduledTime = intent.getStringExtra("scheduled_time") ?: ""
        val userId = intent.getIntExtra("user_id", -1)
        val isDaily = intent.getBooleanExtra("is_daily", false)
        val alarmHour = intent.getIntExtra("hour", -1)
        val alarmMinute = intent.getIntExtra("minute", -1)

        Log.d("MedReminderReceiver", "Broadcast received: action=$action, medId=$medId, medName=$medName, userId=$userId, isDaily=$isDaily, hour=$alarmHour, min=$alarmMinute")

        when (action) {
            DiaWellNotificationHelper.ACTION_TAKEN -> {
                Log.d("MedReminderReceiver", "Action TAKEN received for medId=$medId")
                MedicineVoiceService.stop(context)
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)

                // Cancel only any pending snooze alarm; preserve tomorrow's repeating daily alarm!
                DiaWellNotificationHelper.cancelSnoozeAlarm(context, medId)

                val persisted = MedicationAlarmStorage.getReminder(context, medId)
                val effectiveDaily = isDaily || (persisted?.isDaily == true) || (persisted?.repeatType.equals("Daily", ignoreCase = true))
                if (!effectiveDaily) {
                    DiaWellNotificationHelper.cancelMedicationAlarm(context, medId)
                }

                DiaWellNotificationHelper.showGeneralNotification(
                    context = context,
                    title = "Dose Completed",
                    message = "Great job taking $medName on time!"
                )
                Toast.makeText(context, "Marked as Taken", Toast.LENGTH_SHORT).show()

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
                MedicineVoiceService.stop(context)
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)

                val persisted = MedicationAlarmStorage.getReminder(context, medId)
                val effectiveDaily = isDaily || (persisted?.isDaily == true) || (persisted?.repeatType.equals("Daily", ignoreCase = true))
                val userName = intent.getStringExtra("user_name") ?: persisted?.userName ?: ""

                // Snooze with separate requestCode so tomorrow's daily alarm is NEVER overwritten
                DiaWellNotificationHelper.snoozeMedicationAlarm(context, medId, medName, medDose, 5, userId, effectiveDaily, userName)
                DiaWellNotificationHelper.showGeneralNotification(
                    context = context,
                    title = "Medication Snoozed",
                    message = "We will remind you to take $medName again in 5 minutes."
                )
                Toast.makeText(context, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show()

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
                MedicineVoiceService.stop(context)
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)

                // Cancel only any pending snooze alarm; preserve tomorrow's repeating daily alarm!
                DiaWellNotificationHelper.cancelSnoozeAlarm(context, medId)

                val persisted = MedicationAlarmStorage.getReminder(context, medId)
                val effectiveDaily = isDaily || (persisted?.isDaily == true) || (persisted?.repeatType.equals("Daily", ignoreCase = true))
                if (!effectiveDaily) {
                    DiaWellNotificationHelper.cancelMedicationAlarm(context, medId)
                }

                DiaWellNotificationHelper.showGeneralNotification(
                    context = context,
                    title = "Dose Skipped",
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
                MedicineVoiceService.stop(context)
                DiaWellNotificationHelper.stopAlarmSound(context)
                NotificationManagerCompat.from(context).cancel(medId)
            }

            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED",
            "android.intent.action.MY_PACKAGE_REPLACED",
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                Log.d("MedReminderReceiver", "Device reboot / power on / app update received ($action). Restoring all alarms.")
                DiaWellNotificationHelper.scheduleDailyHealthReminders(context)
                DiaWellNotificationHelper.rescheduleAllActiveMedications(context)
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
                val isSnooze = intent.getBooleanExtra("is_snooze", false)
                Log.d("MedReminderReceiver", "Scheduled alarm time arrived for medId=$medId! (isSnooze=$isSnooze)")

                val persisted = MedicationAlarmStorage.getReminder(context, medId)
                val effectiveDaily = isDaily || (persisted?.isDaily == true) || (persisted?.repeatType.equals("Daily", ignoreCase = true))
                val isActive = persisted?.isActive != false
                val userName = intent.getStringExtra("user_name") ?: persisted?.userName ?: ""

                val resolvedHour = when {
                    alarmHour in 0..23 -> alarmHour
                    persisted != null && persisted.hour in 0..23 -> persisted.hour
                    else -> Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                }
                val resolvedMinute = when {
                    alarmMinute in 0..59 -> alarmMinute
                    persisted != null && persisted.minute in 0..59 -> persisted.minute
                    else -> Calendar.getInstance().get(Calendar.MINUTE)
                }

                // If this is a snooze alarm, clear the snooze code. Do NOT reschedule tomorrow's daily alarm,
                // because tomorrow's daily alarm was already scheduled when the regular alarm originally rang.
                if (isSnooze) {
                    DiaWellNotificationHelper.cancelSnoozeAlarm(context, medId)
                    Log.d("MedReminderReceiver", "Snoozed alarm triggered for medId=$medId. Cleared snooze code.")
                } else if (effectiveDaily && isActive) {
                    // Reschedule daily occurrence for tomorrow
                    DiaWellNotificationHelper.scheduleNextDailyMedicationAlarm(
                        context = context,
                        medId = medId,
                        medName = medName,
                        dose = medDose,
                        hour = resolvedHour,
                        minute = resolvedMinute,
                        userId = userId,
                        userName = userName
                    )
                    Log.d("MedReminderReceiver", "Self-rescheduled next daily occurrence for medId=$medId at tomorrow $resolvedHour:$resolvedMinute")
                }

                // 2. THEN: Keep CPU awake for 30s to guarantee screen wake and loud audio playback
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

                // 3. Trigger full-screen alarm activity + loud looping audio + insistent notification for TODAY'S dose
                DiaWellNotificationHelper.triggerMedicationAlarm(
                    context = context,
                    medId = medId,
                    medName = medName,
                    dosage = medDose,
                    scheduledTime = scheduledTime,
                    userId = userId,
                    isDaily = effectiveDaily,
                    userName = userName
                )

                // 4. Start Personalized Voice Announcement if user has voice announcements enabled
                val isVoiceEnabled = try {
                    SessionManager.init(context.applicationContext)
                    SessionManager.getInstance().isVoiceAnnouncementsEnabled()
                } catch (_: Exception) {
                    true
                }
                if (isVoiceEnabled) {
                    MedicineVoiceService.start(
                        context = context,
                        medId = medId,
                        medicineName = medName,
                        dosage = medDose,
                        userName = userName
                    )
                }
            }

            else -> {
                Log.w("MedReminderReceiver", "Unknown action received: $action")
            }
        }
    }
}
