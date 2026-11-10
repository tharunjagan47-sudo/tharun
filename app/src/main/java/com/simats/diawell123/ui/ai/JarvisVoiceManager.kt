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

/**
 * Manages two-way Speech-to-Text (SpeechRecognizer) and Text-to-Speech (TextToSpeech)
 * with Indian-English (en-IN) priority and Tanglish phonetic voice normalization.
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

    private val _voiceState = MutableStateFlow(JarvisVoiceState.IDLE)
    val voiceState: StateFlow<JarvisVoiceState> = _voiceState.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _voiceError = MutableStateFlow<String?>(null)
    val voiceError: StateFlow<String?> = _voiceError.asStateFlow()

    private val _ttsReady = MutableStateFlow(false)
    val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    private val _activeLocaleLabel = MutableStateFlow("en-IN")
    val activeLocaleLabel: StateFlow<String> = _activeLocaleLabel.asStateFlow()

    init {
        initTextToSpeech()
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
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            mainHandler.post {
                                if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                                    _voiceState.value = JarvisVoiceState.IDLE
                                }
                                pendingOnDone?.invoke()
                                pendingOnDone = null
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            mainHandler.post {
                                if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                                    _voiceState.value = JarvisVoiceState.IDLE
                                }
                                pendingOnDone = null
                            }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            mainHandler.post {
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
                _activeLocaleLabel.value = "Jarvis Male • ${bestMaleVoice.locale.toLanguageTag()}"
                Log.d(TAG, "Selected Jarvis male voice: ${bestMaleVoice.name} (${bestMaleVoice.locale})")
                return
            }

            // No named male pack detected on device — fall back to deeper baritone tuning
            val indianLocale = Locale.Builder().setLanguage("en").setRegion("IN").build()
            val ukResult = tts.setLanguage(Locale.UK)
            if (ukResult >= 0) {
                _activeLocaleLabel.value = "Jarvis Male • en-GB"
            } else {
                val inResult = tts.setLanguage(indianLocale)
                _activeLocaleLabel.value = if (inResult >= 0) {
                    "Jarvis Male • en-IN"
                } else {
                    tts.setLanguage(Locale.ENGLISH)
                    "Jarvis Male • en-US"
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
                _voiceState.value = JarvisVoiceState.SPEAKING

                val maxLen = TextToSpeech.getMaxSpeechInputLength().coerceAtLeast(1000)
                if (cleaned.length <= maxLen) {
                    val utteranceId = "jarvis_${UUID.randomUUID()}"
                    tts.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    val chunks = cleaned.chunked(maxLen)
                    chunks.forEachIndexed { idx, chunk ->
                        val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                        val uid = if (idx == chunks.lastIndex) "jarvis_last_${UUID.randomUUID()}" else "jarvis_part_$idx"
                        tts.speak(chunk, mode, null, uid)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TTS speak failed: ${e.message}", e)
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
                textToSpeech?.stop()
                if (_voiceState.value == JarvisVoiceState.SPEAKING) {
                    _voiceState.value = JarvisVoiceState.IDLE
                }
            } catch (_: Exception) {}
        }
    }

    fun release() {
        mainHandler.post {
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
                .replace(Regex("[✓✔✕⚠️🩺⚡💊🩸🥗🚶🍚🔥💧🎙️🔊🔇🚨⏰]"), " ")

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

