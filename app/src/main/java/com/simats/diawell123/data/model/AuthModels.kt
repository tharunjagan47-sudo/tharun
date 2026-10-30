package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data model for Login request body.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * Data model for Register / Signup request body.
 */
data class RegisterRequest(
    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("confirm_password")
    val confirmPassword: String? = null
)

/**
 * Standard API response wrapper for authentication endpoints.
 */
data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: UserData? = null
)

/**
 * User data payload returned upon successful login/signup.
 */
data class UserData(
    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("diabetes_type")
    val diabetesType: String? = null,

    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("height_cm")
    val heightCm: Float? = null,

    @SerializedName("weight_kg")
    val weightKg: Float? = null,

    @SerializedName("current_glucose_mg_dl")
    val currentGlucoseMgDl: Float? = null,

    @SerializedName("target_glucose_mg_dl")
    val targetGlucoseMgDl: Float? = null,

    @SerializedName("profile_photo")
    val profilePhoto: String? = null,

    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String? = null,

    @SerializedName("is_profile_completed")
    val isProfileCompleted: Boolean? = null,

    @SerializedName("next_screen")
    val nextScreen: String? = null,

    @SerializedName("token")
    val token: String? = null
)

