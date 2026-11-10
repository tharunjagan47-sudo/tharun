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
                    val indianLocale = Locale("en", "IN")
                    val langResult = tts.setLanguage(indianLocale)
                    if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                        langResult == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        val fallbackResult = tts.setLanguage(Locale.ENGLISH)
                        _activeLocaleLabel.value = if (fallbackResult >= 0) "en-US" else "default"
                    } else {
                        _activeLocaleLabel.value = "en-IN"
                        // Select a crisp en-IN offline/network voice if present
                        try {
                            val preferredVoice = tts.voices
                                ?.filter { it.locale.language == "en" && it.locale.country == "IN" }
                                ?.minByOrNull { if (it.isNetworkConnectionRequired) 1 else 0 }
                            if (preferredVoice != null) {
                                tts.voice = preferredVoice
                            }
                        } catch (_: Exception) {}
                    }

                    tts.setSpeechRate(1.0f)
                    tts.setPitch(1.02f)

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

