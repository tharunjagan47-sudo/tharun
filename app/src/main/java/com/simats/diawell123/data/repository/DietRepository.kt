package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.DietApiService
import com.simats.diawell123.data.model.AiRecommendationData
import com.simats.diawell123.data.model.FoodDetailsData
import com.simats.diawell123.data.model.FoodExploreData
import com.simats.diawell123.data.model.FoodLogData
import com.simats.diawell123.data.model.FoodLogItem
import com.simats.diawell123.data.model.FoodLogRequest
import com.simats.diawell123.data.model.NutritionSummaryData
import com.simats.diawell123.data.model.SimpleActionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface DietRepository {
    suspend fun getAiFoodRecommendations(
        limit: Int? = 10,
        category: String? = null,
        currentGlucose: Float? = null,
        dietType: String? = null,
        healthFocus: String? = null
    ): Result<AiRecommendationData>

    suspend fun updateHealthInputsAndRecommend(
        glucose: Float,
        dietPreference: String,
        healthFocus: String
    ): Result<AiRecommendationData>

    suspend fun exploreFoods(query: String? = null, category: String? = null, dietType: String? = null): Result<FoodExploreData>
    suspend fun getFoodDetails(foodId: Int? = null, dishName: String? = null, category: String? = null): Result<FoodDetailsData>
    suspend fun getFoodLog(date: String? = null): Result<FoodLogData>
    suspend fun logMeal(request: FoodLogRequest): Result<FoodLogItem>
    suspend fun deleteMeal(logId: Int): Result<Boolean>
    suspend fun getNutritionSummary(date: String? = null): Result<NutritionSummaryData>
}

class DietRepositoryImpl(
    private val apiService: DietApiService = ApiClient.dietApiService
) : DietRepository {

    override suspend fun getAiFoodRecommendations(
        limit: Int?,
        category: String?,
        currentGlucose: Float?,
        dietType: String?,
        healthFocus: String?
    ): Result<AiRecommendationData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAiFoodRecommendations(
                limit = limit ?: 10,
                category = category?.takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) },
                currentGlucose = currentGlucose,
                dietType = dietType?.takeIf { it.isNotBlank() },
                healthFocus = healthFocus?.takeIf { it.isNotBlank() }
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load AI recommendations." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load AI recommendations (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify network and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun updateHealthInputsAndRecommend(
        glucose: Float,
        dietPreference: String,
        healthFocus: String
    ): Result<AiRecommendationData> = withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf(
                "update_inputs" to true,
                "current_glucose" to glucose,
                "dietary_preferences" to dietPreference,
                "health_focus" to healthFocus
            )
            val response = apiService.updateHealthInputsAndRecommend(requestBody)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to update health inputs." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to update health inputs (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify network and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun exploreFoods(
        query: String?,
        category: String?,
        dietType: String?
    ): Result<FoodExploreData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.exploreFoods(
                query = query?.takeIf { it.isNotBlank() },
                category = category?.takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) },
                dietType = dietType?.takeIf { it.isNotBlank() }
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load foods." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to explore foods (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify network and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun getFoodDetails(
        foodId: Int?,
        dishName: String?,
        category: String?
    ): Result<FoodDetailsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFoodDetails(
                foodId = foodId?.takeIf { it > 0 },
                dishName = dishName?.takeIf { it.isNotBlank() },
                category = category?.takeIf { it.isNotBlank() }
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Food details not found." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load food details (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify network and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun getFoodLog(date: String?): Result<FoodLogData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFoodLog(date = date?.takeIf { it.isNotBlank() })
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load food log." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load food log (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify network and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun logMeal(request: FoodLogRequest): Result<FoodLogItem> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.logMeal(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    val loggedItem = FoodLogItem(
                        id = body.data?.meals?.firstOrNull()?.id ?: 0,
                        category = request.mealCategory,
                        time = request.logTime ?: "",
                        title = request.foodName,
                        portion = request.portion,
                        calories = "${request.calories} kcal",
                        caloriesNum = request.calories,
                        carbsG = request.carbsG,
                        proteinG = request.proteinG,
                        fatG = request.fatG,
                        icon = request.icon
                    )
                    Result.success(loggedItem)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to log meal." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to log meal (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify network and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun deleteMeal(logId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMeal(logId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to delete meal." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to delete meal (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify network and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun getNutritionSummary(date: String?): Result<NutritionSummaryData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNutritionSummary(date = date?.takeIf { it.isNotBlank() })
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load nutrition summary." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to load nutrition summary (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify network and backend."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val parsed = Gson().fromJson(errorBody, SimpleActionResponse::class.java)
            parsed.message
        } catch (_: Exception) {
            null
        }
    }
}

