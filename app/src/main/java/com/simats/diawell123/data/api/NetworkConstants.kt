package com.simats.diawell123.data.api

/**
 * Centralized Network Configuration for DiaWell Backend Communication.
 *
 * Configurable endpoints:
 * - PHP Backend (Apache Port 80):
 *     - Emulator: "http://10.0.2.2/"
 *     - Physical Device LAN: "http://172.23.176.99/"
 * - FastAPI AI Microservice (Port 8000):
 *     - Emulator: "http://10.0.2.2:8000/"
 *     - Physical Device LAN: "http://172.23.176.99:8000/"
 */
object NetworkConstants {

    // -------------------------------------------------------------------------
    // PHP Backend Settings (Port 80 - Apache/MySQL)
    // -------------------------------------------------------------------------
    const val EMULATOR_PHP_BASE_URL = "http://10.0.2.2/"
    const val PHYSICAL_DEVICE_PHP_LAN_URL = "http://172.23.176.99/" // Host PC LAN IPv4 from ipconfig
    const val DEVTUNNEL_BASE_URL = "https://bhft6cwn-80.inc1.devtunnels.ms/diawell_backend/"

    // -------------------------------------------------------------------------
    // Python FastAPI AI Gateway Settings (Port 8000 - Ollama qwen2.5:3b)
    // -------------------------------------------------------------------------
    const val EMULATOR_AI_BASE_URL = "http://10.0.2.2:8000/"
    const val PHYSICAL_DEVICE_AI_LAN_URL = "http://172.23.176.99:8000/"

    /**
     * Direct FastAPI AI Chat endpoint path.
     */
    const val CHAT_ENDPOINT = "api/v1/chat"

    /**
     * PHP Bridge fallback endpoint (Apache Port 80 -> FastAPI Port 8000).
     */
    const val PHP_BRIDGE_CHAT_ENDPOINT = "DiaWell_backend/ai/chat.php"

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
     * Active Base URL used by Retrofit for PHP authentication, profiles & logs.
     * Automatically routes to emulator loopback or physical device host IP.
     */
    val BASE_URL: String
        get() = if (isEmulator) EMULATOR_PHP_BASE_URL else PHYSICAL_DEVICE_PHP_LAN_URL

    /**
     * Active Base URL used by Retrofit for Dr. Jarvis AI Chatbot.
     * Automatically routes to emulator loopback or physical device host IP.
     */
    val AI_BASE_URL: String
        get() = if (isEmulator) EMULATOR_AI_BASE_URL else PHYSICAL_DEVICE_AI_LAN_URL
}
