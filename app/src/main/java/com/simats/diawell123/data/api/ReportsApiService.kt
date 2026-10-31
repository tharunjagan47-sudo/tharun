package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.WeeklyReportResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportsApiService {

    @GET("reports/weekly.php")
    suspend fun getWeeklyReport(
        @Query("end_date") endDate: String? = null
    ): Response<WeeklyReportResponse>
}

