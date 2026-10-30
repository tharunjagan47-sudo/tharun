package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.GlucoseApiService
import com.simats.diawell123.data.model.GlucoseDetailsData
import com.simats.diawell123.data.model.GlucoseHistoryData
import com.simats.diawell123.data.model.LogGlucoseRequest
import com.simats.diawell123.data.model.LoggedGlucoseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface GlucoseRepository {
    suspend fun logGlucose(
        readingValue: Float,
        readingType: String,
        readingDate: String,
        readingTime: String,
        notes: String
    ): Result<LoggedGlucoseData>

    suspend fun getGlucoseHistory(range: String = "7 days"): Result<GlucoseHistoryData>

    suspend fun getReadingDetails(readingId: Int? = null): Result<GlucoseDetailsData>

    suspend fun deleteReading(readingId: Int): Result<Boolean>
}

class GlucoseRepositoryImpl(
    private val apiService: GlucoseApiService = ApiClient.glucoseApiService
) : GlucoseRepository {

    override suspend fun logGlucose(
        readingValue: Float,
        readingType: String,
        readingDate: String,
        readingTime: String,
        notes: String
    ): Result<LoggedGlucoseData> = withContext(Dispatchers.IO) {
        try {
            val request = LogGlucoseRequest(
                readingValue = readingValue,
                readingType = readingType,
                readingDate = readingDate,
                readingTime = readingTime,
                notes = notes
            )
            val response = apiService.logGlucose(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to log glucose reading." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to log glucose reading (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please check connection and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun getGlucoseHistory(range: String): Result<GlucoseHistoryData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGlucoseHistory(range = range)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load glucose history." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load glucose history (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please check connection and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun getReadingDetails(readingId: Int?): Result<GlucoseDetailsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReadingDetails(readingId = readingId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load reading details." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load reading details (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please check connection and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun deleteReading(readingId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteReading(readingId = readingId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to delete reading." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to delete reading (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please check connection and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val obj = Gson().fromJson(errorBody, JsonObject::class.java)
            if (obj.has("message")) obj.get("message").asString else null
        } catch (_: Exception) {
            null
        }
    }
}

