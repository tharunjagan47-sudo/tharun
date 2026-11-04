package com.simats.diawell123.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.AlertType
import com.simats.diawell123.data.model.BadgeType
import com.simats.diawell123.data.model.ChatMessage
import com.simats.diawell123.data.model.ChatSender
import com.simats.diawell123.data.model.NutritionMetric
import com.simats.diawell123.data.repository.ChatRepository
import com.simats.diawell123.data.repository.ChatRepositoryImpl
import com.simats.diawell123.ui.profile.ProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * ViewModel managing state for Dr. Jarvis AI Chatbot.
 * Maintains message history, loading indicator, network error states,
 * and clinical alert/macro extraction.
 */
class ChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastUserMessage: String? = null

    init {
        initWelcomeMessage()
    }

    private fun initWelcomeMessage() {
        val userName = ProfileState.userName.ifBlank { "there" }
        val greeting = ChatMessage(
            sender = ChatSender.AI,
            text = "Hello $userName! I am Dr. Jarvis, your DiaWell diabetes wellness and clinical lifestyle assistant. ⚡🩺\n\nAsk me about verified high-fibre Indian foods, meal planning, safe physical activity guidelines, or blood glucose target management.",
            alertType = AlertType.NONE,
            metrics = emptyList()
        )
        _messages.value = listOf(greeting)
    }

    /**
     * Send user message to Dr. Jarvis AI backend.
     */
    fun sendMessage(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        lastUserMessage = trimmed
        _errorMessage.value = null

        // Append User Message to StateFlow
        val userMsg = ChatMessage(
            sender = ChatSender.USER,
            text = trimmed
        )
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.sendMessage(trimmed)

                result.fold(
                    onSuccess = { chatResponse ->
                        val responseText = chatResponse.getCleanResponse()
                        if (responseText.isNotBlank()) {
                            val alertType = detectAlertType(responseText)
                            val metrics = extractNutritionMetrics(responseText)

                            val aiMsg = ChatMessage(
                                sender = ChatSender.AI,
                                text = responseText,
                                alertType = alertType,
                                metrics = metrics
                            )
                            _messages.value = _messages.value + aiMsg
                        } else {
                            val errorMsg = "Dr. Jarvis returned an empty response. Please try rephrasing your question."
                            _errorMessage.value = errorMsg
                            _messages.value = _messages.value + ChatMessage(
                                sender = ChatSender.AI,
                                text = "⚠️ $errorMsg",
                                isError = true
                            )
                        }
                    },
                    onFailure = { error ->
                        val errorMsg = error.localizedMessage ?: "Could not reach Dr. Jarvis."
                        _errorMessage.value = errorMsg

                        val errorAiMsg = ChatMessage(
                            sender = ChatSender.AI,
                            text = "⚠️ $errorMsg\n\nPlease check your network connection or tap Retry.",
                            isError = true
                        )
                        _messages.value = _messages.value + errorAiMsg
                    }
                )
            } catch (t: Throwable) {
                val errorMsg = t.localizedMessage ?: "An unexpected error occurred."
                _errorMessage.value = errorMsg

                val errorAiMsg = ChatMessage(
                    sender = ChatSender.AI,
                    text = "⚠️ $errorMsg\n\nPlease check your network connection or tap Retry.",
                    isError = true
                )
                _messages.value = _messages.value + errorAiMsg
            } finally {
                // Guaranteed to execute in every situation: success, failure, timeout, JSON error, or exception
                _isLoading.value = false
            }
        }
    }

    /**
     * Retry the most recent failed message.
     */
    fun retryLastMessage() {
        lastUserMessage?.let { query ->
            _errorMessage.value = null
            // Remove the trailing error message if present to keep the UI clean
            val currentList = _messages.value
            if (currentList.isNotEmpty() && currentList.last().isError) {
                _messages.value = currentList.dropLast(1)
            }
            sendMessage(query)
        }
    }

    /**
     * Clear active error message banner.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Reset chat conversation history.
     */
    fun resetChat() {
        _errorMessage.value = null
        initWelcomeMessage()
    }

    // -------------------------------------------------------------------------
    // Diabetes Highlighting & Metric Extraction
    // -------------------------------------------------------------------------

    companion object {
        /**
         * Detect emergency clinical keywords/phrases in AI response text.
         */
        fun detectAlertType(text: String): AlertType {
            val lower = text.lowercase()

            return when {
                // Hypoglycemia emergency triggers
                lower.contains("< 70") || lower.contains("<70") ||
                lower.contains("hypoglycemia") || lower.contains("low sugar") ||
                lower.contains("fast-acting carb") -> AlertType.HYPOGLYCEMIA_WARNING

                // Ketone / DKA emergency triggers
                lower.contains("ketone") || lower.contains("ketoacidosis") || lower.contains("dka") ->
                    AlertType.KETONE_WARNING

                // Hyperglycemia warning triggers
                lower.contains("> 250") || lower.contains(">250") ||
                lower.contains(">= 300") || lower.contains(">=300") ||
                lower.contains("hyperglycemia") || lower.contains("severe hyperglycemia") ->
                    AlertType.HYPERGLYCEMIA_WARNING

                // Contraindicated or urgent doctor notice
                lower.contains("contraindicated") || lower.contains("seek emergency") ||
                lower.contains("consult your doctor immediately") -> AlertType.GENERAL_CLINICAL_ALERT

                else -> AlertType.NONE
            }
        }

        /**
         * Extract macronutrient and MET activity metrics for structured UI badge chips.
         */
        fun extractNutritionMetrics(text: String): List<NutritionMetric> {
            val metrics = mutableListOf<NutritionMetric>()
            val lower = text.lowercase()

            // 1. Fibre extraction (e.g., "5.1g fibre", "4.6g fiber", "13.4g fibre per 100g")
            val fibreMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*g?\\s*(?:of\\s*)?fib(?:er|re)").matcher(lower)
            if (fibreMatcher.find()) {
                val valueStr = fibreMatcher.group(1) ?: ""
                val fibreVal = valueStr.toFloatOrNull() ?: 0f
                val isHighFibre = fibreVal >= 3.0f
                metrics.add(
                    NutritionMetric(
                        label = if (isHighFibre) "🌾 High-Fibre: ${valueStr}g" else "🌾 Fibre: ${valueStr}g",
                        value = "${valueStr}g",
                        isHighlight = isHighFibre,
                        badgeType = BadgeType.HIGH_FIBRE
                    )
                )
            }

            // 2. Calories extraction (e.g., "195 kcal", "68.7 kcal", "195 calories")
            val calMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:k?cal|calories)").matcher(lower)
            if (calMatcher.find()) {
                val valueStr = calMatcher.group(1) ?: ""
                metrics.add(
                    NutritionMetric(
                        label = "🔥 ${valueStr} kcal",
                        value = "${valueStr} kcal",
                        isHighlight = false,
                        badgeType = BadgeType.CALORIES
                    )
                )
            }

            // 3. Carbohydrates extraction (e.g., "30.9g carbohydrates", "8.55g carbs")
            val carbMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*g?\\s*(?:of\\s*)?carb").matcher(lower)
            if (carbMatcher.find()) {
                val valueStr = carbMatcher.group(1) ?: ""
                metrics.add(
                    NutritionMetric(
                        label = "🍞 Carbs: ${valueStr}g",
                        value = "${valueStr}g",
                        isHighlight = false,
                        badgeType = BadgeType.CARBOHYDRATES
                    )
                )
            }

            // 4. Protein extraction (e.g., "5.4g protein")
            val proteinMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*g?\\s*(?:of\\s*)?protein").matcher(lower)
            if (proteinMatcher.find()) {
                val valueStr = proteinMatcher.group(1) ?: ""
                metrics.add(
                    NutritionMetric(
                        label = "🥩 Protein: ${valueStr}g",
                        value = "${valueStr}g",
                        isHighlight = false,
                        badgeType = BadgeType.PROTEIN
                    )
                )
            }

            // 5. MET Exercise Value (e.g., "3.5 METs", "MET: 4.0")
            val metMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:mets?|met\\b)").matcher(lower)
            if (metMatcher.find()) {
                val valueStr = metMatcher.group(1) ?: ""
                metrics.add(
                    NutritionMetric(
                        label = "⚡ ${valueStr} METs",
                        value = "${valueStr} METs",
                        isHighlight = true,
                        badgeType = BadgeType.MET_ACTIVITY
                    )
                )
            }

            return metrics
        }
    }
}

