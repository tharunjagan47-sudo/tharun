package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.NotificationsApiService
import com.simats.diawell123.data.model.MarkReadRequest
import com.simats.diawell123.data.model.NotificationsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface NotificationsRepository {
    suspend fun getNotifications(): Result<NotificationsData>
    suspend fun markAsRead(notificationId: Int): Result<Boolean>
    suspend fun clearAll(): Result<Boolean>
}

class NotificationsRepositoryImpl(
    private val apiService: NotificationsApiService = ApiClient.notificationsApiService
) : NotificationsRepository {

    override suspend fun getNotifications(): Result<NotificationsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load notifications." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load notifications (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markAsRead(MarkReadRequest(notificationId))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to mark notification as read."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAll(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.clearAllNotifications()
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to clear notifications."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val jsonObject = Gson().fromJson(errorBody, JsonObject::class.java)
            jsonObject.get("message")?.asString
        } catch (_: Exception) {
            null
        }
    }
}

