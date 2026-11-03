package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Request payload sent to POST DiaWell_backend/ai/chat.php
 */
data class ChatRequest(
    @SerializedName("message")
    val message: String
)

/**
 * Standardized JSON response returned by the DiaWell AI backend.
 * Structure: {"success": true, "message": "...", "response": "..."}
 */
data class ChatResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("response")
    val response: String
)

/**
 * Identifies the message author.
 */
enum class ChatSender {
    USER,
    AI
}

/**
 * Specific clinical diabetes alert classification.
 */
enum class AlertType {
    NONE,
    HYPOGLYCEMIA_WARNING,   // Glucose < 70 mg/dL (Requires fast-acting carbs)
    HYPERGLYCEMIA_WARNING,  // Glucose > 250 mg/dL (Elevated, contraindication)
    KETONE_WARNING,         // Ketone/DKA warning
    GENERAL_CLINICAL_ALERT  // General medication or clinical advisory
}

/**
 * Badge categorization for extracted macros & health metrics.
 */
enum class BadgeType {
    HIGH_FIBRE,
    CALORIES,
    CARBOHYDRATES,
    PROTEIN,
    MET_ACTIVITY,
    GENERAL
}

/**
 * Structured metric chip for macros, calories, or MET values.
 */
data class NutritionMetric(
    val label: String,
    val value: String,
    val isHighlight: Boolean = false,
    val badgeType: BadgeType = BadgeType.GENERAL
)

/**
 * UI State representation of a single chat message bubble.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val alertType: AlertType = AlertType.NONE,
    val metrics: List<NutritionMetric> = emptyList(),
    val isError: Boolean = false
)

