package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

/**
 * Server response for Home Dashboard API.
 */
data class DashboardResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: DashboardData? = null
)

/**
 * Dashboard core data payload.
 */
data class DashboardData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("greeting")
    val greeting: String = "",

    @SerializedName("wellness_subtitle")
    val wellnessSubtitle: String = "Daily Wellness • 94% On Track",

    @SerializedName("streak_badge")
    val streakBadge: String = "🔥 7 Day Logging Streak",

    @SerializedName("target_badge")
    val targetBadge: String = "🎯 Fasting Target Met",

    @SerializedName("ai_tip")
    val aiTip: HomeAiTip? = null,

    @SerializedName("routine")
    val routine: List<HomeRoutineItem> = emptyList(),

    @SerializedName("profile")
    val profile: HomeProfile? = null,

    @SerializedName("metrics")
    val metrics: HomeMetrics? = null
)

/**
 * AI daily tip for the patient.
 */
data class HomeAiTip(
    @SerializedName("title")
    val title: String = "Dr. JARVIS Tip Of The Day ⚡",

    @SerializedName("tip")
    val tip: String = "A 10-minute light walk after meals helps smooth out blood sugar levels effectively!"
)

/**
 * Daily routine timeline check-in item.
 */
data class HomeRoutineItem(
    @SerializedName("icon")
    val icon: String = "✓",

    @SerializedName("label")
    val label: String = "",

    @SerializedName("is_done")
    val isDone: Boolean = false
)

/**
 * Patient profile summary on the dashboard.
 */
data class HomeProfile(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("diabetes_type")
    val diabetesType: String = "Type 2",

    @SerializedName("photo_url")
    val photoUrl: String? = null
)

/**
 * Dashboard metric cards grouping.
 */
data class HomeMetrics(
    @SerializedName("glucose")
    val glucose: HomeMetricItem? = null,

    @SerializedName("nutrition")
    val nutrition: HomeMetricItem? = null,

    @SerializedName("medication")
    val medication: HomeMetricItem? = null,

    @SerializedName("weekly_progress")
    val weeklyProgress: HomeMetricItem? = null
)

/**
 * Individual metric card model.
 */
data class HomeMetricItem(
    @SerializedName("title")
    val title: String = "",

    @SerializedName("value")
    val value: String = "",

    @SerializedName("value_num")
    val valueNum: Double? = null,

    @SerializedName("subtitle")
    val subtitle: String = "",

    @SerializedName("status_text")
    val statusText: String = "",

    @SerializedName("status_bg")
    val statusBg: String = "#E8F5E9",

    @SerializedName("status_color")
    val statusColor: String = "#2E7D32"
)

