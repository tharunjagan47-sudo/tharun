package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.FoodPreferencesResponse
import com.simats.diawell123.data.model.HealthPreferencesRequest
import com.simats.diawell123.data.model.HealthPreferencesResponse
import com.simats.diawell123.data.model.PatientDetailsRequest
import com.simats.diawell123.data.model.PatientDetailsResponse
import com.simats.diawell123.data.model.ProfileDetailsResponse
import com.simats.diawell123.data.model.UpdateFoodPreferencesRequest
import com.simats.diawell123.data.model.UpdateProfileRequest
import com.simats.diawell123.data.model.UpdateProfileResponse
import com.simats.diawell123.data.model.UploadProfilePhotoResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ProfileApiService {

    @GET("auth/editprofile.php")
    suspend fun getProfile(): Response<ProfileDetailsResponse>

    @POST("auth/editprofile.php")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @POST("auth/patientdetails.php")
    suspend fun savePatientDetails(
        @Body request: PatientDetailsRequest
    ): Response<PatientDetailsResponse>

    @POST("auth/healthpreferences.php")
    suspend fun saveHealthPreferences(
        @Body request: HealthPreferencesRequest
    ): Response<HealthPreferencesResponse>

    @GET("auth/foodpreferences.php")
    suspend fun getFoodPreferences(): Response<FoodPreferencesResponse>

    @POST("auth/foodpreferences.php")
    suspend fun saveFoodPreferences(
        @Body request: UpdateFoodPreferencesRequest
    ): Response<FoodPreferencesResponse>

    @Multipart
    @POST("auth/uploadprofile.php")
    suspend fun uploadProfilePhoto(
        @Part photo: MultipartBody.Part
    ): Response<UploadProfilePhotoResponse>
}

