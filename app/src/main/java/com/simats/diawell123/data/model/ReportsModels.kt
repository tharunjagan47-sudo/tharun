package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

data class WeeklyReportResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: WeeklyReportData? = null
)

data class WeeklyReportData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("week_label")
    val weekLabel: String = "",

    @SerializedName("start_date")
    val startDate: String = "",

    @SerializedName("end_date")
    val endDate: String = "",

    @SerializedName("glucose")
    val glucose: WeeklyGlucoseSummary = WeeklyGlucoseSummary(),

    @SerializedName("nutrition")
    val nutrition: WeeklyNutritionSummary = WeeklyNutritionSummary(),

    @SerializedName("medication")
    val medication: WeeklyMedicationSummary = WeeklyMedicationSummary(),

    @SerializedName("activity")
    val activity: WeeklyActivitySummary = WeeklyActivitySummary(),

    @SerializedName("chart_data")
    val chartData: List<WeeklyGlucoseChartPoint> = emptyList()
)

data class WeeklyGlucoseSummary(
    @SerializedName("average")
    val average: String = "120 mg/dL",

    @SerializedName("average_num")
    val averageNum: Float = 120f,

    @SerializedName("time_in_range_percent")
    val timeInRangePercent: Int = 78,

    @SerializedName("above_target_percent")
    val aboveTargetPercent: Int = 14,

    @SerializedName("below_target_percent")
    val belowTargetPercent: Int = 8,

    @SerializedName("summary_tag")
    val summaryTag: String = "Optimal Glycemic Control"
)

data class WeeklyNutritionSummary(
    @SerializedName("total_meals_logged")
    val totalMealsLogged: Int = 0,

    @SerializedName("total_calories")
    val totalCalories: String = "0 kcal",

    @SerializedName("daily_average_calories")
    val dailyAverageCalories: String = "1,450 kcal"
)

data class WeeklyMedicationSummary(
    @SerializedName("adherence_percent")
    val adherencePercent: Int = 0,

    @SerializedName("status_label")
    val statusLabel: String = ""
)

data class WeeklyActivitySummary(
    @SerializedName("total_active_mins")
    val totalActiveMins: Int = 0,

    @SerializedName("workout_count")
    val workoutCount: Int = 0,

    @SerializedName("summary_label")
    val summaryLabel: String = ""
)

data class WeeklyGlucoseChartPoint(
    @SerializedName("day")
    val day: String = "",

    @SerializedName("date")
    val date: String = "",

    @SerializedName("average_glucose")
    val averageGlucose: Float = 120f
)

