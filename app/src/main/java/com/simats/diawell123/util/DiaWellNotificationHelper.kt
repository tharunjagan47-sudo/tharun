package com.simats.diawell123.util

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.simats.diawell123.MainActivity
import com.simats.diawell123.R
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.receiver.MedReminderReceiver
import com.simats.diawell123.ui.meds.MedAlarmActivity

/**
 * Central Notification and Alarm Helper for DiaWell.
 * Strictly separates Normal Notifications (default sound once, no full-screen)
 * from Scheduled Medication Alarms (loud looping alarm audio stream, continuous vibration,
 * and Full-Screen MedAlarmActivity).
 */
object DiaWellNotificationHelper {

    // Notification Channel IDs
    const val CRITICAL_CHANNEL_ID = "diawell_critical_alerts_v1"
    const val GENERAL_CHANNEL_ID = "diawell_general_updates_v1"
    const val ALARM_CHANNEL_ID = "diawell_med_alarm_fullscreen_v3"

    private const val LEGACY_CHANNEL_1 = "diawell_meds_channel"
    private const val LEGACY_CHANNEL_2 = "diawell_meds_channel_v2"
    private const val LEGACY_CHANNEL_3 = "diawell_meds_alarm_v2"
    private const val LEGACY_CHANNEL_4 = "diawell_general_channel"
    private const val LEGACY_CHANNEL_5 = "diawell_med_alarm_v4"

    // Broadcast Intent Actions
    const val ACTION_ALARM_TRIGGER = "com.simats.diawell123.ACTION_ALARM_TRIGGER"
    const val ACTION_MED_REMINDER = "com.simats.diawell123.ACTION_MED_REMINDER"
    const val ACTION_DISMISS_ALARM = "com.simats.diawell123.ACTION_DISMISS_ALARM"
    const val ACTION_TAKEN = "com.simats.diawell123.ACTION_TAKEN"
    const val ACTION_SNOOZE = "com.simats.diawell123.ACTION_SNOOZE"
    const val ACTION_SKIP = "com.simats.diawell123.ACTION_SKIP"
    const val ACTION_DAILY_ROUTINE_REMINDER = "com.simats.diawell123.ACTION_DAILY_ROUTINE_REMINDER"

    // Singleton audio & vibration state
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var stopAlarmRunnable: Runnable? = null

    // Debounce state to prevent duplicate alarm/notification triggers within 5 seconds
    private var lastAlarmTriggerTime: Long = 0L
    private var lastAlarmMedName: String = ""

    /**
     * Initializes notification channels for critical alerts, general updates, and full-screen medication alarms.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // 1. Purge legacy channels to prevent Android caching old sound configs
            try {
                manager.deleteNotificationChannel(LEGACY_CHANNEL_1)
                manager.deleteNotificationChannel(LEGACY_CHANNEL_2)
                manager.deleteNotificationChannel(LEGACY_CHANNEL_3)
                manager.deleteNotificationChannel(LEGACY_CHANNEL_4)
                manager.deleteNotificationChannel(LEGACY_CHANNEL_5)
            } catch (_: Exception) {}

            val notifSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // 2. Channel 1: Critical Health Alerts (IMPORTANCE_HIGH, heads-up banner with standard sound + vibration)
            val criticalChannel = NotificationChannel(
                CRITICAL_CHANNEL_ID,
                "Critical Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent blood sugar warnings for hypoglycemia and hyperglycemia."
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(
                    notifSound,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(criticalChannel)

            // 3. Channel 2: Daily Health & Activity Updates (IMPORTANCE_DEFAULT, plays notification sound once)
            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "Daily Health & Activity Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Standard logs, confirmations, diet/exercise tracking, and routine reminders."
                enableLights(true)
                enableVibration(true)
                setSound(
                    notifSound,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            manager.createNotificationChannel(generalChannel)

            // 4. Channel 3: Full-Screen Medication Alarms (IMPORTANCE_HIGH, silent channel so audio is exclusively controlled by MediaPlayer USAGE_ALARM stream)
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Medication Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen medication alarms with loud ringing audio stream."
                enableLights(true)
                enableVibration(false) // Continuous vibration is managed by startAlarmSound()
                setSound(null, null)  // Loud looping audio is managed by startAlarmSound() MediaPlayer on USAGE_ALARM
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(alarmChannel)

            Log.d("DiaWellNotifications", "Notification channels initialized: $CRITICAL_CHANNEL_ID, $GENERAL_CHANNEL_ID, and $ALARM_CHANNEL_ID")
        }
    }

    // =========================================================================
    // 1. CRITICAL HEALTH ALERTS (High-Priority Heads-Up Banner, Standard Sound + Vibration)
    // =========================================================================

    /**
     * Shows a high-priority critical alert for dangerously low or high glucose readings.
     */
    fun showCriticalHealthAlert(
        context: Context,
        title: String,
        message: String,
        notifId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("DiaWellNotifications", "POST_NOTIFICATIONS not granted. Suppressed critical alert.")
                return
            }
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_screen", "glucose")
            putExtra("notif_id", notifId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        } catch (_: Exception) {
            null
        }

        val notifSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CRITICAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .apply {
                if (largeIcon != null) setLargeIcon(largeIcon)
            }
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message).setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(notifSound)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
            Log.d("DiaWellNotifications", "Critical health alert shown for id=$notifId: $title")
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Failed to show critical health alert: ${e.message}", e)
        }
    }

    // =========================================================================
    // 2. DAILY HEALTH & ACTIVITY UPDATES (Standard chime, NO full-screen)
    // =========================================================================

    /**
     * Shows a standard notification for confirmations, logs, meal/exercise tracking, and routine nudges.
     */
    fun showGeneralNotification(
        context: Context,
        title: String,
        message: String,
        notifId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("DiaWellNotifications", "POST_NOTIFICATIONS not granted. Suppressed general notification.")
                return
            }
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notif_id", notifId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        } catch (_: Exception) {
            null
        }

        val notifSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .apply {
                if (largeIcon != null) setLargeIcon(largeIcon)
            }
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message).setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(notifSound)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
            Log.d("DiaWellNotifications", "General notification shown for id=$notifId: $title")
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Failed to show general notification: ${e.message}", e)
        }
    }

    /**
     * Shows a gentle hydration update notification.
     */
    fun notifyHydrationUpdate(context: Context) {
        showGeneralNotification(
            context = context,
            title = "💧 Hydration Update",
            message = "Water intake updated! Staying hydrated helps your kidneys flush excess sugar.",
            notifId = 8801
        )
    }

    /**
     * Backward-compatible alias for existing callers.
     */
    fun showMedicationNotification(
        context: Context,
        title: String,
        message: String,
        medId: Int = (System.currentTimeMillis() % 10000).toInt(),
        playAudio: Boolean = false
    ) {
        if (playAudio) {
            triggerMedicationAlarm(context, medId, title, message)
        } else {
            showGeneralNotification(context, title, message, medId)
        }
    }

    // =========================================================================
    // 2. SCHEDULED MEDICATION ALARM TRIGGER (Loud looping alarm + Full-Screen Activity)
    // =========================================================================

    /**
     * Triggered ONLY when scheduled alarm clock time hits.
     * Starts looping alarm sound, continuous vibration, builds full-screen notification,
     * and immediately launches MedAlarmActivity.
     */
    fun triggerMedicationAlarm(
        context: Context,
        medId: Int,
        medName: String,
        dosage: String,
        scheduledTime: String = "",
        userId: Int = -1,
        isDaily: Boolean = false
    ) {
        // Prevent duplicate alarms firing within 5 seconds for the same medication
        val now = System.currentTimeMillis()
        if (now - lastAlarmTriggerTime < 5000L && lastAlarmMedName.equals(medName, ignoreCase = true)) {
            Log.w("DiaWellNotifications", "Duplicate triggerMedicationAlarm suppressed for $medName within 5s")
            return
        }
        lastAlarmTriggerTime = now
        lastAlarmMedName = medName

        val resolvedUserId = userId.takeIf { it > 0 } ?: try {
            SessionManager.init(context.applicationContext)
            SessionManager.getInstance().getUserId() ?: -1
        } catch (_: Exception) {
            -1
        }

        createNotificationChannels(context)

        // 1. Start loud looping MediaPlayer on USAGE_ALARM stream + continuous vibration
        startAlarmSound(context)

        // 2. Full-Screen Intent to MedAlarmActivity
        val alarmActivityIntent = Intent(context, MedAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dosage", dosage)
            putExtra("scheduled_time", scheduledTime)
            putExtra("user_id", resolvedUserId)
            putExtra("is_daily", isDaily)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            medId,
            alarmActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action PendingIntents for notification heads-up banner
        val takenIntent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dosage", dosage)
            putExtra("user_id", resolvedUserId)
            putExtra("is_daily", isDaily)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            medId + 10000,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dosage", dosage)
            putExtra("user_id", resolvedUserId)
            putExtra("is_daily", isDaily)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            medId + 20000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_SKIP
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dosage", dosage)
            putExtra("user_id", resolvedUserId)
            putExtra("is_daily", isDaily)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            medId + 30000,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_DISMISS_ALARM
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("user_id", resolvedUserId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            medId + 40000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        } catch (_: Exception) {
            null
        }

        // Clean, non-looping single alert notification (audio & continuous vibration are driven by startAlarmSound)
        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .apply {
                if (largeIcon != null) setLargeIcon(largeIcon)
            }
            .setContentTitle("💊 Time for your Medication!")
            .setContentText("Take $medName ($dosage)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Please take your scheduled dose of $medName ($dosage) now.")
                    .setBigContentTitle("💊 Time for your Medication!")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setDeleteIntent(dismissPendingIntent) // Swiping away stops the alarm without altering status
            .setSound(null) // Audio is exclusively and cleanly handled by the MediaPlayer engine
            .setOnlyAlertOnce(true) // Alerts only once — never re-triggers or creates repeated noise
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Taken", takenPendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 5m", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipPendingIntent)
            .build()

        try {
            // Cancel any prior notification for this medId before posting to prevent notification stacking
            NotificationManagerCompat.from(context).cancel(medId)
            NotificationManagerCompat.from(context).notify(medId, notification)
            Log.d("DiaWellNotifications", "Full-screen alarm notification posted once for id=$medId")
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Failed to post alarm notification: ${e.message}", e)
        }

        // 3. Immediately launch MedAlarmActivity directly to wake up screen
        try {
            context.startActivity(alarmActivityIntent)
            Log.d("DiaWellNotifications", "MedAlarmActivity launched directly.")
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Failed to launch MedAlarmActivity directly: ${e.message}", e)
        }
    }

    // =========================================================================
    // 3. AUDIO & VIBRATION PLAYER ENGINE
    // =========================================================================

    @Synchronized
    fun startAlarmSound(context: Context) {
        stopAlarmSound(context)

        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            Log.d("DiaWellNotifications", "Alarm MediaPlayer playback started on USAGE_ALARM stream.")
        } catch (e: Exception) {
            Log.w("DiaWellNotifications", "MediaPlayer failed (${e.message}), trying Ringtone.")
            try {
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone.play()
            } catch (re: Exception) {
                Log.e("DiaWellNotifications", "Ringtone fallback failed: ${re.message}")
            }
        }

        // Continuous repeating vibration
        try {
            val vibPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(vibPattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(vibPattern, 0)
            }
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Vibrator start failed: ${e.message}")
        }

        // Auto-stop after 30 seconds
        val runnable = Runnable {
            Log.d("DiaWellNotifications", "Alarm auto-stopped after 30s timeout.")
            stopAlarmSound(context)
        }
        stopAlarmRunnable = runnable
        mainHandler.postDelayed(runnable, 30_000L)
    }

    @Synchronized
    fun stopAlarmSound(context: Context) {
        stopAlarmRunnable?.let {
            mainHandler.removeCallbacks(it)
            stopAlarmRunnable = null
        }

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Error releasing MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Error cancelling Vibrator: ${e.message}")
        } finally {
            vibrator = null
        }
    }

    // =========================================================================
    // 4. EXACT ALARM SCHEDULING (AlarmManager.setAlarmClock)
    // =========================================================================

    fun checkAndPromptExactAlarmPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    context,
                    "Please allow Alarms & Reminders for DiaWell to ring on time",
                    Toast.LENGTH_LONG
                ).show()
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                        if (context !is android.app.Activity) {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("DiaWellNotifications", "Failed to launch exact alarm settings: ${e.message}", e)
                }
                return false
            }
        }
        return true
    }

    fun scheduleExactMedicationAlarm(
        context: Context,
        medId: Int,
        medName: String,
        dose: String,
        triggerAtMillis: Long,
        userId: Int = -1,
        isDaily: Boolean = false,
        alarmHour: Int = -1,
        alarmMinute: Int = -1,
        requestCodeOverride: Int = -1
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        checkAndPromptExactAlarmPermission(context)

        val broadcastReqCode = if (requestCodeOverride > 0) requestCodeOverride else medId

        // Only clear matching requestCode so daily snoozes don't wipe tomorrow's recurring alarm
        if (requestCodeOverride > 0) {
            cancelAlarmRequestCode(context, broadcastReqCode)
        } else {
            cancelMedicationAlarm(context, medId)
        }

        // Defensive safety check against past or near-immediate timestamps
        var finalTrigger = triggerAtMillis
        val minValidTime = System.currentTimeMillis() + 5000L
        if (finalTrigger <= minValidTime) {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = finalTrigger }
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            finalTrigger = cal.timeInMillis
            Log.w("DiaWellNotifications", "triggerAtMillis ($triggerAtMillis) was in past or <= 5s. Rolled forward to tomorrow: $finalTrigger")
        }

        val triggerCal = java.util.Calendar.getInstance().apply { timeInMillis = finalTrigger }
        val resolvedHour = if (alarmHour in 0..23) alarmHour else triggerCal.get(java.util.Calendar.HOUR_OF_DAY)
        val resolvedMinute = if (alarmMinute in 0..59) alarmMinute else triggerCal.get(java.util.Calendar.MINUTE)

        val resolvedUserId = userId.takeIf { it > 0 } ?: try {
            SessionManager.init(context.applicationContext)
            SessionManager.getInstance().getUserId() ?: -1
        } catch (_: Exception) {
            -1
        }
        val scheduledTimeLabel = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            .format(java.util.Date(finalTrigger))

        val intent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dose", dose)
            putExtra("scheduled_time", scheduledTimeLabel)
            putExtra("user_id", resolvedUserId)
            putExtra("is_daily", isDaily)
            putExtra("hour", resolvedHour)
            putExtra("minute", resolvedMinute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            broadcastReqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_screen", "medications")
            putExtra("med_id", medId)
        }

        val showPendingIntent = PendingIntent.getActivity(
            context,
            medId,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val clockInfo = AlarmManager.AlarmClockInfo(finalTrigger, showPendingIntent)
            alarmManager.setAlarmClock(clockInfo, pendingIntent)
            Log.d("DiaWellNotifications", "Scheduled AlarmClock for medId=$medId at $finalTrigger")
        } catch (e: SecurityException) {
            Log.w("DiaWellNotifications", "SecurityException on setAlarmClock: ${e.message}. Falling back.")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        finalTrigger,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        finalTrigger,
                        pendingIntent
                    )
                }
            } catch (e2: Exception) {
                Log.e("DiaWellNotifications", "Fallback exact alarm failed: ${e2.message}", e2)
                alarmManager.set(AlarmManager.RTC_WAKEUP, finalTrigger, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("DiaWellNotifications", "Failed to schedule alarm: ${e.message}", e)
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, finalTrigger, pendingIntent)
            } catch (_: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, finalTrigger, pendingIntent)
            }
        }
    }

    /**
     * Reschedules tomorrow's exact daily alarm after today's daily alarm fires.
     */
    fun scheduleNextDailyMedicationAlarm(
        context: Context,
        medId: Int,
        medName: String,
        dose: String,
        hour: Int,
        minute: Int,
        userId: Int = -1
    ) {
        val nextCal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        scheduleExactMedicationAlarm(
            context = context,
            medId = medId,
            medName = medName,
            dose = dose,
            triggerAtMillis = nextCal.timeInMillis,
            userId = userId,
            isDaily = true,
            alarmHour = hour,
            alarmMinute = minute
        )
        Log.d("DiaWellNotifications", "Rescheduled Daily alarm for medId=$medId at tomorrow $hour:$minute")
    }

    /**
     * Snooze medication alarm by scheduling a one-time alarm X minutes later.
     * Uses a separate request code when recurring daily so tomorrow's daily alarm remains intact.
     */
    fun snoozeMedicationAlarm(
        context: Context,
        medId: Int,
        medName: String,
        dose: String,
        snoozeMinutes: Int = 5,
        userId: Int = -1,
        isDaily: Boolean = false
    ) {
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val requestCodeId = if (isDaily) medId + 50000 else medId
        scheduleExactMedicationAlarm(
            context = context,
            medId = medId,
            medName = medName,
            dose = dose,
            triggerAtMillis = triggerTime,
            userId = userId,
            isDaily = false,
            requestCodeOverride = requestCodeId
        )
        Log.d("DiaWellNotifications", "Snoozed medId=$medId (req=$requestCodeId) for $snoozeMinutes minutes")
    }

    private fun cancelAlarmRequestCode(context: Context, reqId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("DiaWellNotifications", "Cancelled alarm for reqId=$reqId")
        }
    }

    fun cancelMedicationAlarm(context: Context, medId: Int) {
        listOf(medId, medId + 50000).forEach { reqId ->
            cancelAlarmRequestCode(context, reqId)
        }
    }

    // =========================================================================
    // 5. AUTOMATED DAILY HEALTH & ROUTINE REMINDERS (8:00 AM, 1:30 PM, 6:00 PM)
    // =========================================================================

    private data class DailyRoutineDef(
        val id: Int,
        val hour: Int,
        val minute: Int,
        val title: String,
        val message: String
    )

    /**
     * Schedules automated daily health check-in nudges at 8:00 AM, 1:30 PM, and 6:00 PM.
     * Rolls over to tomorrow if the time has already passed today.
     */
    fun scheduleDailyHealthReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val routines = listOf(
            DailyRoutineDef(
                id = 8001,
                hour = 8,
                minute = 0,
                title = "🩸 Morning Fasting Check",
                message = "Good morning! Don't forget to log your fasting blood sugar before breakfast."
            ),
            DailyRoutineDef(
                id = 8002,
                hour = 13,
                minute = 30,
                title = "🍛 Healthy Lunch Reminder",
                message = "Time for a balanced low-GI lunch with plenty of fiber and greens."
            ),
            DailyRoutineDef(
                id = 8003,
                hour = 18,
                minute = 0,
                title = "🚶 Evening Walk Time",
                message = "A 20-minute brisk walk right now can significantly improve your evening insulin sensitivity."
            )
        )

        for (routine in routines) {
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, routine.hour)
                set(java.util.Calendar.MINUTE, routine.minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, MedReminderReceiver::class.java).apply {
                action = ACTION_DAILY_ROUTINE_REMINDER
                putExtra("reminder_id", routine.id)
                putExtra("reminder_title", routine.title)
                putExtra("reminder_message", routine.message)
                putExtra("hour", routine.hour)
                putExtra("minute", routine.minute)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                routine.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                }
                Log.d("DiaWellNotifications", "Scheduled daily routine reminder ${routine.title} for ${cal.time}")
            } catch (e: Exception) {
                Log.w("DiaWellNotifications", "Fallback schedule for ${routine.title}: ${e.message}")
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
        }
    }

    /**
     * Reschedules a routine reminder for the next day after it fires.
     */
    fun scheduleSingleRoutineReminder(
        context: Context,
        reminderId: Int,
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, MedReminderReceiver::class.java).apply {
            action = ACTION_DAILY_ROUTINE_REMINDER
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_message", message)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
            Log.d("DiaWellNotifications", "Rescheduled routine reminder $title for tomorrow at ${cal.time}")
        } catch (_: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }
    }
}
