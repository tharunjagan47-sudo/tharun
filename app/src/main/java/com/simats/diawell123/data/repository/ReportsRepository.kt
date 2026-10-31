package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.ReportsApiService
import com.simats.diawell123.data.model.WeeklyReportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface ReportsRepository {
    suspend fun getWeeklyReport(endDate: String? = null): Result<WeeklyReportData>
}

class ReportsRepositoryImpl(
    private val apiService: ReportsApiService = ApiClient.reportsApiService
) : ReportsRepository {

    override suspend fun getWeeklyReport(endDate: String?): Result<WeeklyReportData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWeeklyReport(endDate = endDate)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to compile weekly report." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load weekly report (${response.code()})."
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

