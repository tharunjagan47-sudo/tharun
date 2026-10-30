package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.DeleteGlucoseResponse
import com.simats.diawell123.data.model.GlucoseDetailsResponse
import com.simats.diawell123.data.model.GlucoseHistoryResponse
import com.simats.diawell123.data.model.LogGlucoseRequest
import com.simats.diawell123.data.model.LogGlucoseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GlucoseApiService {

    @POST("glucose/log.php")
    suspend fun logGlucose(
        @Body request: LogGlucoseRequest
    ): Response<LogGlucoseResponse>

    @GET("glucose/history.php")
    suspend fun getGlucoseHistory(
        @Query("range") range: String = "7 days"
    ): Response<GlucoseHistoryResponse>

    @GET("glucose/details.php")
    suspend fun getReadingDetails(
        @Query("reading_id") readingId: Int? = null
    ): Response<GlucoseDetailsResponse>

    @DELETE("glucose/details.php")
    suspend fun deleteReading(
        @Query("reading_id") readingId: Int
    ): Response<DeleteGlucoseResponse>
}

