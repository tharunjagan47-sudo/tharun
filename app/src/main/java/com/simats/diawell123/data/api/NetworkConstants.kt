package com.simats.diawell123.data.api

/**
 * Centralized Network Configuration for DiaWell Backend Communication.
 *
 * Configurable endpoints:
 * - PHP Backend (Apache Port 80):
 *     - Emulator: "http://10.0.2.2/"
 *     - Physical Device (ADB Reverse): "http://127.0.0.1/"
 * - Ollama Engine: Port 11434 (via PHP middleware)
 */
object NetworkConstants {

    // -------------------------------------------------------------------------
    // PHP Backend Settings (Apache Port 80 / ADB Reverse Port 8000 -> 80)
    // -------------------------------------------------------------------------
    const val LOCAL_PHP_BASE_URL = "http://127.0.0.1/diawell_backend/"
    const val ADB_REVERSE_PHP_BASE_URL = "http://127.0.0.1:8000/diawell_backend/"
    const val EMULATOR_PHP_BASE_URL = "http://10.0.2.2/diawell_backend/"
    const val PHYSICAL_DEVICE_PHP_LAN_URL = "http://192.168.1.3/diawell_backend/" // Current Host PC LAN IPv4 from ipconfig
    const val DEVTUNNEL_BASE_URL = "https://surcharge-blot-expenses.ngrok-free.dev/diawell_backend/"

    // -------------------------------------------------------------------------
    // Legacy / Microservice Settings (Kept for compatibility)
    // -------------------------------------------------------------------------
    const val EMULATOR_AI_BASE_URL = "http://10.0.2.2/diawell_backend/"
    const val PHYSICAL_DEVICE_AI_LAN_URL = "http://192.168.1.3/diawell_backend/"

    /**
     * Standardized relative chat endpoint path appended to BASE_URL (http://127.0.0.1/diawell_backend/).
     */
    const val CHAT_ENDPOINT = "ai/chat.php"
    const val PHP_BRIDGE_CHAT_ENDPOINT = "ai/chat.php"

    /**
     * Ordered fallback endpoints for Dr. Jarvis AI if primary connection fails.
     */
    val AI_CHAT_FALLBACK_URLS: List<String> = listOf(
        "http://192.168.1.3/diawell_backend/ai/chat.php",
        "http://127.0.0.1:8000/diawell_backend/ai/chat.php",
        "http://192.168.1.7/diawell_backend/ai/chat.php",
        "http://10.0.2.2/diawell_backend/ai/chat.php",
        "http://127.0.0.1/diawell_backend/ai/chat.php",
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
     * Automatic connection fallback in ApiClient/ChatRepository seamlessly probes ADB reverse (8000),
     * LAN Wi-Fi, and Emulator (10.0.2.2) endpoints if 127.0.0.1:80 is unavailable.
     */
    val BASE_URL: String
        get() = if (isEmulator) EMULATOR_PHP_BASE_URL else PHYSICAL_DEVICE_PHP_LAN_URL

    /**
     * Active Base URL used by Retrofit for Dr. Jarvis AI Chatbot.
     */
    val AI_BASE_URL: String
        get() = BASE_URL
}
