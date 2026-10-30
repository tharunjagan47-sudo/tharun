package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a food item in the DiaWell catalog / explore screen.
 */
data class FoodItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("diet_type")
    val dietType: String,

    @SerializedName("is_user_preference")
    val isUserPreference: Boolean = false,

    @SerializedName("preference_tag")
    val preferenceTag: String? = null,

    @SerializedName("portion")
    val portion: String,

    @SerializedName("calories")
    val calories: String,

    @SerializedName("calories_num")
    val caloriesNum: Int = 0,

    @SerializedName("carbs_g")
    val carbsG: Float = 0f,

    @SerializedName("protein_g")
    val proteinG: Float = 0f,

    @SerializedName("fat_g")
    val fatG: Float = 0f,

    @SerializedName("fiber_g")
    val fiberG: Float = 0f,

    @SerializedName("glycemic_index")
    val glycemicIndex: String = "Low",

    @SerializedName("icon")
    val icon: String = "🥣",

    @SerializedName("health_benefits")
    val healthBenefits: String? = null
)

/**
 * API Response for Explore Food endpoint.
 */
data class FoodExploreResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: FoodExploreData? = null
)

data class FoodExploreData(
    @SerializedName("search_query")
    val searchQuery: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("user_preference")
    val userPreference: String? = null,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("foods")
    val foods: List<FoodItem> = emptyList()
)

/**
 * Detailed information for a selected food item.
 */
data class FoodDetailsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: FoodDetailsData? = null
)

data class FoodDetailsData(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("category")
    val category: String = "",

    @SerializedName("diet_type")
    val dietType: String = "",

    @SerializedName("portion")
    val portion: String = "1 serving",

    @SerializedName("calories")
    val calories: Int = 0,

    @SerializedName("calories_label")
    val caloriesLabel: String = "0 kcal",

    @SerializedName("macros")
    val macros: FoodMacros = FoodMacros(),

    @SerializedName("glycemic_index")
    val glycemicIndex: String = "Low",

    @SerializedName("glycemic_details")
    val glycemicDetails: GlycemicDetails = GlycemicDetails(),

    @SerializedName("micronutrients")
    val micronutrients: MicronutrientsData? = null,

    @SerializedName("diabetic_guidance")
    val diabeticGuidance: String? = null,

    @SerializedName("icon")
    val icon: String = "🥣",

    @SerializedName("ingredients")
    val ingredients: List<String> = emptyList(),

    @SerializedName("health_benefits")
    val healthBenefits: String? = null
)

data class FoodMacros(
    @SerializedName("carbs_g")
    val carbsG: Float = 0f,

    @SerializedName("protein_g")
    val proteinG: Float = 0f,

    @SerializedName("fat_g")
    val fatG: Float = 0f,

    @SerializedName("fiber_g")
    val fiberG: Float = 0f,

    @SerializedName("summary")
    val summary: String? = null
)

data class GlycemicDetails(
    @SerializedName("badge")
    val badge: String = "Low Glycemic Impact",

    @SerializedName("color")
    val color: String = "#2E7D32",

    @SerializedName("bg_color")
    val bgColor: String = "#E8F5E9",

    @SerializedName("description")
    val description: String = ""
)

/**
 * Represents a logged meal from food_logs.
 */
data class FoodLogItem(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("category")
    val category: String = "BREAKFAST",

    @SerializedName("time")
    val time: String = "",

    @SerializedName("raw_time")
    val rawTime: String? = null,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("portion")
    val portion: String = "1 serving",

    @SerializedName("calories")
    val calories: String = "0 kcal",

    @SerializedName("calories_num")
    val caloriesNum: Int = 0,

    @SerializedName("carbs_g")
    val carbsG: Float = 0f,

    @SerializedName("protein_g")
    val proteinG: Float = 0f,

    @SerializedName("fat_g")
    val fatG: Float = 0f,

    @SerializedName("icon")
    val icon: String = "🥣"
)

/**
 * Request body for logging a new meal.
 */
data class FoodLogRequest(
    @SerializedName("meal_category")
    val mealCategory: String,

    @SerializedName("food_name")
    val foodName: String,

    @SerializedName("portion")
    val portion: String = "1 serving",

    @SerializedName("calories")
    val calories: Int,

    @SerializedName("carbs_g")
    val carbsG: Float = 0f,

    @SerializedName("protein_g")
    val proteinG: Float = 0f,

    @SerializedName("fat_g")
    val fatG: Float = 0f,

    @SerializedName("log_date")
    val logDate: String? = null,

    @SerializedName("log_time")
    val logTime: String? = null,

    @SerializedName("icon")
    val icon: String = "🥣"
)

/**
 * API Response for Food Log (GET & POST).
 */
data class FoodLogResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: FoodLogData? = null
)

data class FoodLogData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("date")
    val date: String = "",

    @SerializedName("date_label")
    val dateLabel: String = "",

    @SerializedName("summary")
    val summary: DietNutritionSummary = DietNutritionSummary(),

    @SerializedName("meals_count")
    val mealsCount: Int = 0,

    @SerializedName("meals")
    val meals: List<FoodLogItem> = emptyList()
)

data class DietNutritionSummary(
    @SerializedName("total_calories")
    val totalCalories: String = "0 kcal",

    @SerializedName("total_calories_num")
    val totalCaloriesNum: Int = 0,

    @SerializedName("carbs")
    val carbs: String = "0 g",

    @SerializedName("protein")
    val protein: String = "0 g",

    @SerializedName("fat")
    val fat: String = "0 g",

    @SerializedName("macros_label")
    val macrosLabel: String = "Carbs 0 g • Protein 0 g • Fat 0 g"
)

/**
 * API Response for Nutrition Summary endpoint.
 */
data class NutritionSummaryResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: NutritionSummaryData? = null
)

data class NutritionSummaryData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("date")
    val date: String = "",

    @SerializedName("date_label")
    val dateLabel: String = "",

    @SerializedName("calories")
    val calories: CalorieProgress = CalorieProgress(),

    @SerializedName("macros")
    val macros: MacroProgress = MacroProgress(),

    @SerializedName("meals_logged_count")
    val mealsLoggedCount: Int = 0,

    @SerializedName("meals_breakdown")
    val mealsBreakdown: MealsBreakdown = MealsBreakdown()
)

data class CalorieProgress(
    @SerializedName("consumed")
    val consumed: Int = 0,

    @SerializedName("target")
    val target: Int = 1800,

    @SerializedName("remaining")
    val remaining: Int = 1800,

    @SerializedName("consumed_label")
    val consumedLabel: String = "0 kcal",

    @SerializedName("progress_percent")
    val progressPercent: Int = 0
)

data class MacroProgress(
    @SerializedName("carbs_g")
    val carbsG: Float = 0f,

    @SerializedName("protein_g")
    val proteinG: Float = 0f,

    @SerializedName("fat_g")
    val fatG: Float = 0f,

    @SerializedName("summary_label")
    val summaryLabel: String = "Carbs 0 g • Protein 0 g • Fat 0 g"
)

data class MealsBreakdown(
    @SerializedName("breakfast")
    val breakfast: String = "0 kcal",

    @SerializedName("lunch")
    val lunch: String = "0 kcal",

    @SerializedName("snacks")
    val snacks: String = "0 kcal",

    @SerializedName("dinner")
    val dinner: String = "0 kcal"
)

/**
 * Generic response for deletion.
 */
data class SimpleActionResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

/**
 * Micronutrient profile from Indian Food Nutrition Dataset.
 */
data class MicronutrientsData(
    @SerializedName("free_sugar_g")
    val freeSugarG: Float = 0f,

    @SerializedName("sodium_mg")
    val sodiumMg: Float = 0f,

    @SerializedName("calcium_mg")
    val calciumMg: Float = 0f,

    @SerializedName("iron_mg")
    val ironMg: Float = 0f,

    @SerializedName("vitamin_c_mg")
    val vitaminCMg: Float = 0f,

    @SerializedName("folate_ug")
    val folateUg: Float = 0f
)

/**
 * AI Food Recommendation API Response from PHP backend / Python microservice.
 */
data class AiFoodRecommendationResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: AiRecommendationData? = null
)

data class AiRecommendationData(
    @SerializedName("patient_context")
    val patientContext: PatientContext? = null,

    @SerializedName("hypoglycemia_alert")
    val hypoglycemiaAlert: String? = null,

    @SerializedName("allergy_filtering_notes")
    val allergyFilteringNotes: List<String> = emptyList(),

    @SerializedName("recommendations_count")
    val recommendationsCount: Int = 0,

    @SerializedName("recommendations")
    val recommendations: List<RecommendedFoodItem> = emptyList(),

    @SerializedName("daily_plan")
    val dailyPlan: DailyPlanData? = null,

    @SerializedName("disclaimer")
    val disclaimer: String? = null
)

data class DailyPlanData(
    @SerializedName("breakfast")
    val breakfast: List<RecommendedFoodItem> = emptyList(),

    @SerializedName("lunch")
    val lunch: List<RecommendedFoodItem> = emptyList(),

    @SerializedName("snack")
    val snack: List<RecommendedFoodItem> = emptyList(),

    @SerializedName("dinner")
    val dinner: List<RecommendedFoodItem> = emptyList()
)

data class PatientContext(
    @SerializedName("diabetes_type")
    val diabetesType: String? = null,

    @SerializedName("current_glucose_mg_dl")
    val currentGlucoseMgDl: Float? = null,

    @SerializedName("target_glucose_mg_dl")
    val targetGlucoseMgDl: Float? = null,

    @SerializedName("glucose_status")
    val glucoseStatus: String? = null,

    @SerializedName("dietary_preference")
    val dietaryPreference: String? = null,

    @SerializedName("allergies")
    val allergies: List<String> = emptyList(),

    @SerializedName("health_focus")
    val healthFocus: String? = null,

    @SerializedName("activity_level")
    val activityLevel: String? = null,

    @SerializedName("category_filter")
    val categoryFilter: String? = null
)

data class RecommendedFoodItem(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("dish_name")
    val dishName: String? = null,

    @SerializedName("calories")
    val calories: Float? = null,

    @SerializedName("calories_label")
    val caloriesLabel: String = "",

    @SerializedName("carbs_g")
    val carbsG: Float = 0f,

    @SerializedName("protein_g")
    val proteinG: Float = 0f,

    @SerializedName("fat_g")
    val fatG: Float = 0f,

    @SerializedName("free_sugar_g")
    val freeSugarG: Float = 0f,

    @SerializedName("fibre_g")
    val fibreG: Float = 0f,

    @SerializedName("sodium_mg")
    val sodiumMg: Float = 0f,

    @SerializedName("calcium_mg")
    val calciumMg: Float? = null,

    @SerializedName("iron_mg")
    val ironMg: Float? = null,

    @SerializedName("vitamin_c_mg")
    val vitaminCMg: Float? = null,

    @SerializedName("folate_ug")
    val folateUg: Float? = null,

    @SerializedName("category")
    val category: String = "Lunch/Dinner",

    @SerializedName("diet_type")
    val dietType: String = "Vegetarian",

    @SerializedName("portion")
    val portion: String = "100 g portion basis",

    @SerializedName("icon")
    val icon: String = "🥗",

    @SerializedName("recommendation_score")
    val recommendationScore: Int = 85,

    @SerializedName("recommendation_reason")
    val recommendationReason: String = "",

    @SerializedName("highlights")
    val highlights: List<String> = emptyList(),

    @SerializedName("glycemic_impact")
    val glycemicImpact: GlycemicDetails? = null
)


