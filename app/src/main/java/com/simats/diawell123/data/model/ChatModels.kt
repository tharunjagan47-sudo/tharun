package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Live Patient Context (Personal Health RAG) passed to Dr. Jarvis.
 */
data class ChatPatientContext(
    @SerializedName("patient_name")
    val patientName: String? = null,

    @SerializedName("latest_glucose")
    val latestGlucose: String? = null,

    @SerializedName("avg_glucose")
    val avgGlucose: Float? = null,

    @SerializedName("estimated_hba1c")
    val estimatedHba1c: Float? = null,

    @SerializedName("glucose_history_count")
    val glucoseHistoryCount: Int = 0,

    @SerializedName("active_medications")
    val activeMedications: String? = null,

    @SerializedName("today_water_glasses")
    val todayWaterGlasses: String? = null,

    @SerializedName("today_calories_in")
    val todayCaloriesIn: String? = null,

    @SerializedName("today_calories_burned")
    val todayCaloriesBurned: String? = null,

    @SerializedName("food_preferences")
    val foodPreferences: String? = null
)

/**
 * Single turn in the conversation memory passed to Dr. Jarvis.
 */
data class ChatHistoryMessage(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)

/**
 * Request payload sent to DiaWell AI /api/chat or ai/chat.php
 * Primary input field is `prompt`. `message` is maintained for compatibility.
 */
data class ChatRequest(
    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("model")
    val model: String? = "qwen2.5:0.5b",

    @SerializedName("temperature")
    val temperature: Double? = 0.3,

    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("message")
    val message: String? = prompt,

    @SerializedName("patient_context")
    val patientContext: ChatPatientContext? = null,

    @SerializedName("history")
    val history: List<ChatHistoryMessage>? = null
)

/**
 * Standardized JSON response returned by the DiaWell AI backend.
 * Structure: {"success": true, "model": "qwen2.5:0.5b", "response": "...", "created_at": "...", "duration_ms": 12039.84}
 */
data class ChatResponse(
    @SerializedName("success")
    val success: Boolean = true,

    @SerializedName("model")
    val model: String? = null,

    @SerializedName("response")
    val response: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("duration_ms")
    val durationMs: Double? = null,

    @SerializedName("message")
    val message: String? = null
) {
    /**
     * Safely extract AI reply text, prioritizing `response` and falling back to `message`.
     */
    fun getCleanResponse(): String {
        return (response?.takeIf { it.isNotBlank() } ?: message ?: "").trim()
    }
}

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
    val isError: Boolean = false,
    val attachedImageUrl: String? = null
)
