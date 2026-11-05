package com.simats.diawell123.service

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.SaveFcmTokenRequest
import com.simats.diawell123.util.DiaWellNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging Service for handling incoming remote push notifications
 * and token refreshes.
 */
class DiaWellFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_TOKEN"
        private const val PREFS_NAME = "diawell_fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"

        fun getSavedToken(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_FCM_TOKEN, null)
        }

        fun saveTokenLocally(context: Context, token: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        }

        fun sendTokenToServer(context: Context, token: String) {
            saveTokenLocally(context, token)
            val userId = try {
                SessionManager.getInstance().getUserId()
            } catch (_: Exception) {
                null
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiClient.notificationsApiService.saveFcmToken(
                        SaveFcmTokenRequest(
                            fcmToken = token,
                            userId = userId,
                            deviceType = "Android"
                        )
                    )
                    if (response.isSuccessful) {
                        Log.d(TAG, "FCM token successfully registered on server: $token")
                    } else {
                        Log.w(TAG, "Server responded with HTTP ${response.code()} when saving FCM token.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send FCM token to backend: ${e.message}")
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM registration token: $token")
        sendTokenToServer(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("DiaWellFCM", "Message received from: ${remoteMessage.from}")

        // 1. Extract payload data
        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val medName = data["med_name"] ?: ""
        val dosage = data["dosage"] ?: ""
        val medId = data["med_id"]?.toIntOrNull() ?: (System.currentTimeMillis() % 10000).toInt()

        val rawTitle = notification?.title ?: data["title"] ?: "💊 Medication Reminder"
        val rawBody = notification?.body ?: data["body"] ?: data["message"] ?: run {
            if (medName.isNotBlank()) {
                "Time to take $medName" + (if (dosage.isNotBlank()) " ($dosage)" else "")
            } else {
                "It is time to take your scheduled diabetes medication."
            }
        }

        Log.d("DiaWellFCM", "Displaying FCM notification: Title='$rawTitle', Body='$rawBody'")

        // 2. Display high-priority heads-up notification (works in foreground and background)
        DiaWellNotificationHelper.showMedicationNotification(
            context = applicationContext,
            title = rawTitle,
            message = rawBody,
            medId = medId
        )
    }
}

