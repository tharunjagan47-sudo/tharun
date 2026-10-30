package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.HomeApiService
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.DashboardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface HomeRepository {
    suspend fun getDashboardData(): Result<DashboardData>
}

class HomeRepositoryImpl(
    private val apiService: HomeApiService = ApiClient.homeApiService
) : HomeRepository {

    override suspend fun getDashboardData(): Result<DashboardData> = withContext(Dispatchers.IO) {
        try {
            val userId = try {
                SessionManager.getInstance().getUserId()
            } catch (_: Exception) {
                null
            }

            val response = apiService.getDashboardData(userId = userId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load dashboard data." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load dashboard data (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and local backend.", e))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "An unexpected error occurred while loading dashboard.", e))
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = Gson().fromJson(errorBody, JsonObject::class.java)
            when {
                json.has("message") -> json.get("message").asString
                json.has("error") -> json.get("error").asString
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

