package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.ProfileApiService
import com.simats.diawell123.data.local.SessionManager
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
        weightKg: Float?,
        phone: String? = null,
        gender: String? = null,
        targetFastingGlucose: Int? = null,
        targetPostmealGlucose: Int? = null
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

    suspend fun updateProfilePic(userId: Int, base64: String): Result<String>
}

class ProfileRepositoryImpl(
    private val apiService: ProfileApiService = ApiClient.profileApiService
) : ProfileRepository {

    private fun getSessionManager(): SessionManager? {
        return try {
            SessionManager.getInstance()
        } catch (_: Exception) {
            null
        }
    }

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
        weightKg: Float?,
        phone: String?,
        gender: String?,
        targetFastingGlucose: Int?,
        targetPostmealGlucose: Int?
    ): Result<ProfileData> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateProfileRequest(
                fullName = fullName,
                email = email,
                phone = phone,
                gender = gender,
                diabetesType = diabetesType,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                targetFastingGlucose = targetFastingGlucose,
                targetPostmealGlucose = targetPostmealGlucose
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
                userId = getSessionManager()?.getUserId(),
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
                userId = getSessionManager()?.getUserId(),
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
        val sm = getSessionManager()
        try {
            val response = apiService.getFoodPreferences()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    sm?.saveFoodPreferences(body.data)
                    return@withContext Result.success(body.data)
                }
            }
            val cached = sm?.getCachedFoodPreferences()
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.success(FoodPreferencesData())
            }
        } catch (_: Exception) {
            val cached = sm?.getCachedFoodPreferences()
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.success(FoodPreferencesData())
            }
        }
    }

    override suspend fun saveFoodPreferences(
        preferredDiet: String,
        allergies: List<String>,
        preferredStyles: List<String>,
        foodsToAvoid: List<String>
    ): Result<FoodPreferencesData> = withContext(Dispatchers.IO) {
        val sm = getSessionManager()
        val localData = FoodPreferencesData(
            userId = sm?.getUserId() ?: 0,
            preferredDiet = preferredDiet,
            allergies = allergies,
            preferredStyles = preferredStyles,
            foodsToAvoid = foodsToAvoid
        )
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
                    sm?.saveFoodPreferences(body.data)
                    return@withContext Result.success(body.data)
                }
            }
            sm?.saveFoodPreferences(localData)
            Result.success(localData)
        } catch (_: Exception) {
            sm?.saveFoodPreferences(localData)
            Result.success(localData)
        }
    }

    override suspend fun uploadProfilePhoto(photoFile: File): Result<UploadPhotoData> = withContext(Dispatchers.IO) {
        try {
            val sessionManager = getSessionManager()
            val userId = sessionManager?.getUserId()
            val mimeType = when (photoFile.extension.lowercase()) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
            val requestBody = photoFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("profile_photo", photoFile.name, requestBody)
            val response = apiService.uploadProfilePhoto(part, userId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    body.data.photoPath?.let { sessionManager?.saveProfilePhoto(it) }
                    body.data.photoUrl?.let { sessionManager?.saveProfilePhotoUrl(it) }
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

    override suspend fun updateProfilePic(userId: Int, base64: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateProfilePic(
                com.simats.diawell123.data.model.UpdateProfilePicRequest(
                    userId = userId,
                    profilePic = base64
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    val pic = body.data?.profilePic ?: body.profilePic ?: base64
                    Result.success(pic)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to update profile pic." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to update profile pic (${response.code()})."
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

