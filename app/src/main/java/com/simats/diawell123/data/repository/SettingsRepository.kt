package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.SettingsApiService
import com.simats.diawell123.data.model.ChangePasswordRequest
import com.simats.diawell123.data.model.DeleteAccountRequest
import com.simats.diawell123.data.model.ExportRecordsData
import com.simats.diawell123.data.model.NotificationPreferencesData
import com.simats.diawell123.data.model.UpdateNotificationPreferencesRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface SettingsRepository {
    suspend fun getNotificationPreferences(): Result<NotificationPreferencesData>
    suspend fun updateNotificationPreferences(
        medicationReminders: Boolean,
        mealLoggingReminders: Boolean,
        glucoseCheckins: Boolean,
        weeklyReportAlerts: Boolean
    ): Result<NotificationPreferencesData>

    suspend fun changePassword(
        currentPass: String,
        newPass: String,
        confirmPass: String
    ): Result<String>

    suspend fun exportRecords(range: String = "all", format: String = "json"): Result<ExportRecordsData>

    suspend fun deleteAccount(password: String): Result<String>
}

class SettingsRepositoryImpl(
    private val apiService: SettingsApiService = ApiClient.settingsApiService
) : SettingsRepository {

    override suspend fun getNotificationPreferences(): Result<NotificationPreferencesData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNotificationPreferences()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to get notification preferences." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to get notification preferences (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationPreferences(
        medicationReminders: Boolean,
        mealLoggingReminders: Boolean,
        glucoseCheckins: Boolean,
        weeklyReportAlerts: Boolean
    ): Result<NotificationPreferencesData> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateNotificationPreferencesRequest(
                medicationReminders = medicationReminders,
                mealLoggingReminders = mealLoggingReminders,
                glucoseCheckins = glucoseCheckins,
                weeklyReportAlerts = weeklyReportAlerts
            )
            val response = apiService.updateNotificationPreferences(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to save notification preferences." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to save notification preferences (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        currentPass: String,
        newPass: String,
        confirmPass: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = ChangePasswordRequest(
                currentPassword = currentPass,
                newPassword = newPass,
                confirmPassword = confirmPass
            )
            val response = apiService.changePassword(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.message.ifEmpty { "Password changed successfully." })
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to change password." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to change password (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportRecords(range: String, format: String): Result<ExportRecordsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.exportRecords(range = range, format = format)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to export records." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to export records (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteAccount(DeleteAccountRequest(password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.message.ifEmpty { "Account deleted successfully." })
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to delete account." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to delete account (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
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

