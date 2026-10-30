package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.ActivityHistoryResponse
import com.simats.diawell123.data.model.ActivityRecommendationsResponse
import com.simats.diawell123.data.model.LogActivityRequest
import com.simats.diawell123.data.model.LogActivityResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ActivityApiService {

    @GET("activity/recommendations.php")
    suspend fun getRecommendations(
        @Query("current_glucose") currentGlucose: Float? = null,
        @Query("fitness_level") fitnessLevel: String? = null,
        @Query("preferred_activity") preferredActivity: String? = null
    ): Response<ActivityRecommendationsResponse>

    @POST("activity/log.php")
    suspend fun logActivity(
        @Body request: LogActivityRequest
    ): Response<LogActivityResponse>

    @GET("activity/history.php")
    suspend fun getActivityHistory(
        @Query("filter") filter: String? = null
    ): Response<ActivityHistoryResponse>
}

