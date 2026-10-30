package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.AiFoodRecommendationResponse
import com.simats.diawell123.data.model.FoodDetailsResponse
import com.simats.diawell123.data.model.FoodExploreResponse
import com.simats.diawell123.data.model.FoodLogRequest
import com.simats.diawell123.data.model.FoodLogResponse
import com.simats.diawell123.data.model.NutritionSummaryResponse
import com.simats.diawell123.data.model.SimpleActionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DietApiService {

    @GET("ai/food_recommendations.php")
    suspend fun getAiFoodRecommendations(
        @Query("limit") limit: Int? = 10,
        @Query("category") category: String? = null,
        @Query("current_glucose") currentGlucose: Float? = null,
        @Query("diet_type") dietType: String? = null,
        @Query("health_focus") healthFocus: String? = null
    ): Response<AiFoodRecommendationResponse>

    @POST("ai/food_recommendations.php")
    suspend fun updateHealthInputsAndRecommend(
        @Body request: Map<String, Any>
    ): Response<AiFoodRecommendationResponse>

    @GET("diet/explore.php")
    suspend fun exploreFoods(
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("diet_type") dietType: String? = null
    ): Response<FoodExploreResponse>

    @GET("diet/details.php")
    suspend fun getFoodDetails(
        @Query("food_id") foodId: Int? = null,
        @Query("dish_name") dishName: String? = null,
        @Query("category") category: String? = null
    ): Response<FoodDetailsResponse>

    @GET("diet/log.php")
    suspend fun getFoodLog(
        @Query("date") date: String? = null
    ): Response<FoodLogResponse>

    @POST("diet/log.php")
    suspend fun logMeal(
        @Body request: FoodLogRequest
    ): Response<FoodLogResponse>

    @DELETE("diet/log.php")
    suspend fun deleteMeal(
        @Query("log_id") logId: Int
    ): Response<SimpleActionResponse>

    @GET("diet/summary.php")
    suspend fun getNutritionSummary(
        @Query("date") date: String? = null
    ): Response<NutritionSummaryResponse>
}

