package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.DietApiService
import com.simats.diawell123.data.model.AiRecommendationData
import com.simats.diawell123.data.model.DailyPlanData
import com.simats.diawell123.data.model.FoodDetailsData
import com.simats.diawell123.data.model.FoodExploreData
import com.simats.diawell123.data.model.FoodLogData
import com.simats.diawell123.data.model.FoodLogItem
import com.simats.diawell123.data.model.FoodLogRequest
import com.simats.diawell123.data.model.GlycemicDetails
import com.simats.diawell123.data.model.NutritionSummaryData
import com.simats.diawell123.data.model.PatientContext
import com.simats.diawell123.data.model.RecommendedFoodItem
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
            if (response.isSuccessful && response.body() != null && response.body()!!.success && response.body()!!.data != null && response.body()!!.data!!.recommendations.isNotEmpty()) {
                Result.success(response.body()!!.data!!)
            } else {
                val localData = generateLocalAiRecommendations(
                    limit = limit ?: 10,
                    category = category,
                    currentGlucose = currentGlucose,
                    dietType = dietType,
                    healthFocus = healthFocus
                )
                Result.success(localData)
            }
        } catch (_: Exception) {
            val localData = generateLocalAiRecommendations(
                limit = limit ?: 10,
                category = category,
                currentGlucose = currentGlucose,
                dietType = dietType,
                healthFocus = healthFocus
            )
            Result.success(localData)
        }
    }

    override suspend fun updateHealthInputsAndRecommend(
        glucose: Float,
        dietPreference: String,
        healthFocus: String
    ): Result<AiRecommendationData> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = try { com.simats.diawell123.data.local.SessionManager.getInstance().getUserId() } catch (_: Exception) { null }
            val requestBody = mutableMapOf<String, Any>(
                "update_inputs" to true,
                "current_glucose" to glucose,
                "dietary_preferences" to dietPreference,
                "health_focus" to healthFocus,
                "user_id" to (currentUserId ?: 0)
            )
            val response = apiService.updateHealthInputsAndRecommend(requestBody)
            if (response.isSuccessful && response.body() != null && response.body()!!.success && response.body()!!.data != null && response.body()!!.data!!.recommendations.isNotEmpty()) {
                Result.success(response.body()!!.data!!)
            } else {
                val localData = generateLocalAiRecommendations(
                    limit = 10,
                    category = null,
                    currentGlucose = glucose,
                    dietType = dietPreference,
                    healthFocus = healthFocus
                )
                Result.success(localData)
            }
        } catch (_: Exception) {
            val localData = generateLocalAiRecommendations(
                limit = 10,
                category = null,
                currentGlucose = glucose,
                dietType = dietPreference,
                healthFocus = healthFocus
            )
            Result.success(localData)
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

    private fun generateLocalAiRecommendations(
        limit: Int = 10,
        category: String? = null,
        currentGlucose: Float? = 120f,
        dietType: String? = "Vegetarian",
        healthFocus: String? = "Glucose Control"
    ): AiRecommendationData {
        val glucose = currentGlucose ?: 120f
        val diet = dietType?.takeIf { it.isNotBlank() } ?: "Vegetarian"
        val focus = healthFocus?.takeIf { it.isNotBlank() } ?: "Glucose Control"

        val (glucoseStatus, alert) = when {
            glucose < 70f -> Pair(
                "hypoglycemia",
                "⚠️ Hypoglycemia Warning (${glucose.toInt()} mg/dL): Immediate fast-acting carbs recommended! Consume 15g simple carbs (e.g., fruit juice, glucose powder or raisins). Follow with complex carbs & protein once glucose reaches >70 mg/dL."
            )
            glucose > 180f -> Pair("high", null)
            glucose >= 140f -> Pair("elevated", null)
            else -> Pair("normal", null)
        }

        val masterCatalog = listOf(
            RecommendedFoodItem(
                id = 101,
                title = "Palak Tofu & Saag",
                dishName = "Palak Tofu",
                calories = 180f,
                caloriesLabel = "180 kcal",
                carbsG = 7f,
                proteinG = 16f,
                fatG = 8f,
                freeSugarG = 1f,
                fibreG = 6f,
                sodiumMg = 210f,
                category = "Lunch",
                dietType = "Vegan",
                portion = "1 bowl • 250 g",
                icon = "🍲",
                highlights = listOf("Low GI: 22", "Carbs: 7g", "Fiber: 6g", "Vegan"),
                glycemicImpact = GlycemicDetails(badge = "Ultra Low GI", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 102,
                title = "Methi Moong Dal Soup",
                dishName = "Methi Moong Dal",
                calories = 135f,
                caloriesLabel = "135 kcal",
                carbsG = 12f,
                proteinG = 11f,
                fatG = 3f,
                freeSugarG = 0f,
                fibreG = 7f,
                sodiumMg = 180f,
                category = "Dinner",
                dietType = "Vegan",
                portion = "1 bowl • 220 g",
                icon = "🥣",
                highlights = listOf("Fenugreek Power", "High Fibre", "Satiating"),
                glycemicImpact = GlycemicDetails(badge = "Low Glycemic", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 103,
                title = "Sprouted Moong & Cucumber Salad",
                dishName = "Sprouts Salad",
                calories = 110f,
                caloriesLabel = "110 kcal",
                carbsG = 14f,
                proteinG = 9f,
                fatG = 2f,
                freeSugarG = 2f,
                fibreG = 6f,
                sodiumMg = 120f,
                category = "Snack",
                dietType = "Vegan",
                portion = "1 cup • 180 g",
                icon = "🥗",
                highlights = listOf("Live Enzymes", "Carbs: 14g", "Zero Added Sugar"),
                glycemicImpact = GlycemicDetails(badge = "Low Glycemic", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 104,
                title = "Roasted Chana & Pumpkin Seeds",
                dishName = "Roasted Chana Mix",
                calories = 150f,
                caloriesLabel = "150 kcal",
                carbsG = 15f,
                proteinG = 10f,
                fatG = 5f,
                freeSugarG = 1f,
                fibreG = 5f,
                sodiumMg = 95f,
                category = "Snack",
                dietType = "Vegan",
                portion = "1/2 cup • 50 g",
                icon = "🧆",
                highlights = listOf("Magnesium Rich", "Crunchy Snack", "Low GI"),
                glycemicImpact = GlycemicDetails(badge = "Low Glycemic", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 105,
                title = "Paneer Tikka with Vegetables",
                dishName = "Paneer Tikka",
                calories = 230f,
                caloriesLabel = "230 kcal",
                carbsG = 8f,
                proteinG = 19f,
                fatG = 13f,
                freeSugarG = 2f,
                fibreG = 4f,
                sodiumMg = 240f,
                category = "Lunch",
                dietType = "Vegetarian",
                portion = "1 plate • 200 g",
                icon = "🧀",
                highlights = listOf("High Protein: 19g", "Carbs: 8g", "Keto Friendly"),
                glycemicImpact = GlycemicDetails(badge = "Very Low GI", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 106,
                title = "Greek Yogurt with Almonds & Chia",
                dishName = "Greek Yogurt Bowl",
                calories = 160f,
                caloriesLabel = "160 kcal",
                carbsG = 10f,
                proteinG = 14f,
                fatG = 6f,
                freeSugarG = 4f,
                fibreG = 4f,
                sodiumMg = 70f,
                category = "Breakfast",
                dietType = "Vegetarian",
                portion = "1 cup • 170 g",
                icon = "🫐",
                highlights = listOf("Probiotics", "Omega-3", "High Protein"),
                glycemicImpact = GlycemicDetails(badge = "Low Glycemic", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 107,
                title = "Moong Dal & Spinach Chilla",
                dishName = "Moong Chilla",
                calories = 165f,
                caloriesLabel = "165 kcal",
                carbsG = 18f,
                proteinG = 12f,
                fatG = 4f,
                freeSugarG = 1f,
                fibreG = 5f,
                sodiumMg = 160f,
                category = "Breakfast",
                dietType = "Vegetarian",
                portion = "2 chillas • 160 g",
                icon = "🥞",
                highlights = listOf("Plant Protein", "Low GI: 32", "High Fibre"),
                glycemicImpact = GlycemicDetails(badge = "Low GI", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 108,
                title = "Bajra & Methi Roti with Soya Curry",
                dishName = "Bajra Roti & Soya",
                calories = 220f,
                caloriesLabel = "220 kcal",
                carbsG = 24f,
                proteinG = 18f,
                fatG = 6f,
                freeSugarG = 1f,
                fibreG = 7f,
                sodiumMg = 220f,
                category = "Dinner",
                dietType = "Vegetarian",
                portion = "1 roti + 1 bowl • 280 g",
                icon = "🫓",
                highlights = listOf("Complex Carbs", "High Fibre", "Sustained Energy"),
                glycemicImpact = GlycemicDetails(badge = "Low GI", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 109,
                title = "Egg Bhurji with Spinach & Peppers",
                dishName = "Egg Bhurji",
                calories = 175f,
                caloriesLabel = "175 kcal",
                carbsG = 4f,
                proteinG = 16f,
                fatG = 10f,
                freeSugarG = 1f,
                fibreG = 3f,
                sodiumMg = 230f,
                category = "Breakfast",
                dietType = "Eggetarian",
                portion = "2 eggs • 180 g",
                icon = "🍳",
                highlights = listOf("Zero Sugar", "Carbs: 4g", "High Protein"),
                glycemicImpact = GlycemicDetails(badge = "Ultra Low GI", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 110,
                title = "Boiled Egg White & Avocado Salad",
                dishName = "Egg White Salad",
                calories = 145f,
                caloriesLabel = "145 kcal",
                carbsG = 3f,
                proteinG = 18f,
                fatG = 7f,
                freeSugarG = 1f,
                fibreG = 4f,
                sodiumMg = 190f,
                category = "Snack",
                dietType = "Eggetarian",
                portion = "3 whites • 160 g",
                icon = "🥚",
                highlights = listOf("Lean Protein", "Healthy Fats", "Zero Sugar"),
                glycemicImpact = GlycemicDetails(badge = "Ultra Low GI", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 111,
                title = "Tandoori Grilled Chicken Tikka",
                dishName = "Grilled Chicken Tikka",
                calories = 210f,
                caloriesLabel = "210 kcal",
                carbsG = 3f,
                proteinG = 32f,
                fatG = 7f,
                freeSugarG = 0f,
                fibreG = 2f,
                sodiumMg = 260f,
                category = "Lunch",
                dietType = "Non-Vegetarian",
                portion = "1 plate • 220 g",
                icon = "🍗",
                highlights = listOf("Protein: 32g", "Carbs: 3g", "Zero Sugar"),
                glycemicImpact = GlycemicDetails(badge = "Minimal GI Impact", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 112,
                title = "Chicken & Vegetable Clear Soup",
                dishName = "Chicken Clear Soup",
                calories = 130f,
                caloriesLabel = "130 kcal",
                carbsG = 5f,
                proteinG = 20f,
                fatG = 3f,
                freeSugarG = 1f,
                fibreG = 3f,
                sodiumMg = 210f,
                category = "Dinner",
                dietType = "Non-Vegetarian",
                portion = "1 bowl • 250 g",
                icon = "🍲",
                highlights = listOf("Hydrating", "Low Calorie", "High Protein"),
                glycemicImpact = GlycemicDetails(badge = "Minimal GI Impact", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 113,
                title = "Steamed Fish Tikka with Herbs",
                dishName = "Steamed Fish Tikka",
                calories = 165f,
                caloriesLabel = "165 kcal",
                carbsG = 2f,
                proteinG = 28f,
                fatG = 5f,
                freeSugarG = 0f,
                fibreG = 1f,
                sodiumMg = 200f,
                category = "Dinner",
                dietType = "Non-Vegetarian",
                portion = "1 fillet • 180 g",
                icon = "🐟",
                highlights = listOf("Omega-3", "Protein: 28g", "Carbs: 2g"),
                glycemicImpact = GlycemicDetails(badge = "Minimal GI Impact", color = "#2E7D32", bgColor = "#E8F5E9")
            ),
            RecommendedFoodItem(
                id = 114,
                title = "Fresh Orange Juice & Dates (Hypo Rescue)",
                dishName = "Orange Juice & Dates",
                calories = 115f,
                caloriesLabel = "115 kcal",
                carbsG = 22f,
                proteinG = 2f,
                fatG = 0f,
                freeSugarG = 16f,
                fibreG = 2f,
                sodiumMg = 15f,
                category = "Snack",
                dietType = "Vegan",
                portion = "1 glass • 200 ml",
                icon = "🍊",
                highlights = listOf("Fast Carbs: 22g", "Hypo Rescue", "Vitamin C"),
                glycemicImpact = GlycemicDetails(badge = "Fast Acting Carbs", color = "#E65100", bgColor = "#FFF3E0")
            )
        )

        val dietLower = diet.lowercase()
        val filteredByDiet = masterCatalog.filter { item ->
            val itemDiet = item.dietType.lowercase()
            when {
                dietLower.contains("vegan") -> itemDiet == "vegan"
                dietLower.contains("vegetarian") && !dietLower.contains("non-vegetarian") -> itemDiet == "vegan" || itemDiet == "vegetarian"
                dietLower.contains("eggetarian") -> itemDiet == "vegan" || itemDiet == "vegetarian" || itemDiet == "eggetarian"
                else -> true
            }
        }.let { list ->
            if (dietLower.contains("low-carb") || dietLower.contains("keto")) {
                list.filter { it.carbsG <= 15f }
            } else {
                list
            }
        }

        val baseList = if (filteredByDiet.isNotEmpty()) filteredByDiet else masterCatalog

        val scoredItems = baseList.map { item ->
            var score = 82
            val reason: String

            if (glucose < 70f) {
                if (item.carbsG >= 15f) score += 16 else score -= 10
                reason = "Fast-acting carbs (${item.carbsG.toInt()}g) to safely restore blood sugar above 70 mg/dL."
            } else if (glucose > 180f) {
                if (item.carbsG <= 10f) score += 15
                if (item.fibreG >= 5f) score += 4
                if (item.proteinG >= 15f) score += 3
                reason = "Ultra-low carbs (${item.carbsG.toInt()}g) & ${item.fibreG.toInt()}g fibre to rapidly stabilize high glucose (${glucose.toInt()} mg/dL)."
            } else if (glucose >= 140f) {
                if (item.carbsG <= 18f) score += 10
                if (item.proteinG >= 12f) score += 4
                reason = "Low glycemic index dish (${item.carbsG.toInt()}g carbs) with protein to control elevated glucose (${glucose.toInt()} mg/dL)."
            } else {
                if (focus.contains("Weight", ignoreCase = true) && item.calories != null && item.calories <= 200f) score += 8
                if (focus.contains("Heart", ignoreCase = true) && item.sodiumMg <= 200f) score += 8
                if (focus.contains("Energy", ignoreCase = true) && item.proteinG >= 14f) score += 8
                reason = "Optimal macro balance (${item.proteinG.toInt()}g protein, ${item.carbsG.toInt()}g carbs) tailored for ${focus} at target glucose (${glucose.toInt()} mg/dL)."
            }

            item.copy(
                recommendationScore = score.coerceIn(75, 99),
                recommendationReason = reason
            )
        }.sortedByDescending { it.recommendationScore }

        val finalRecommendations = if (!category.isNullOrBlank() && !category.equals("All", ignoreCase = true)) {
            val catFiltered = scoredItems.filter { it.category.equals(category, ignoreCase = true) }
            if (catFiltered.isNotEmpty()) catFiltered else scoredItems
        } else {
            scoredItems
        }

        val breakfastList = scoredItems.filter { it.category.equals("Breakfast", ignoreCase = true) }.take(2)
            .ifEmpty { scoredItems.take(2) }
        val lunchList = scoredItems.filter { it.category.equals("Lunch", ignoreCase = true) }.take(2)
            .ifEmpty { scoredItems.drop(2).take(2) }
        val snackList = scoredItems.filter { it.category.equals("Snack", ignoreCase = true) }.take(2)
            .ifEmpty { scoredItems.drop(4).take(2) }
        val dinnerList = scoredItems.filter { it.category.equals("Dinner", ignoreCase = true) }.take(2)
            .ifEmpty { scoredItems.drop(6).take(2) }

        val dailyPlan = DailyPlanData(
            breakfast = breakfastList,
            lunch = lunchList,
            snack = snackList,
            dinner = dinnerList
        )

        val patientContext = PatientContext(
            diabetesType = "Type 2",
            currentGlucoseMgDl = glucose,
            targetGlucoseMgDl = 110f,
            glucoseStatus = glucoseStatus,
            dietaryPreference = diet,
            healthFocus = focus,
            categoryFilter = category ?: "All"
        )

        return AiRecommendationData(
            patientContext = patientContext,
            hypoglycemiaAlert = alert,
            allergyFilteringNotes = emptyList(),
            recommendationsCount = finalRecommendations.size,
            recommendations = finalRecommendations.take(limit),
            dailyPlan = dailyPlan,
            disclaimer = "Medical Disclaimer: Suggestions are calculated from nutritional dataset values and personal health context. Always consult your physician or registered dietitian."
        )
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

