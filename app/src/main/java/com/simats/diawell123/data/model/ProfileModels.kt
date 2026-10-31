package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

/**
 * Profile Details (editprofile.php GET)
 */
data class ProfileDetailsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: ProfileData? = null
)

data class ProfileData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("full_name")
    val fullName: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("diabetes_type")
    val diabetesType: String = "Type 2",

    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("height_cm")
    val heightCm: Float? = null,

    @SerializedName("weight_kg")
    val weightKg: Float? = null,

    @SerializedName("profile_photo")
    val profilePhoto: String? = null,

    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String? = null,

    @SerializedName("current_glucose_mg_dl")
    val currentGlucoseMgDl: Float? = null,

    @SerializedName("target_glucose_mg_dl")
    val targetGlucoseMgDl: Float? = null
)

/**
 * Update Personal Profile (editprofile.php POST)
 */
data class UpdateProfileRequest(
    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("diabetes_type")
    val diabetesType: String,

    @SerializedName("age")
    val age: Int?,

    @SerializedName("height_cm")
    val heightCm: Float?,

    @SerializedName("weight_kg")
    val weightKg: Float?
)

data class UpdateProfileResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: ProfileData? = null
)

/**
 * Patient Details (patientdetails.php POST)
 */
data class PatientDetailsRequest(
    @SerializedName("diabetes_type")
    val diabetesType: String = "Type 2",

    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("height_cm")
    val heightCm: Float? = null,

    @SerializedName("weight_kg")
    val weightKg: Float? = null,

    @SerializedName("current_glucose_mg_dl")
    val currentGlucoseMgDl: Float? = null,

    @SerializedName("target_glucose_mg_dl")
    val targetGlucoseMgDl: Float? = null
)

data class PatientDetailsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: ProfileData? = null
)

/**
 * Health Preferences (healthpreferences.php POST)
 */
data class HealthPreferencesRequest(
    @SerializedName("health_focus")
    val healthFocus: String? = null,

    @SerializedName("dietary_preferences")
    val dietaryPreferences: List<String> = emptyList(),

    @SerializedName("allergies")
    val allergies: List<String> = emptyList(),

    @SerializedName("activity_level")
    val activityLevel: String? = null
)

data class HealthPreferencesResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)

/**
 * Food Preferences (foodpreferences.php GET / POST)
 */
data class FoodPreferencesResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: FoodPreferencesData? = null
)

data class FoodPreferencesData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("preferred_diet")
    val preferredDiet: String = "Non-Vegetarian",

    @SerializedName("allergies")
    val allergies: List<String> = emptyList(),

    @SerializedName("preferred_styles")
    val preferredStyles: List<String> = emptyList(),

    @SerializedName("foods_to_avoid")
    val foodsToAvoid: List<String> = emptyList()
)

data class UpdateFoodPreferencesRequest(
    @SerializedName("preferred_diet")
    val preferredDiet: String,

    @SerializedName("allergies")
    val allergies: List<String> = emptyList(),

    @SerializedName("preferred_styles")
    val preferredStyles: List<String> = emptyList(),

    @SerializedName("foods_to_avoid")
    val foodsToAvoid: List<String> = emptyList()
)

/**
 * Upload Profile Photo Response
 */
data class UploadProfilePhotoResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: UploadPhotoData? = null
)

data class UploadPhotoData(
    @SerializedName("photo_path")
    val photoPath: String = "",

    @SerializedName("photo_url")
    val photoUrl: String = ""
)

