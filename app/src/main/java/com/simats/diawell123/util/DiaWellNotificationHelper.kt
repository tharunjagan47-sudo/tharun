package com.simats.diawell123.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.simats.diawell123.MainActivity
import com.simats.diawell123.R
import com.simats.diawell123.receiver.MedReminderReceiver

/**
 * Central Notification and Alarm Helper for DiaWell Medication Reminders.
 */
object DiaWellNotificationHelper {

    const val CHANNEL_ID = "diawell_meds_channel_v2"
    private const val OLD_CHANNEL_ID = "diawell_meds_channel"
    const val CHANNEL_NAME = "Medication Reminders"
    const val CHANNEL_DESC = "Timely reminders to take your prescribed diabetes medications."
    const val ACTION_MED_REMINDER = "com.simats.diawell123.ACTION_MED_REMINDER"

    /**
     * Initializes the High-Importance Notification Channel with sound & vibration.
     * Safe to call repeatedly (idempotent).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Clean up legacy channel without alarm sound attributes
            try {
                manager.deleteNotificationChannel(OLD_CHANNEL_ID)
            } catch (_: Exception) {}

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            manager.createNotificationChannel(channel)
            Log.d("DiaWellNotifications", "Notification channel created: $CHANNEL_ID with sound: $soundUri")
        }
    }

    /**
     * Displays a rich, high-priority medication notification immediately.
     */
    fun showMedicationNotification(
        context: Context,
        title: String,
        message: String,
        medId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        createNotificationChannel(context)

        // Check runtime permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("DiaWellNotifications", "POST_NOTIFICATIONS permission not granted. Notification suppressed.")
                return
            }
        }

        // Tap action: launches MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_screen", "medications")
            putExtra("med_id", medId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        } catch (_: Exception) {
            null
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .apply {
                if (largeIcon != null) setLargeIcon(largeIcon)
            }
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 500, 500))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(medId, notification)
            Log.d("DiaWellNotifications", "Medication notification displayed for id=$medId")
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Failed to display notification: ${e.message}", e)
        }
    }

    /**
     * Schedules an exact local alarm via AlarmManager.
     */
    fun scheduleExactMedicationAlarm(
        context: Context,
        medId: Int,
        medName: String,
        dose: String,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_MED_REMINDER
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dose", dose)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d("DiaWellNotifications", "Scheduled exact alarm for medId=$medId at $triggerAtMillis")
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Failed to schedule exact alarm: ${e.message}", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancels any previously scheduled alarm for a medication.
     */
    fun cancelMedicationAlarm(context: Context, medId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_MED_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("DiaWellNotifications", "Cancelled alarm for medId=$medId")
        }
    }
}
