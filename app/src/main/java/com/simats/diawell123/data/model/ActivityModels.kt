package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

/**
 * Server response for activity/recommendations.php
 */
data class ActivityRecommendationsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: ActivityRecommendationsData? = null
)

data class ActivityRecommendationsData(
    @SerializedName("success")
    val isSuccess: Boolean? = true,

    @SerializedName("recommendations")
    val recommendations: List<ActivityRecommendationItem> = emptyList(),

    @SerializedName("user_context")
    val userContext: ActivityUserContext? = null,

    @SerializedName("safety_guidance")
    val safetyGuidance: String? = null,

    @SerializedName("message")
    val message: String? = null
)

data class ActivityRecommendationItem(
    @SerializedName("activity")
    val activity: String = "Walking",

    @SerializedName("icon")
    val icon: String = "🚶‍♂️",

    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,

    @SerializedName("distance_km")
    val distanceKm: Float? = null,

    @SerializedName("speed_kmh")
    val speedKmh: Float? = null,

    @SerializedName("speed_source")
    val speedSource: String? = null,

    @SerializedName("met_value")
    val metValue: Float? = null,

    @SerializedName("intensity")
    val intensity: String = "Moderate",

    @SerializedName("estimated_calories_burned")
    val estimatedCaloriesBurned: Int? = null,

    @SerializedName("compendium_code")
    val compendiumCode: Int? = null,

    @SerializedName("compendium_description")
    val compendiumDescription: String? = null,

    @SerializedName("reason")
    val reason: String = "",

    @SerializedName("safety_note")
    val safetyNote: String = ""
)

data class ActivityUserContext(
    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("height_cm")
    val heightCm: Float? = null,

    @SerializedName("weight_kg")
    val weightKg: Float? = null,

    @SerializedName("bmi")
    val bmi: Float? = null,

    @SerializedName("current_glucose_mg_dl")
    val currentGlucoseMgDl: Float? = null,

    @SerializedName("target_glucose_mg_dl")
    val targetGlucoseMgDl: Float? = null,

    @SerializedName("reading_type")
    val readingType: String? = null,

    @SerializedName("fitness_level")
    val fitnessLevel: String? = null,

    @SerializedName("safety_clearance")
    val safetyClearance: Boolean = true,

    @SerializedName("status_code")
    val statusCode: String? = null
)

/**
 * Payload for logging completed workout
 */
data class LogActivityRequest(
    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("activity_type")
    val activityType: String = "Walking",

    @SerializedName("duration_mins")
    val durationMins: Int = 30,

    @SerializedName("distance_km")
    val distanceKm: Float = 0f,

    @SerializedName("calories_kcal")
    val caloriesKcal: Int = 0,

    @SerializedName("glucose_before")
    val glucoseBefore: Float? = null,

    @SerializedName("glucose_after")
    val glucoseAfter: Float? = null,

    @SerializedName("activity_date")
    val activityDate: String? = null,

    @SerializedName("activity_time")
    val activityTime: String? = null,

    @SerializedName("notes")
    val notes: String? = null
)

data class LogActivityResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: LoggedActivityData? = null
)

data class LoggedActivityData(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("type")
    val type: String = "",

    @SerializedName("duration_mins")
    val durationMins: Int = 0,

    @SerializedName("distance_km")
    val distanceKm: Float = 0f,

    @SerializedName("calories_kcal")
    val caloriesKcal: Int = 0
)

/**
 * Server response for activity/history.php
 */
data class ActivityHistoryResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: ActivityHistoryData? = null
)

data class ActivityHistoryData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("filter")
    val filter: String = "All",

    @SerializedName("summary")
    val summary: ActivityHistorySummary = ActivityHistorySummary(),

    @SerializedName("workouts")
    val workouts: List<ActivityHistoryItem> = emptyList()
)

data class ActivityHistorySummary(
    @SerializedName("total_workouts")
    val totalWorkouts: Int = 0,

    @SerializedName("total_distance_km")
    val totalDistanceKm: Float = 0f,

    @SerializedName("total_calories_kcal")
    val totalCaloriesKcal: Int = 0,

    @SerializedName("average_duration_mins")
    val averageDurationMins: Int = 0
)

data class ActivityHistoryItem(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("type")
    val type: String = "Walking",

    @SerializedName("icon")
    val icon: String = "🚶‍♂️",

    @SerializedName("date")
    val date: String = "",

    @SerializedName("raw_date")
    val rawDate: String = "",

    @SerializedName("durationMins")
    val durationMins: Int = 0,

    @SerializedName("distanceKm")
    val distanceKm: Float = 0f,

    @SerializedName("caloriesKcal")
    val caloriesKcal: Int = 0,

    @SerializedName("glucoseBefore")
    val glucoseBefore: Int = 0,

    @SerializedName("glucoseAfter")
    val glucoseAfter: Int = 0,

    @SerializedName("notes")
    val notes: String = ""
)

