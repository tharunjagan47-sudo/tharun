package com.simats.diawell123.ui.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

enum class JarvisVoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class DivaMouthShape {
    SMILE,
    HALF_OPEN,
    WIDE_OPEN,
    ROUND_O
}

/**
 * Manages two-way Speech-to-Text (SpeechRecognizer) and Text-to-Speech (TextToSpeech)
 * with Indian-English (en-IN) priority, Tanglish phonetic voice normalization,
 * and real-time syllable-synced mouth viseme animation for DIVA.
 */
class JarvisVoiceManager(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var selectedMaleVoice: Voice? = null
    private var pendingSpeechText: String? = null
    private var pendingOnDone: (() -> Unit)? = null

    private val utteranceTexts = HashMap<String, String>()
    private val activeLipSyncRunnables = mutableListOf<Runnable>()
    private var lipSyncGeneration = 0
    private var receivedRangeStart = false

    private val _voiceState = MutableStateFlow(JarvisVoiceState.IDLE)
    val voiceState: StateFlow<JarvisVoiceState> = _voiceState.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _mouthOpenness = MutableStateFlow(0f)
    val mouthOpenness: StateFlow<Float> = _mouthOpenness.asStateFlow()

    private val _mouthShape = MutableStateFlow(DivaMouthShape.SMILE)
    val mouthShape: StateFlow<DivaMouthShape> = _mouthShape.asStateFlow()

    private val _voiceError = MutableStateFlow<String?>(null)
    val voiceError: StateFlow<String?> = _voiceError.asStateFlow()

    private val _ttsReady = MutableStateFlow(false)
    val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    private val _activeLocaleLabel = MutableStateFlow("en-IN")
    val activeLocaleLabel: StateFlow<String> = _activeLocaleLabel.asStateFlow()

    init {
        initTextToSpeech()
    }

    private fun clearLipSyncRunnables() {
        activeLipSyncRunnables.forEach { mainHandler.removeCallbacks(it) }
        activeLipSyncRunnables.clear()
    }

    private fun postLipSync(delayMs: Long, action: () -> Unit) {
        lateinit var runnable: Runnable
        runnable = Runnable {
            activeLipSyncRunnables.remove(runnable)
            action()
        }
        activeLipSyncRunnables.add(runnable)
        mainHandler.postDelayed(runnable, delayMs)
    }

    private fun resetLipSync() {
        lipSyncGeneration++
        receivedRangeStart = false
        clearLipSyncRunnables()
        _mouthOpenness.value = 0f
        _mouthShape.value = DivaMouthShape.SMILE
    }

    private fun classifyViseme(segment: String): DivaMouthShape {
        val lower = segment.lowercase(Locale.ROOT)
        return when {
            lower.any { it == 'o' || it == 'u' || it == 'w' } -> DivaMouthShape.ROUND_O
            lower.any { it == 'a' || it == 'e' || it == 'i' || it == 'y' } -> DivaMouthShape.WIDE_OPEN
            else -> DivaMouthShape.HALF_OPEN
        }
    }

    private fun extractSyllableShapes(word: String): List<DivaMouthShape> {
        val cleaned = word.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }
        if (cleaned.isEmpty()) return listOf(DivaMouthShape.HALF_OPEN)

        val vowelRegex = Regex("[aeiouy]+")
        val matches = vowelRegex.findAll(cleaned).toList()
        if (matches.isEmpty()) {
            return listOf(classifyViseme(cleaned))
        }
        return matches.take(3).map { match ->
            classifyViseme(match.value)
        }
    }

    private fun triggerWordLipSync(word: String) {
        val gen = ++lipSyncGeneration
        clearLipSyncRunnables()

        val trimmed = word.trim()
        if (trimmed.isEmpty()) {
            _mouthOpenness.value = 0.08f
            _mouthShape.value = DivaMouthShape.SMILE
            return
        }

        val syllables = extractSyllableShapes(trimmed)
        val syllableDurationMs = when {
            trimmed.length <= 3 -> 115L
            trimmed.length <= 6 -> 130L
            else -> 120L
        }

        syllables.forEachIndexed { idx, shape ->
            val startDelay = idx * syllableDurationMs
            val peakOpen = when (shape) {
                DivaMouthShape.WIDE_OPEN -> 0.94f
                DivaMouthShape.ROUND_O -> 0.84f
                DivaMouthShape.HALF_OPEN -> 0.64f
                DivaMouthShape.SMILE -> 0.25f
            }
            val dipOpen = (peakOpen * 0.30f).coerceIn(0.16f, 0.34f)

            // Syllable peak opening
            postLipSync(startDelay) {
                if (gen == lipSyncGeneration && _voiceState.value == JarvisVoiceState.SPEAKING) {
                    _mouthShape.value = shape
                    _mouthOpenness.value = peakOpen
                }
            }

            // Mid-syllable consonant closure dip
            postLipSync(startDelay + (syllableDurationMs * 0.62f).toLong()) {
                if (gen == lipSyncGeneration && _voiceState.value == JarvisVoiceState.SPEAKING) {
                    _mouthOpenness.value = dipOpen
                }
            }
        }

        // Gentle mouth rest between words / punctuation pauses
        val endDelay = syllables.size * syllableDurationMs + 35L
        postLipSync(endDelay) {
            if (gen == lipSyncGeneration && _voiceState.value == JarvisVoiceState.SPEAKING) {
                _mouthOpenness.value = 0.08f
                _mouthShape.value = DivaMouthShape.SMILE
            }
        }
    }

    private fun startFallbackLipSyncLoop(gen: Int, step: Int = 0) {
        if (gen != lipSyncGeneration || receivedRangeStart || _voiceState.value != JarvisVoiceState.SPEAKING) {
            return
        }
        val pattern = listOf(
            DivaMouthShape.WIDE_OPEN to 0.90f,
            DivaMouthShape.HALF_OPEN to 0.30f,
            DivaMouthShape.ROUND_O to 0.82f,
            DivaMouthShape.HALF_OPEN to 0.24f,
            DivaMouthShape.WIDE_OPEN to 0.78f,
            DivaMouthShape.SMILE to 0.10f
        )
        val (shape, open) = pattern[step % pattern.size]
        _mouthShape.value = shape
        _mouthOpenness.value = open

        val nextDelay = if (step % 6 == 5) 145L else 95L
        postLipSync(nextDelay) {
            startFallbackLipSyncLoop(gen, step + 1)
        }
    }

    private fun initTextToSpeech() {
        try {
            textToSpeech = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val tts = textToSpeech ?: return@TextToSpeech
                    applyJarvisMaleVoice(tts)

                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            mainHandler.post {
                                if (_voiceState.value != JarvisVoiceState.LISTENING) {
                                    _voiceState.value = JarvisVoiceState.SPEAKING
                                }
                                receivedRangeStart = false
                                val gen = ++lipSyncGeneration
                                clearLipSyncRunnables()
                                // Subtle opening breath as DIVA begins speaking
                                _mouthShape.value = DivaMouthShape.HALF_OPEN
                                _mouthOpenness.value = 0.38f
                                // If TTS engine doesn't emit onRangeStart within 220ms, run rhythmic cadence
                                postLipSync(220L) {
                                    if (!receivedRangeStart && gen == lipSyncGeneration &&
                                        _voiceState.value == JarvisVoiceState.SPEAKING
                                    ) {
                                        startFallbackLipSyncLoop(gen)
                                    }
                                }
                            }
                        }

                        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                            mainHandler.post {
                                if (_voiceState.value != JarvisVoiceState.SPEAKING) return@post
                                receivedRangeStart = true
                                val fullText = utteranceId?.let { utteranceTexts[it] }.orEmpty()
                                val word = if (fullText.isNotEmpty() && start >= 0 && start < fullText.length) {
                                    val safeEnd = end.coerceIn(start + 1, fullText.length)
                                    fullText.substring(start, safeEnd)
                                } else {
                                    "ah"
                                }
                                triggerWordLipSync(word)
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            mainHandler.post {
                                utteranceId?.let { utteranceTexts.remove(it) }
                                val isIntermediateChunk = utteranceId?.startsWith("jarvis_part_") == true
                                if (!isIntermediateChunk) {
                                    resetLipSync()
                                    if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                                        _voiceState.value = JarvisVoiceState.IDLE
                                    }
                                    pendingOnDone?.invoke()
                                    pendingOnDone = null
                                }
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            mainHandler.post {
                                utteranceId?.let { utteranceTexts.remove(it) }
                                resetLipSync()
                                if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                                    _voiceState.value = JarvisVoiceState.IDLE
                                }
                                pendingOnDone = null
                            }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            mainHandler.post {
                                utteranceId?.let { utteranceTexts.remove(it) }
                                resetLipSync()
                                if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                                    _voiceState.value = JarvisVoiceState.IDLE
                                }
                                pendingOnDone = null
                            }
                        }
                    })

                    isTtsInitialized = true
                    _ttsReady.value = true

                    pendingSpeechText?.let { queued ->
                        val cb = pendingOnDone
                        pendingSpeechText = null
                        pendingOnDone = null
                        speak(queued, cb)
                    }
                } else {
                    Log.w(TAG, "TextToSpeech init failed with status=$status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TextToSpeech setup error: ${e.message}", e)
        }
    }

    /**
     * Selects a deep J.A.R.V.I.S.-style male English voice (British J.A.R.V.I.S. / Indian Male / US Male),
     * strictly avoiding female voices and adjusting pitch/cadence to a calm baritone.
     */
    private fun applyJarvisMaleVoice(tts: TextToSpeech) {
        try {
            val bestMaleVoice = selectBestJarvisMaleVoice(tts.voices)
            if (bestMaleVoice != null) {
                selectedMaleVoice = bestMaleVoice
                tts.language = bestMaleVoice.locale
                tts.voice = bestMaleVoice
                tts.setSpeechRate(0.95f)
                tts.setPitch(0.88f)
                _activeLocaleLabel.value = "DIVA Voice • ${bestMaleVoice.locale.toLanguageTag()}"
                Log.d(TAG, "Selected DIVA voice: ${bestMaleVoice.name} (${bestMaleVoice.locale})")
                return
            }

            // No named male pack detected on device — fall back to deeper baritone tuning
            val indianLocale = Locale.Builder().setLanguage("en").setRegion("IN").build()
            val ukResult = tts.setLanguage(Locale.UK)
            if (ukResult >= 0) {
                _activeLocaleLabel.value = "DIVA Voice • en-GB"
            } else {
                val inResult = tts.setLanguage(indianLocale)
                _activeLocaleLabel.value = if (inResult >= 0) {
                    "DIVA Voice • en-IN"
                } else {
                    tts.setLanguage(Locale.ENGLISH)
                    "DIVA Voice • en-US"
                }
            }
            tts.setSpeechRate(0.94f)
            tts.setPitch(0.76f)
        } catch (e: Exception) {
            Log.w(TAG, "Jarvis male voice selection fallback: ${e.message}")
            tts.setSpeechRate(0.94f)
            tts.setPitch(0.78f)
        }
    }

    private fun selectBestJarvisMaleVoice(voices: Set<Voice>?): Voice? {
        if (voices.isNullOrEmpty()) return null

        val femaleTokens = listOf(
            "female", "woman", "samantha", "karen", "moira", "veena", "lekha", "neerja",
            "en-in-x-ena", "en-in-x-enc", "en-gb-x-gba", "en-gb-x-gbc", "en-gb-x-fis",
            "en-us-x-tpf", "en-us-x-tpc", "en-us-x-sfg", "en-us-x-iog", "en-us-x-sfh",
            "en-au-x-aua", "en-au-x-auc"
        )
        val jarvisBritishMaleIds = listOf("en-gb-x-rjs", "en-gb-x-gbd", "en-gb-x-gbb")
        val indianMaleIds = listOf("en-in-x-end", "en-in-x-ene")
        val otherEnglishMaleIds = listOf("en-us-x-iom", "en-us-x-iol", "en-us-x-tpd", "en-au-x-aub", "en-au-x-aud")
        val genericMaleTokens = listOf(
            "male", "david", "george", "daniel", "oliver", "arthur", "james",
            "ryan", "guy", "mark", "rishi", "prabhat", "neeraj"
        )

        return voices
            .asSequence()
            .filter { voice ->
                val isNotInstalled = voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) == true
                voice.locale.language.equals("en", ignoreCase = true) && !isNotInstalled
            }
            .mapNotNull { voice ->
                val nameLower = voice.name.lowercase(Locale.ROOT)
                val featureStr = voice.features?.joinToString(" ")?.lowercase(Locale.ROOT).orEmpty()
                val combined = "$nameLower $featureStr"

                if (femaleTokens.any { token -> combined.contains(token) }) {
                    return@mapNotNull null
                }

                val genderBaseScore = when {
                    jarvisBritishMaleIds.any { nameLower.contains(it) } -> 1200
                    indianMaleIds.any { nameLower.contains(it) } -> 1150
                    otherEnglishMaleIds.any { nameLower.contains(it) } -> 1000
                    genericMaleTokens.any { token ->
                        Regex("(?:^|[^a-z])${Regex.escape(token)}(?:[^a-z]|$)").containsMatchIn(combined)
                    } -> when {
                        voice.locale.country.equals("GB", ignoreCase = true) -> 980
                        voice.locale.country.equals("IN", ignoreCase = true) -> 950
                        else -> 900
                    }
                    else -> return@mapNotNull null
                }

                val offlineBonus = if (!voice.isNetworkConnectionRequired) 500 else 0
                val totalScore = genderBaseScore + offlineBonus + voice.quality
                voice to totalScore
            }
            .maxByOrNull { it.second }
            ?.first
    }

    fun isLiveRecognitionAvailable(): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(appContext)
        } catch (_: Exception) {
            false
        }
    }

    fun startListening(
        onFinalResult: (String) -> Unit,
        onFallbackIntentNeeded: () -> Unit
    ) {
        mainHandler.post {
            stopSpeaking()
            _voiceError.value = null
            _partialTranscript.value = ""
            _rmsLevel.value = 0f

            if (!isLiveRecognitionAvailable()) {
                _voiceState.value = JarvisVoiceState.IDLE
                onFallbackIntentNeeded()
                return@post
            }

            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _voiceState.value = JarvisVoiceState.LISTENING
                            _voiceError.value = null
                        }

                        override fun onBeginningOfSpeech() {
                            _voiceState.value = JarvisVoiceState.LISTENING
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // Typical SpeechRecognizer RMS ranges roughly -2 dB to 10+ dB
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                            _rmsLevel.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _rmsLevel.value = 0f
                            if (_voiceState.value == JarvisVoiceState.LISTENING) {
                                _voiceState.value = JarvisVoiceState.THINKING
                            }
                        }

                        override fun onError(error: Int) {
                            _rmsLevel.value = 0f
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Tap the orb and speak again."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap the orb to try again."
                                SpeechRecognizer.ERROR_AUDIO -> "Microphone audio error. Please check mic access."
                                SpeechRecognizer.ERROR_NETWORK,
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service offline. Switching to fallback dialog..."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed."
                                else -> "Voice recognition interrupted. Please try again."
                            }
                            if (error == SpeechRecognizer.ERROR_CLIENT) {
                                _voiceState.value = JarvisVoiceState.IDLE
                                return
                            }
                            if (error == SpeechRecognizer.ERROR_NETWORK ||
                                error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                            ) {
                                _voiceState.value = JarvisVoiceState.IDLE
                                onFallbackIntentNeeded()
                                return
                            }
                            _voiceError.value = msg
                            _voiceState.value = JarvisVoiceState.ERROR
                        }

                        override fun onResults(results: Bundle?) {
                            _rmsLevel.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val spoken = matches?.firstOrNull()?.trim().orEmpty()
                            if (spoken.isNotBlank()) {
                                _partialTranscript.value = spoken
                                _voiceState.value = JarvisVoiceState.THINKING
                                onFinalResult(spoken)
                            } else {
                                _voiceError.value = "Didn't catch that. Tap the orb to speak."
                                _voiceState.value = JarvisVoiceState.ERROR
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val partial = partialResults
                                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                ?.firstOrNull()
                                ?.trim()
                            if (!partial.isNullOrBlank()) {
                                _partialTranscript.value = partial
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1400L)
                }

                _voiceState.value = JarvisVoiceState.LISTENING
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start SpeechRecognizer: ${e.message}", e)
                _voiceState.value = JarvisVoiceState.IDLE
                onFallbackIntentNeeded()
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
        }
    }

    fun cancelListening() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {}
            _rmsLevel.value = 0f
            _voiceError.value = null
            if (_voiceState.value == JarvisVoiceState.LISTENING) {
                _voiceState.value = JarvisVoiceState.IDLE
            }
        }
    }

    fun setThinkingState() {
        mainHandler.post {
            _voiceError.value = null
            _voiceState.value = JarvisVoiceState.THINKING
        }
    }

    fun setIdleState() {
        mainHandler.post {
            _rmsLevel.value = 0f
            resetLipSync()
            _voiceState.value = JarvisVoiceState.IDLE
        }
    }

    fun speak(rawText: String, onDone: (() -> Unit)? = null) {
        val cleaned = cleanTextForIndianSpeech(rawText)
        if (cleaned.isBlank()) {
            onDone?.invoke()
            return
        }

        mainHandler.post {
            if (!isTtsInitialized || textToSpeech == null) {
                pendingSpeechText = cleaned
                pendingOnDone = onDone
                return@post
            }

            try {
                val tts = textToSpeech ?: return@post
                if (selectedMaleVoice == null) {
                    applyJarvisMaleVoice(tts)
                } else {
                    tts.voice = selectedMaleVoice
                    tts.setSpeechRate(0.95f)
                    tts.setPitch(0.88f)
                }
                pendingOnDone = onDone
                utteranceTexts.clear()
                resetLipSync()
                _voiceState.value = JarvisVoiceState.SPEAKING

                val maxLen = TextToSpeech.getMaxSpeechInputLength().coerceAtLeast(1000)
                if (cleaned.length <= maxLen) {
                    val utteranceId = "jarvis_${UUID.randomUUID()}"
                    utteranceTexts[utteranceId] = cleaned
                    tts.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    val chunks = cleaned.chunked(maxLen)
                    chunks.forEachIndexed { idx, chunk ->
                        val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                        val uid = if (idx == chunks.lastIndex) "jarvis_last_${UUID.randomUUID()}" else "jarvis_part_${idx}_${UUID.randomUUID()}"
                        utteranceTexts[uid] = chunk
                        tts.speak(chunk, mode, null, uid)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TTS speak failed: ${e.message}", e)
                resetLipSync()
                _voiceState.value = JarvisVoiceState.IDLE
                pendingOnDone = null
            }
        }
    }

    fun stopSpeaking() {
        mainHandler.post {
            try {
                pendingSpeechText = null
                pendingOnDone = null
                utteranceTexts.clear()
                resetLipSync()
                textToSpeech?.stop()
                if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                    _voiceState.value = JarvisVoiceState.IDLE
                }
            } catch (_: Exception) {}
        }
    }

    fun release() {
        mainHandler.post {
            utteranceTexts.clear()
            resetLipSync()
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
            try {
                textToSpeech?.stop()
                textToSpeech?.shutdown()
                textToSpeech = null
            } catch (_: Exception) {}
        }
    }

    companion object {
        private const val TAG = "JarvisVoiceManager"

        /**
         * Strips Markdown/emojis, expands medical units, and applies Tanglish phonetic spelling
         * so Indian-English TTS pronounces responses naturally.
         */
        fun cleanTextForIndianSpeech(rawText: String): String {
            var text = rawText
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
                .replace(Regex("\\*(.*?)\\*"), "$1")
                .replace(Regex("^\\s*[•\\-*]+\\s*", RegexOption.MULTILINE), "")
                .replace(Regex("#+\\s*"), "")
                // Remove non-ASCII symbol/emoji blocks while retaining ASCII punctuation & letters
                .replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), " ")
                .replace(Regex("[\\p{So}\\p{Cn}\\u2600-\\u27BF\\u2300-\\u23FF]"), " ")

            // Expand clinical abbreviations
            text = text
                .replace(Regex("\\bmg/dL\\b", RegexOption.IGNORE_CASE), "milligrams per deciliter")
                .replace(Regex("\\beHbA1c\\b", RegexOption.IGNORE_CASE), "estimated H B A 1 C")
                .replace(Regex("\\bHbA1c\\b", RegexOption.IGNORE_CASE), "H B A 1 C")
                .replace(Regex("\\bMETs\\b", RegexOption.IGNORE_CASE), "metabolic units")
                .replace(Regex("\\bMET\\b"), "metabolic unit")
                .replace(Regex("\\bkcal\\b", RegexOption.IGNORE_CASE), "kilocalories")
                .replace(Regex("(\\d+(?:\\.\\d+)?)\\s*g\\b"), "$1 grams")
                .replace(Regex("(\\d+(?:\\.\\d+)?)\\s*mg\\b", RegexOption.IGNORE_CASE), "$1 milligrams")
                .replace(Regex("\\bDr\\.\\s*"), "Doctor ")
                .replace("< 70", "below 70")
                .replace("<70", "below 70")
                .replace("> 180", "above 180")
                .replace(">180", "above 180")
                .replace("> 250", "above 250")
                .replace(">250", "above 250")

            // Tanglish phonetic dictionary for clearer en-IN voice synthesis
            val tanglishPhonetics = listOf(
                Regex("\\bvanakkam\\b", RegexOption.IGNORE_CASE) to "va-nak-kam",
                Regex("\\bnandri\\b", RegexOption.IGNORE_CASE) to "nan-dree",
                Regex("\\bromba\\b", RegexOption.IGNORE_CASE) to "rom-ba",
                Regex("\\bnalla\\b", RegexOption.IGNORE_CASE) to "nal-la",
                Regex("\\bsarkkarai\\b|\\bsarkarai\\b", RegexOption.IGNORE_CASE) to "sar-ka-rai",
                Regex("\\bsaapadu\\b|\\bsapadu\\b", RegexOption.IGNORE_CASE) to "saa-paa-du",
                Regex("\\bkaalaila\\b|\\bkalaila\\b", RegexOption.IGNORE_CASE) to "kaa-lai-la",
                Regex("\\bmathiyam\\b|\\bmadhiyam\\b", RegexOption.IGNORE_CASE) to "ma-dhee-yam",
                Regex("\\brathiri\\b|\\biravu\\b", RegexOption.IGNORE_CASE) to "raa-thi-ri",
                Regex("\\bragi\\b", RegexOption.IGNORE_CASE) to "raa-gi",
                Regex("\\bkambu\\b", RegexOption.IGNORE_CASE) to "kam-bu",
                Regex("\\bthinai\\b", RegexOption.IGNORE_CASE) to "thi-nai",
                Regex("\\bidli\\b", RegexOption.IGNORE_CASE) to "id-lee",
                Regex("\\bdosai\\b|\\bdosa\\b", RegexOption.IGNORE_CASE) to "do-sai",
                Regex("\\bkeerai\\b", RegexOption.IGNORE_CASE) to "kee-rai",
                Regex("\\bvendakkai\\b", RegexOption.IGNORE_CASE) to "ven-dak-kai",
                Regex("\\bpavakkai\\b|\\bpagarkai\\b", RegexOption.IGNORE_CASE) to "paa-vak-kai",
                Regex("\\bsundal\\b", RegexOption.IGNORE_CASE) to "sun-dal",
                Regex("\\bkuzhambu\\b", RegexOption.IGNORE_CASE) to "ku-zham-bu",
                Regex("\\bmayakkam\\b", RegexOption.IGNORE_CASE) to "ma-yak-kam",
                Regex("\\bnadai\\b", RegexOption.IGNORE_CASE) to "na-dai"
            )
            tanglishPhonetics.forEach { (pattern, replacement) ->
                text = text.replace(pattern, replacement)
            }

            return text
                .replace(Regex("\\s*\n+\\s*"), ". ")
                .replace(Regex("\\s{2,}"), " ")
                .trim()
        }
    }
}

