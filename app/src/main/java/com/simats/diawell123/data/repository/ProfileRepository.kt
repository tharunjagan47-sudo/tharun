package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.ProfileApiService
import com.simats.diawell123.data.model.FoodPreferencesData
import com.simats.diawell123.data.model.HealthPreferencesRequest
import com.simats.diawell123.data.model.PatientDetailsRequest
import com.simats.diawell123.data.model.ProfileData
import com.simats.diawell123.data.model.UpdateFoodPreferencesRequest
import com.simats.diawell123.data.model.UpdateProfileRequest
import com.simats.diawell123.data.model.UploadPhotoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

interface ProfileRepository {
    suspend fun getProfile(): Result<ProfileData>
    suspend fun updateProfile(
        fullName: String,
        email: String,
        diabetesType: String,
        age: Int?,
        heightCm: Float?,
        weightKg: Float?
    ): Result<ProfileData>

    suspend fun savePatientDetails(
        diabetesType: String,
        age: Int?,
        heightCm: Float?,
        weightKg: Float?,
        currentGlucose: Float?,
        targetGlucose: Float?
    ): Result<ProfileData>

    suspend fun saveHealthPreferences(
        healthFocus: String?,
        dietaryPreferences: List<String>,
        allergies: List<String>,
        activityLevel: String?
    ): Result<Boolean>

    suspend fun getFoodPreferences(): Result<FoodPreferencesData>

    suspend fun saveFoodPreferences(
        preferredDiet: String,
        allergies: List<String>,
        preferredStyles: List<String>,
        foodsToAvoid: List<String>
    ): Result<FoodPreferencesData>

    suspend fun uploadProfilePhoto(photoFile: File): Result<UploadPhotoData>
}

class ProfileRepositoryImpl(
    private val apiService: ProfileApiService = ApiClient.profileApiService
) : ProfileRepository {

    override suspend fun getProfile(): Result<ProfileData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load profile." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load profile (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        fullName: String,
        email: String,
        diabetesType: String,
        age: Int?,
        heightCm: Float?,
        weightKg: Float?
    ): Result<ProfileData> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateProfileRequest(
                fullName = fullName,
                email = email,
                diabetesType = diabetesType,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg
            )
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to update profile." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to update profile (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePatientDetails(
        diabetesType: String,
        age: Int?,
        heightCm: Float?,
        weightKg: Float?,
        currentGlucose: Float?,
        targetGlucose: Float?
    ): Result<ProfileData> = withContext(Dispatchers.IO) {
        try {
            val request = PatientDetailsRequest(
                diabetesType = diabetesType,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                currentGlucoseMgDl = currentGlucose,
                targetGlucoseMgDl = targetGlucose
            )
            val response = apiService.savePatientDetails(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to save patient details." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to save patient details (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveHealthPreferences(
        healthFocus: String?,
        dietaryPreferences: List<String>,
        allergies: List<String>,
        activityLevel: String?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = HealthPreferencesRequest(
                healthFocus = healthFocus,
                dietaryPreferences = dietaryPreferences,
                allergies = allergies,
                activityLevel = activityLevel
            )
            val response = apiService.saveHealthPreferences(request)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to save health preferences."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodPreferences(): Result<FoodPreferencesData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFoodPreferences()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load food preferences." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load food preferences (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFoodPreferences(
        preferredDiet: String,
        allergies: List<String>,
        preferredStyles: List<String>,
        foodsToAvoid: List<String>
    ): Result<FoodPreferencesData> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateFoodPreferencesRequest(
                preferredDiet = preferredDiet,
                allergies = allergies,
                preferredStyles = preferredStyles,
                foodsToAvoid = foodsToAvoid
            )
            val response = apiService.saveFoodPreferences(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to save food preferences." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to save food preferences (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfilePhoto(photoFile: File): Result<UploadPhotoData> = withContext(Dispatchers.IO) {
        try {
            val requestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("photo", photoFile.name, requestBody)
            val response = apiService.uploadProfilePhoto(part)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to upload photo." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to upload photo (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
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

