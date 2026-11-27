package com.simats.diawell123.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import com.simats.diawell123.R
import com.simats.diawell123.receiver.MedReminderReceiver
import com.simats.diawell123.ui.meds.MedAlarmActivity
import com.simats.diawell123.util.DiaWellNotificationHelper
import java.util.Locale

/**
 * Dedicated Lightweight Foreground Service for Personalized Voice Announcements (Text-to-Speech).
 *
 * When a scheduled medication alarm triggers, this service:
 * 1. Immediately displays the high-priority medication reminder notification as foreground.
 * 2. Requests transient audio focus and ducks alarm ringtone.
 * 3. Speaks a personalized announcement: "Hi [UserName], it's time for your medicine, [MedicineName]."
 * 4. Automatically shuts down TTS and terminates itself upon completion, or immediately when
 *    the user interacts with "Mark as Taken", "Dismiss", or "Snooze".
 */
class MedicineVoiceService : Service(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "MedicineVoiceService"
        const val ACTION_START_VOICE = "com.simats.diawell123.service.ACTION_START_VOICE"
        const val ACTION_STOP_VOICE = "com.simats.diawell123.service.ACTION_STOP_VOICE"

        const val EXTRA_MED_ID = "med_id"
        const val EXTRA_MEDICINE_NAME = "medicine_name"
        const val EXTRA_DOSAGE = "dosage"
        const val EXTRA_USER_NAME = "user_name"

        fun start(
            context: Context,
            medId: Int,
            medicineName: String,
            dosage: String = "",
            userName: String = ""
        ) {
            try {
                val intent = Intent(context, MedicineVoiceService::class.java).apply {
                    action = ACTION_START_VOICE
                    putExtra(EXTRA_MED_ID, medId)
                    putExtra(EXTRA_MEDICINE_NAME, medicineName)
                    putExtra(EXTRA_DOSAGE, dosage)
                    putExtra(EXTRA_USER_NAME, userName)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "Dispatched start to MedicineVoiceService for medId=$medId, med=$medicineName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start MedicineVoiceService: ${e.message}", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, MedicineVoiceService::class.java).apply {
                    action = ACTION_STOP_VOICE
                }
                context.startService(intent)
                Log.d(TAG, "Dispatched stop to MedicineVoiceService")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to stop MedicineVoiceService: ${e.message}")
            }
        }
    }

    private var tts: TextToSpeech? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var activeMedId: Int = 9001
    private var pendingTextToSpeak: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_VOICE
        if (action == ACTION_STOP_VOICE) {
            Log.d(TAG, "ACTION_STOP_VOICE received. Stopping service.")
            stopAndCleanup(removeNotification = true)
            return START_NOT_STICKY
        }

        activeMedId = intent?.getIntExtra(EXTRA_MED_ID, 9001) ?: 9001
        val medicineName = intent?.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Medication"
        val dosage = intent?.getStringExtra(EXTRA_DOSAGE) ?: ""
        val userName = intent?.getStringExtra(EXTRA_USER_NAME)?.trim() ?: ""

        Log.d(TAG, "Starting MedicineVoiceService for medId=$activeMedId, medicine=$medicineName, user=$userName")

        // Spoken script: "Hi $userName, it's time for your medicine, $medicineName."
        // (If userName is blank, say "Hi, it's time for your medicine, $medicineName.")
        pendingTextToSpeak = if (userName.isNotBlank()) {
            "Hi $userName, it's time for your medicine, $medicineName."
        } else {
            "Hi, it's time for your medicine, $medicineName."
        }

        // 1. Post Foreground Notification immediately
        val notification = buildForegroundNotification(activeMedId, medicineName, dosage, userName)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(activeMedId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(activeMedId, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed: ${e.message}", e)
        }

        // 2. Request audio focus
        requestAudioFocus()

        // 3. Failsafe 15-second safety timeout
        val timeout = Runnable {
            Log.d(TAG, "15s failsafe timeout reached. Stopping service.")
            stopAndCleanup(removeNotification = false)
        }
        timeoutRunnable = timeout
        handler.postDelayed(timeout, 15_000L)

        // 4. Initialize TTS
        try {
            tts = TextToSpeech(applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TextToSpeech: ${e.message}", e)
            stopAndCleanup(removeNotification = false)
        }

        return START_NOT_STICKY
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Log.e(TAG, "TTS Initialization failed with status: $status")
            stopAndCleanup(removeNotification = false)
            return
        }

        val localTts = tts ?: return
        try {
            val locale = Locale.getDefault()
            val langResult = localTts.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                localTts.language = Locale.US
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                localTts.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
            }

            // Natural, clear speech rate (0.95f)
            localTts.setSpeechRate(0.95f)

            localTts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "Voice announcement started: $utteranceId")
                    DiaWellNotificationHelper.setAlarmVolume(0.15f) // Duck alarm music
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "Voice announcement completed: $utteranceId")
                    handler.post {
                        DiaWellNotificationHelper.setAlarmVolume(1.0f) // Restore alarm music
                        stopAndCleanup(removeNotification = false)
                    }
                }

                override fun onError(utteranceId: String?) {
                    Log.w(TAG, "Voice announcement error: $utteranceId")
                    handler.post {
                        DiaWellNotificationHelper.setAlarmVolume(1.0f)
                        stopAndCleanup(removeNotification = false)
                    }
                }
            })

            // Subtle 500ms delay before speaking so alarm focus settles nicely
            handler.postDelayed({
                speakNow(localTts, pendingTextToSpeak)
            }, 500L)

        } catch (e: Exception) {
            Log.e(TAG, "Exception configuring TTS: ${e.message}", e)
            stopAndCleanup(removeNotification = false)
        }
    }

    private fun speakNow(localTts: TextToSpeech, text: String) {
        if (text.isBlank()) {
            stopAndCleanup(removeNotification = false)
            return
        }
        val utteranceId = "MED_VOICE_${activeMedId}_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
        }
        val result = localTts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            Log.w(TAG, "TTS speak call failed with code: $result")
            stopAndCleanup(removeNotification = false)
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                Log.d(TAG, "Audio focus lost ($focusChange), stopping speech.")
                stopAndCleanup(removeNotification = false)
            }
        }
        this.audioFocusChangeListener = focusChangeListener

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(focusChangeListener, handler)
                .build()
            this.audioFocusRequest = request
            audioManager?.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioFocusChangeListener?.let { audioManager?.abandonAudioFocus(it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error abandoning audio focus: ${e.message}")
        } finally {
            audioFocusRequest = null
            audioFocusChangeListener = null
        }
    }

    private fun buildForegroundNotification(
        medId: Int,
        medName: String,
        dosage: String,
        userName: String
    ): Notification {
        DiaWellNotificationHelper.createNotificationChannels(this)

        val alarmActivityIntent = Intent(this, MedAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dosage", dosage)
            putExtra("user_name", userName)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            medId,
            alarmActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "Mark as Taken"
        val takenIntent = Intent(this, MedReminderReceiver::class.java).apply {
            action = DiaWellNotificationHelper.ACTION_TAKEN
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dosage", dosage)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            this,
            medId + 10000,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "Dismiss"
        val dismissIntent = Intent(this, MedReminderReceiver::class.java).apply {
            action = DiaWellNotificationHelper.ACTION_DISMISS_ALARM
            putExtra("med_id", medId)
            putExtra("med_name", medName)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            medId + 40000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "Snooze"
        val snoozeIntent = Intent(this, MedReminderReceiver::class.java).apply {
            action = DiaWellNotificationHelper.ACTION_SNOOZE
            putExtra("med_id", medId)
            putExtra("med_name", medName)
            putExtra("med_dosage", dosage)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            medId + 20000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) {
            null
        }

        val doseSubtext = if (dosage.isNotBlank()) " ($dosage)" else ""

        return NotificationCompat.Builder(this, DiaWellNotificationHelper.ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .apply {
                if (largeIcon != null) setLargeIcon(largeIcon)
            }
            .setContentTitle("Time for your Medication!")
            .setContentText("Take $medName$doseSubtext")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Please take your scheduled dose of $medName$doseSubtext now.")
                    .setBigContentTitle("Time for your Medication!")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setDeleteIntent(dismissPendingIntent)
            .setSound(null)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(android.R.drawable.checkbox_on_background, "Mark as Taken", takenPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 5m", snoozePendingIntent)
            .build()
    }

    private fun stopAndCleanup(removeNotification: Boolean) {
        timeoutRunnable?.let {
            handler.removeCallbacks(it)
            timeoutRunnable = null
        }

        DiaWellNotificationHelper.setAlarmVolume(1.0f)
        abandonAudioFocus()

        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down TTS: ${e.message}")
        } finally {
            tts = null
        }

        try {
            if (removeNotification) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(false)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in stopForeground: ${e.message}")
        }

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAndCleanup(removeNotification = false)
    }
}
