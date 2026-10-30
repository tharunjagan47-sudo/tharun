package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.DashboardResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApiService {

    @GET("home/dashboard.php")
    suspend fun getDashboardData(
        @Query("user_id") userId: Int? = null
    ): Response<DashboardResponse>
}

