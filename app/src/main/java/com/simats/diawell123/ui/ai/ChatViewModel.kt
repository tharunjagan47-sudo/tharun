package com.simats.diawell123.ui.ai

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.AlertType
import com.simats.diawell123.data.model.BadgeType
import com.simats.diawell123.data.model.ChatMessage
import com.simats.diawell123.data.model.ChatSender
import com.simats.diawell123.data.model.NutritionMetric
import com.simats.diawell123.data.repository.ChatRepository
import com.simats.diawell123.data.repository.ChatRepositoryImpl
import com.simats.diawell123.data.repository.GlucoseRepository
import com.simats.diawell123.data.repository.GlucoseRepositoryImpl
import com.simats.diawell123.data.repository.MedsRepository
import com.simats.diawell123.data.repository.MedsRepositoryImpl
import com.simats.diawell123.ui.profile.ProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * ViewModel managing state for Dr. Jarvis AI Chatbot and Siri-style Voice Mode.
 * Supports two-way voice responses, hands-free glucose logging / medication alarm actions,
 * and clinical alert/macro extraction.
 */
class ChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl(),
    private val glucoseRepository: GlucoseRepository = GlucoseRepositoryImpl(),
    private val medsRepository: MedsRepository = MedsRepositoryImpl()
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
            text = "Hello $userName! I am Dr. Jarvis, your DiaWell voice & diabetes wellness assistant. ⚡🩺\n\nTap the **🎙️ Mic** for Siri-style Voice Mode: ask diet questions in English or Tanglish, or say **\"Log my fasting sugar 115\"** or **\"Remind me to take Metformin at 8 PM\"** hands-free!",
            alertType = AlertType.NONE,
            metrics = emptyList()
        )
        _messages.value = listOf(greeting)
    }

    /**
     * Send user query or execute hands-free voice action commands, with optional spoken callback.
     */
    fun sendMessage(
        query: String,
        context: Context? = null,
        onSpokenResponseReady: ((String) -> Unit)? = null,
        onErrorCallback: (() -> Unit)? = null
    ) {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        lastUserMessage = trimmed
        _errorMessage.value = null

        val userMsg = ChatMessage(
            sender = ChatSender.USER,
            text = trimmed
        )
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Check Smart Hands-Free Voice Action Commands (Blood Sugar Logging & Med Alarms)
                if (context != null) {
                    val actionResult = JarvisVoiceCommandHandler.tryExecuteCommand(
                        context = context.applicationContext,
                        utterance = trimmed,
                        glucoseRepository = glucoseRepository,
                        medsRepository = medsRepository
                    )
                    if (actionResult is VoiceActionResult.Executed) {
                        val aiActionMsg = ChatMessage(
                            sender = ChatSender.AI,
                            text = actionResult.displayReply,
                            alertType = actionResult.alertType,
                            metrics = emptyList()
                        )
                        _messages.value = _messages.value + aiActionMsg
                        onSpokenResponseReady?.invoke(actionResult.spokenReply)
                        return@launch
                    }
                }

                // 2. Otherwise query Dr. Jarvis AI conversational backend
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
                            onSpokenResponseReady?.invoke(responseText)
                        } else {
                            val errorMsg = "Dr. Jarvis returned an empty response. Please try rephrasing your question."
                            _errorMessage.value = errorMsg
                            _messages.value = _messages.value + ChatMessage(
                                sender = ChatSender.AI,
                                text = "⚠️ $errorMsg",
                                isError = true
                            )
                            onErrorCallback?.invoke()
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
                        onSpokenResponseReady?.invoke(errorMsg)
                        onErrorCallback?.invoke()
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
                onErrorCallback?.invoke()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Retry the most recent failed message.
     */
    fun retryLastMessage(
        context: Context? = null,
        onSpokenResponseReady: ((String) -> Unit)? = null
    ) {
        lastUserMessage?.let { query ->
            _errorMessage.value = null
            val currentList = _messages.value
            if (currentList.isNotEmpty() && currentList.last().isError) {
                _messages.value = currentList.dropLast(1)
            }
            sendMessage(query, context = context, onSpokenResponseReady = onSpokenResponseReady)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetChat() {
        _errorMessage.value = null
        initWelcomeMessage()
    }

    companion object {
        fun detectAlertType(text: String): AlertType {
            val lower = text.lowercase()

            return when {
                lower.contains("< 70") || lower.contains("<70") ||
                lower.contains("hypoglycemia") || lower.contains("low sugar") ||
                lower.contains("fast-acting carb") -> AlertType.HYPOGLYCEMIA_WARNING

                lower.contains("ketone") || lower.contains("ketoacidosis") || lower.contains("dka") ->
                    AlertType.KETONE_WARNING

                lower.contains("> 250") || lower.contains(">250") ||
                lower.contains(">= 300") || lower.contains(">=300") ||
                lower.contains("hyperglycemia") || lower.contains("severe hyperglycemia") ->
                    AlertType.HYPERGLYCEMIA_WARNING

                lower.contains("contraindicated") || lower.contains("seek emergency") ||
                lower.contains("consult your doctor immediately") -> AlertType.GENERAL_CLINICAL_ALERT

                else -> AlertType.NONE
            }
        }

        fun extractNutritionMetrics(text: String): List<NutritionMetric> {
            val metrics = mutableListOf<NutritionMetric>()
            val lower = text.lowercase()

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
