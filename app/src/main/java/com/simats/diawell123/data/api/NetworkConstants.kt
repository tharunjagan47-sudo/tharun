package com.simats.diawell123.data.api

/**
 * Centralized Network Configuration for DiaWell Backend Communication.
 *
 * Configurable endpoints:
 * - PHP Backend (via ADB Reverse Port 8080 on physical device -> Port 80 on PC):
 *     - Physical Device: "http://127.0.0.1:8080/diawell_backend/"
 *     - Secondary Fallback: "http://127.0.0.1/diawell_backend/"
 *     - Emulator: "http://10.0.2.2/diawell_backend/"
 * - Ollama Engine: Port 11434 (via PHP middleware)
 */
object NetworkConstants {

    // -------------------------------------------------------------------------
    // PHP Backend Settings (Non-Privileged Port 8080 ADB Reverse / Apache)
    // -------------------------------------------------------------------------
    const val ADB_REVERSE_8080_PHP_BASE_URL = "http://127.0.0.1:8080/diawell_backend/"
    const val LOCAL_PHP_BASE_URL = "http://127.0.0.1/diawell_backend/"
    const val ADB_REVERSE_PHP_BASE_URL = "http://127.0.0.1:8000/diawell_backend/"
    const val EMULATOR_PHP_BASE_URL = "http://10.0.2.2/diawell_backend/"
    const val PHYSICAL_DEVICE_PHP_LAN_URL = "http://127.0.0.1:8080/diawell_backend/"
    const val NGROK_HOST = "popcorn-clothing-pungent.ngrok-free.dev"
    const val NGROK_BASE_URL = "https://$NGROK_HOST/diawell_backend/"
    const val DEVTUNNEL_BASE_URL = NGROK_BASE_URL

    // -------------------------------------------------------------------------
    // AI Settings (Kept for compatibility)
    // -------------------------------------------------------------------------
    const val EMULATOR_AI_BASE_URL = "http://10.0.2.2/diawell_backend/"
    const val PHYSICAL_DEVICE_AI_LAN_URL = "http://127.0.0.1:8080/diawell_backend/"

    /**
     * Standardized relative chat endpoint path appended to BASE_URL (http://127.0.0.1:8080/diawell_backend/).
     */
    const val CHAT_ENDPOINT = "ai/chat.php"
    const val PHP_BRIDGE_CHAT_ENDPOINT = "ai/chat.php"

    /**
     * Ordered fallback endpoints for Dr. Jarvis AI if primary connection fails.
     */
    val AI_CHAT_FALLBACK_URLS: List<String> = listOf(
        "http://127.0.0.1:8080/diawell_backend/ai/chat.php",
        "${NGROK_BASE_URL}ai/chat.php",
        "http://127.0.0.1/diawell_backend/ai/chat.php",
        "http://127.0.0.1:8000/diawell_backend/ai/chat.php",
        "http://10.0.2.2/diawell_backend/ai/chat.php",
        "http://127.0.0.1:8080/diawell_backend/diawell_backend/ai/chat.php",
        "http://127.0.0.1/diawell_backend/diawell_backend/ai/chat.php"
    )

    /**
     * Auto-detect if currently running on Android Studio Emulator or Physical Device.
     * Defaults to true in JVM unit test suites to guarantee deterministic test assertions.
     */
    val isEmulator: Boolean by lazy {
        try {
            val fingerprint = android.os.Build.FINGERPRINT ?: ""
            if (fingerprint.isEmpty() || fingerprint == "robolectric") {
                return@lazy true
            }
            fingerprint.startsWith("generic") ||
            fingerprint.startsWith("unknown") ||
            android.os.Build.MODEL.contains("google_sdk") ||
            android.os.Build.MODEL.contains("Emulator") ||
            android.os.Build.MODEL.contains("Android SDK built for x86") ||
            android.os.Build.MANUFACTURER.contains("Genymotion") ||
            android.os.Build.HARDWARE.contains("goldfish") ||
            android.os.Build.HARDWARE.contains("ranchu") ||
            android.os.Build.PRODUCT.contains("sdk_gphone") ||
            android.os.Build.PRODUCT.contains("google_sdk")
        } catch (_: Throwable) {
            true
        }
    }

    /**
     * Standardized primary Base URL used by Retrofit across all DiaWell modules & Dr. Jarvis AI.
     * Physical devices strictly use non-privileged port http://127.0.0.1:8080/diawell_backend/
     * to route reliably through ADB reverse (tcp:8080 -> tcp:80).
     */
    val BASE_URL: String
        get() = if (isEmulator) EMULATOR_PHP_BASE_URL else ADB_REVERSE_8080_PHP_BASE_URL

    /**
     * Active Base URL used by Retrofit for Dr. Jarvis AI Chatbot.
     */
    val AI_BASE_URL: String
        get() = BASE_URL
}
