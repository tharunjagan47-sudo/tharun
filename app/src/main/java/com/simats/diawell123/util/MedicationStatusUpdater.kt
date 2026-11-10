package com.simats.diawell123.util

import android.content.Context
import android.util.Log
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.UpdateMedicationStatusRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MedStatusChangedEvent(
    val medId: Int,
    val medName: String,
    val status: String,
    val newTimeLabel: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Coordinates medication status updates ("Taken", "Skipped", "Snoozed", "Upcoming")
 * from Alarm screen, Notification actions, and in-app screens so both MySQL backend
 * and active ViewModels stay synchronized in real time.
 */
object MedicationStatusUpdater {

    private const val TAG = "MedStatusUpdater"

    private val _events = MutableSharedFlow<MedStatusChangedEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events: SharedFlow<MedStatusChangedEvent> = _events.asSharedFlow()

    fun emitLocalChange(
        medId: Int,
        medName: String,
        status: String,
        newTimeLabel: String? = null
    ) {
        _events.tryEmit(
            MedStatusChangedEvent(
                medId = medId,
                medName = medName,
                status = status,
                newTimeLabel = newTimeLabel
            )
        )
    }

    /**
     * Computes local snoozed time strings for both DB storage ("HH:mm:ss") and UI display ("h:mm a").
     */
    fun computeSnoozeTimeStrings(snoozeMinutes: Int): Pair<String, String> {
        val targetMillis = System.currentTimeMillis() + (snoozeMinutes * 60_000L)
        val dbTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(targetMillis))
        val displayTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(targetMillis))
        return dbTime to displayTime
    }

    /**
     * Persists medication status to the PHP backend and broadcasts an immediate sync event.
     * Safe to invoke from BroadcastReceiver (inside goAsync) or Activity coroutines.
     */
    suspend fun updateStatusAndNotify(
        context: Context,
        medId: Int,
        medName: String,
        status: String,
        explicitUserId: Int = -1,
        snoozeMinutes: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val resolvedUserId = try {
            SessionManager.init(appContext)
            explicitUserId.takeIf { it > 0 } ?: SessionManager.getInstance().getUserId()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve userId: ${e.message}")
            explicitUserId.takeIf { it > 0 }
        }

        val (newDbTime, newDisplayTime) = if (status.equals("Snoozed", ignoreCase = true) && snoozeMinutes > 0) {
            computeSnoozeTimeStrings(snoozeMinutes)
        } else {
            null to null
        }

        // Emit immediate optimistic update for any active UI screens
        emitLocalChange(
            medId = medId,
            medName = medName,
            status = status,
            newTimeLabel = newDisplayTime
        )

        if (resolvedUserId == null || resolvedUserId <= 0) {
            Log.w(TAG, "Skipping remote update: no logged-in user_id for medId=$medId")
            return@withContext false
        }

        val success = withTimeoutOrNull(4500L) {
            try {
                val request = UpdateMedicationStatusRequest(
                    medicationId = medId,
                    status = status,
                    userId = resolvedUserId,
                    medicationName = medName.takeIf { it.isNotBlank() },
                    snoozeMinutes = snoozeMinutes.takeIf { it > 0 },
                    newReminderTime = newDbTime
                )
                val response = ApiClient.medsApiService.updateMedicationStatus(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "Server updated medId=$medId ($medName) to status=$status")
                    true
                } else {
                    Log.w(
                        TAG,
                        "Server rejected status update code=${response.code()} msg=${response.body()?.message ?: response.errorBody()?.string()}"
                    )
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error updating med status: ${e.message}", e)
                false
            }
        } ?: false

        // Re-emit after server confirmation so active ViewModels pull authoritative DB state
        emitLocalChange(
            medId = medId,
            medName = medName,
            status = status,
            newTimeLabel = newDisplayTime
        )

        success
    }
}

