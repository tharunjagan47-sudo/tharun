package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.MarkReadRequest
import com.simats.diawell123.data.model.NotificationsResponse
import com.simats.diawell123.data.model.SimpleNotificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NotificationsApiService {

    @GET("notifications/list.php")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @POST("notifications/mark_read.php")
    suspend fun markAsRead(
        @Body request: MarkReadRequest
    ): Response<SimpleNotificationResponse>

    @POST("notifications/clear_all.php")
    suspend fun clearAllNotifications(): Response<SimpleNotificationResponse>
}

