package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.ChangePasswordRequest
import com.simats.diawell123.data.model.ChangePasswordResponse
import com.simats.diawell123.data.model.DeleteAccountRequest
import com.simats.diawell123.data.model.DeleteAccountResponse
import com.simats.diawell123.data.model.ExportRecordsResponse
import com.simats.diawell123.data.model.NotificationPreferencesResponse
import com.simats.diawell123.data.model.UpdateNotificationPreferencesRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SettingsApiService {

    @GET("settings/notification_preferences.php")
    suspend fun getNotificationPreferences(): Response<NotificationPreferencesResponse>

    @POST("settings/notification_preferences.php")
    suspend fun updateNotificationPreferences(
        @Body request: UpdateNotificationPreferencesRequest
    ): Response<NotificationPreferencesResponse>

    @POST("settings/change_password.php")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @GET("settings/export_records.php")
    suspend fun exportRecords(
        @Query("range") range: String = "all",
        @Query("format") format: String = "json"
    ): Response<ExportRecordsResponse>

    @POST("settings/delete_account.php")
    suspend fun deleteAccount(
        @Body request: DeleteAccountRequest
    ): Response<DeleteAccountResponse>
}

