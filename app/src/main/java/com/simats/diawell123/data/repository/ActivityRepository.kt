package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ActivityApiService
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.model.ActivityHistoryData
import com.simats.diawell123.data.model.ActivityRecommendationsData
import com.simats.diawell123.data.model.LogActivityRequest
import com.simats.diawell123.data.model.LoggedActivityData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface ActivityRepository {
    suspend fun getActivityRecommendations(
        currentGlucose: Float? = null,
        fitnessLevel: String? = null,
        preferredActivity: String? = null
    ): Result<ActivityRecommendationsData>

    suspend fun logActivity(
        activityType: String,
        durationMins: Int,
        distanceKm: Float,
        caloriesKcal: Int,
        glucoseBefore: Float? = null,
        glucoseAfter: Float? = null,
        notes: String? = null
    ): Result<LoggedActivityData>

    suspend fun getActivityHistory(filter: String? = null): Result<ActivityHistoryData>
}

class ActivityRepositoryImpl(
    private val apiService: ActivityApiService = ApiClient.activityApiService
) : ActivityRepository {

    override suspend fun getActivityRecommendations(
        currentGlucose: Float?,
        fitnessLevel: String?,
        preferredActivity: String?
    ): Result<ActivityRecommendationsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecommendations(
                currentGlucose = currentGlucose,
                fitnessLevel = fitnessLevel,
                preferredActivity = preferredActivity
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "No recommendations returned." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to get activity recommendations (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logActivity(
        activityType: String,
        durationMins: Int,
        distanceKm: Float,
        caloriesKcal: Int,
        glucoseBefore: Float?,
        glucoseAfter: Float?,
        notes: String?
    ): Result<LoggedActivityData> = withContext(Dispatchers.IO) {
        try {
            val request = LogActivityRequest(
                activityType = activityType,
                durationMins = durationMins,
                distanceKm = distanceKm,
                caloriesKcal = caloriesKcal,
                glucoseBefore = glucoseBefore,
                glucoseAfter = glucoseAfter,
                notes = notes
            )
            val response = apiService.logActivity(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to log workout." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to log workout (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActivityHistory(filter: String?): Result<ActivityHistoryData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getActivityHistory(filter = filter)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load activity history." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load activity history (${response.code()})."
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

