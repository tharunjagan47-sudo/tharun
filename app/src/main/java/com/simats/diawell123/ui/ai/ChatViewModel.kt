package com.simats.diawell123.ui.ai

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.AlertType
import com.simats.diawell123.data.model.BadgeType
import com.simats.diawell123.data.model.ChatHistoryMessage
import com.simats.diawell123.data.model.ChatMessage
import com.simats.diawell123.data.model.ChatSender
import com.simats.diawell123.data.model.NutritionMetric
import com.simats.diawell123.data.repository.ActivityRepository
import com.simats.diawell123.data.repository.ActivityRepositoryImpl
import com.simats.diawell123.data.repository.ChatRepository
import com.simats.diawell123.data.repository.ChatRepositoryImpl
import com.simats.diawell123.data.repository.DietRepository
import com.simats.diawell123.data.repository.DietRepositoryImpl
import com.simats.diawell123.data.repository.GlucoseRepository
import com.simats.diawell123.data.repository.GlucoseRepositoryImpl
import com.simats.diawell123.data.repository.MedsRepository
import com.simats.diawell123.data.repository.MedsRepositoryImpl
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.ui.profile.ProfileState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * ViewModel managing state for Dr. Jarvis AI Chatbot and Siri-style Voice Mode.
 * Supports two-way voice responses, hands-free glucose logging / medication alarm actions,
 * and live Patient Context Awareness (Personal Health RAG).
 */
class ChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl(),
    private val glucoseRepository: GlucoseRepository = GlucoseRepositoryImpl(),
    private val medsRepository: MedsRepository = MedsRepositoryImpl(),
    private val dietRepository: DietRepository = DietRepositoryImpl(),
    private val activityRepository: ActivityRepository = ActivityRepositoryImpl()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastUserMessage: String? = null
    private var currentJob: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("ChatViewModel", "Uncaught exception in ChatViewModel: ${throwable.message}", throwable)
        _isLoading.value = false
        val cleanMsg = "Something went wrong while connecting. Please check your network and try again."
        _errorMessage.value = cleanMsg
        _messages.value = _messages.value + ChatMessage(
            sender = ChatSender.AI,
            text = cleanMsg,
            isError = true
        )
    }

    init {
        initWelcomeMessage()
    }

    private fun resolveActiveUserName(context: Context? = null): String {
        val sessionName = try {
            val sm = if (context != null) SessionManager.init(context) else SessionManager.getInstance()
            sm.getFullName()?.takeIf { it.isNotBlank() } ?: sm.getUserSession()?.fullName?.takeIf { it.isNotBlank() }
        } catch (_: Exception) { null }

        return sessionName
            ?: ProfileState.userName.takeIf { it.isNotBlank() && !it.equals("Mary Jane", ignoreCase = true) && !it.equals("User", ignoreCase = true) }
            ?: "there"
    }

    private fun buildWelcomeText(name: String): String {
        return "Hello $name! I am **DIVA** (DiaWell Intelligent Virtual Assistant), your personal voice & diabetes wellness companion. ⚡🩺\n\nTap the **🎙️ Mic** for Voice Mode: ask about your blood sugar, medications, or diet (e.g. **\"What is my glucose level?\"** or **\"What medications am I taking?\"**), or say **\"Log my fasting sugar 115\"** hands-free!"
    }

    private fun initWelcomeMessage() {
        val userName = resolveActiveUserName()
        val greeting = ChatMessage(
            sender = ChatSender.AI,
            text = buildWelcomeText(userName),
            alertType = AlertType.NONE,
            metrics = emptyList()
        )
        _messages.value = listOf(greeting)
    }

    fun refreshWelcomeMessage(context: Context? = null) {
        val activeName = resolveActiveUserName(context)
        val currentMsgs = _messages.value
        val welcomeText = buildWelcomeText(activeName)

        if (currentMsgs.isEmpty()) {
            _messages.value = listOf(
                ChatMessage(
                    sender = ChatSender.AI,
                    text = welcomeText,
                    alertType = AlertType.NONE,
                    metrics = emptyList()
                )
            )
        } else if (currentMsgs.first().sender == ChatSender.AI && currentMsgs.first().text.contains("DIVA")) {
            _messages.value = listOf(currentMsgs.first().copy(text = welcomeText)) + currentMsgs.drop(1)
        }
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

        currentJob?.cancel()
        currentJob = viewModelScope.launch(exceptionHandler) {
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

                // 2. Aggregate live Patient Context (Personal Health RAG)
                val patientContext = PatientContextProvider.getLivePatientContext(
                    context = context,
                    glucoseRepo = glucoseRepository,
                    medsRepo = medsRepository,
                    dietRepo = dietRepository,
                    activityRepo = activityRepository
                )

                // 3. Extract continuous multi-turn conversation history (last 6 turns)
                val historyList = _messages.value
                    .filter { !it.isError && it.text.isNotBlank() && it.text != trimmed }
                    .takeLast(6)
                    .map { msg ->
                        ChatHistoryMessage(
                            role = if (msg.sender == ChatSender.USER) "user" else "assistant",
                            content = msg.text
                        )
                    }

                // 4. Query DIVA (DiaWell Intelligent Virtual Assistant) backend with patient context & conversation memory
                val result = repository.sendMessage(trimmed, patientContext, historyList)

                result.fold(
                    onSuccess = { chatResponse ->
                        val rawResponseText = chatResponse.getCleanResponse()
                        val responseText = rawResponseText
                            .replace(Regex("\\bDr\\.?\\s*Jarvis\\b", RegexOption.IGNORE_CASE), "DIVA")
                            .replace(Regex("\\bJarvis\\b", RegexOption.IGNORE_CASE), "DIVA")
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
                            val errorMsg = "DIVA returned an empty response. Please try rephrasing your question."
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
                        val rawMsg = error.localizedMessage.orEmpty()
                        val cleanMsg = when {
                            rawMsg.contains("timed out", true) || rawMsg.contains("Timeout", true) ->
                                "DIVA took a little too long to respond. Please try asking again."
                            rawMsg.contains("Failed to connect", true) || rawMsg.contains("ConnectException", true) || rawMsg.contains("unreachable", true) ->
                                "Unable to connect right now. Please check your network and try again."
                            rawMsg.contains("HTTP 5", true) || rawMsg.contains("Ollama", true) ->
                                "DIVA is currently busy. Please try again in a moment."
                            rawMsg.isNotBlank() && !rawMsg.contains("Exception") && !rawMsg.contains("127.0.0.1") && rawMsg.length < 80 ->
                                rawMsg
                            else ->
                                "Something went wrong. Please check your connection and try again."
                        }
                        _errorMessage.value = cleanMsg

                        val errorAiMsg = ChatMessage(
                            sender = ChatSender.AI,
                            text = cleanMsg,
                            isError = true
                        )
                        _messages.value = _messages.value + errorAiMsg
                        onErrorCallback?.invoke()
                    }
                )
            } catch (t: Throwable) {
                Log.e("ChatViewModel", "Chat turn error", t)
                val cleanMsg = "Something went wrong. Please check your connection and try again."
                _errorMessage.value = cleanMsg

                val errorAiMsg = ChatMessage(
                    sender = ChatSender.AI,
                    text = cleanMsg,
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

    fun stopGenerating() {
        currentJob?.cancel()
        currentJob = null
        _isLoading.value = false
    }

    fun resetChat() {
        stopGenerating()
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
