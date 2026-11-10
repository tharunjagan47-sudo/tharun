package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

/**
 * Notification Preferences
 */
data class NotificationPreferencesResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: NotificationPreferencesData? = null
)

data class NotificationPreferencesData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("medication_reminders")
    val medicationReminders: Boolean = true,

    @SerializedName("meal_logging_reminders")
    val mealLoggingReminders: Boolean = true,

    @SerializedName("glucose_checkins")
    val glucoseCheckins: Boolean = true,

    @SerializedName("weekly_report_alerts")
    val weeklyReportAlerts: Boolean = true,

    @SerializedName("is_customized")
    val isCustomized: Boolean = false
)

data class UpdateNotificationPreferencesRequest(
    @SerializedName("medication_reminders")
    val medicationReminders: Boolean,

    @SerializedName("meal_logging_reminders")
    val mealLoggingReminders: Boolean,

    @SerializedName("glucose_checkins")
    val glucoseCheckins: Boolean,

    @SerializedName("weekly_report_alerts")
    val weeklyReportAlerts: Boolean
)

/**
 * Change Password
 */
data class ChangePasswordRequest(
    @SerializedName("current_password")
    val currentPassword: String,

    @SerializedName("new_password")
    val newPassword: String,

    @SerializedName("confirm_password")
    val confirmPassword: String
)

data class ChangePasswordResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)

/**
 * Export Records
 */
data class ExportRecordsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: ExportRecordsData? = null
)

data class ExportReportOptions(
    val timeRangeLabel: String = "Last 30 Days",
    val daysParam: String = "30",
    val format: String = "PDF",
    val includeGlucose: Boolean = true,
    val includeMeals: Boolean = true,
    val includeMedications: Boolean = true,
    val includeActivity: Boolean = true
)

data class ExportRecordsData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("export_date")
    val exportDate: String = "",

    @SerializedName("range")
    val range: String = "all",

    @SerializedName("days")
    val days: String = "all",

    @SerializedName("patient")
    val patient: ExportPatientProfile = ExportPatientProfile(),

    @SerializedName("summary")
    val summary: ExportSummary = ExportSummary(),

    @SerializedName("glucose_readings")
    val glucoseReadings: List<Map<String, Any>> = emptyList(),

    @SerializedName("food_logs")
    val foodLogs: List<Map<String, Any>> = emptyList(),

    @SerializedName("medications")
    val medications: List<Map<String, Any>> = emptyList(),

    @SerializedName("activity_logs")
    val activityLogs: List<Map<String, Any>> = emptyList(),

    @SerializedName("export_text")
    val exportText: String? = null
)

data class ExportPatientProfile(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("phone")
    val phone: String = "",

    @SerializedName("gender")
    val gender: String = "",

    @SerializedName("blood_group")
    val bloodGroup: String = "",

    @SerializedName("diabetes_type")
    val diabetesType: String = "",

    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("height_cm")
    val heightCm: Float? = null,

    @SerializedName("weight_kg")
    val weightKg: Float? = null,

    @SerializedName("current_glucose")
    val currentGlucose: Float? = null,

    @SerializedName("target_glucose")
    val targetGlucose: Float? = null
)

data class ExportSummary(
    @SerializedName("period_label")
    val periodLabel: String = "",

    @SerializedName("start_date")
    val startDate: String = "",

    @SerializedName("end_date")
    val endDate: String = "",

    @SerializedName("total_glucose_readings")
    val totalGlucoseReadings: Int = 0,

    @SerializedName("average_glucose")
    val averageGlucose: Float = 0f,

    @SerializedName("min_glucose")
    val minGlucose: Float = 0f,

    @SerializedName("max_glucose")
    val maxGlucose: Float = 0f,

    @SerializedName("in_target_percent")
    val inTargetPercent: Int = 0,

    @SerializedName("estimated_hba1c")
    val estimatedHba1c: Float = 0f,

    @SerializedName("total_meals_logged")
    val totalMealsLogged: Int = 0,

    @SerializedName("total_calories")
    val totalCalories: Int = 0,

    @SerializedName("average_daily_calories")
    val averageDailyCalories: Int = 0,

    @SerializedName("total_carbs_g")
    val totalCarbsG: Float = 0f,

    @SerializedName("total_medications")
    val totalMedications: Int = 0,

    @SerializedName("taken_medications")
    val takenMedications: Int = 0,

    @SerializedName("adherence_percent")
    val adherencePercent: Int = 0,

    @SerializedName("total_workouts")
    val totalWorkouts: Int = 0,

    @SerializedName("total_activity_mins")
    val totalActivityMins: Int = 0,

    @SerializedName("total_activity_calories")
    val totalActivityCalories: Int = 0,

    @SerializedName("care_status")
    val careStatus: String = ""
)

/**
 * Delete Account
 */
data class DeleteAccountRequest(
    @SerializedName("password")
    val password: String
)

data class DeleteAccountResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)

