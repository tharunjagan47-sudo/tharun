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
    // PHP Backend Settings (Port 80 - Apache/MySQL)
    // -------------------------------------------------------------------------
    const val EMULATOR_PHP_BASE_URL = "http://10.0.2.2/"
    const val PHYSICAL_DEVICE_PHP_LAN_URL = "http://192.168.1.9/" // Host PC LAN IPv4 from ipconfig (Laptop Wi-Fi)
    const val DEVTUNNEL_BASE_URL = " https://surcharge-blot-expenses.ngrok-free.dev/diawell_backend/"

    // -------------------------------------------------------------------------
    // Legacy / Microservice Settings (Kept for compatibility)
    // -------------------------------------------------------------------------
    const val EMULATOR_AI_BASE_URL = "http://10.0.2.2/"
    const val PHYSICAL_DEVICE_AI_LAN_URL = "http://192.168.1.9/"

    /**
     * Chat endpoint path on Apache Port 80.
     */
    const val CHAT_ENDPOINT = "diawell_backend/ai/chat.php"
    const val PHP_BRIDGE_CHAT_ENDPOINT = "diawell_backend/ai/chat.php"

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
     * Points directly to Apache Port 80 (where chat.php connects to local Ollama).
     */
    val AI_BASE_URL: String
        get() = BASE_URL
}
