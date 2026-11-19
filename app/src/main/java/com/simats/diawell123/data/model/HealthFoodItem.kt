package com.simats.diawell123.data.model

/**
 * Structured dataset item for the AI Food Recommendation Engine.
 * Designed specifically for diabetes wellness, glycemic control, macro-nutrient balancing,
 * and personalized daily meal rotation.
 */
data class HealthFoodItem(
    val id: Int,
    val name: String,
    val mealSlot: String, // "Breakfast", "Lunch", "Snack", "Dinner"
    val calories: Int,
    val carbsGrams: Float,
    val proteinGrams: Float,
    val fiberGrams: Float,
    val fatGrams: Float = 0f,
    val glycemicLoad: String = "Low", // "Low", "Medium"
    val dietType: String = "Vegetarian", // "Vegetarian", "Non-Vegetarian", "Vegan"
    val allergens: List<String> = emptyList(), // "Dairy", "Nuts", "Gluten", "Eggs", "Seafood", "Soy"
    val healthTags: List<String> = emptyList(), // "HighSugarSafe", "LowCarb", "Balanced", "PostWorkout", "LightEvening"
    val healthReason: String = "",
    val portion: String = "1 serving",
    val icon: String = "🥗",
    val isSouthIndian: Boolean = false
)
