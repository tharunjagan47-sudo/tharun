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
    /**
     * Active Base URL used by Retrofit for PHP authentication, profiles & logs.
     * Set to PHYSICAL_DEVICE_PHP_LAN_URL when deploying to a physical Android device.
     */
    const val BASE_URL = EMULATOR_PHP_BASE_URL

    // -------------------------------------------------------------------------
    // Python FastAPI AI Gateway Settings (Port 8000 - Ollama qwen2.5:3b)
    // -------------------------------------------------------------------------
    const val EMULATOR_AI_BASE_URL = "http://10.0.2.2:8000/"
    const val PHYSICAL_DEVICE_AI_LAN_URL = "http://172.23.176.99:8000/"

    /**
     * Active Base URL used by Retrofit for Dr. Jarvis AI Chatbot.
     * Toggle to PHYSICAL_DEVICE_AI_LAN_URL when testing on a physical Android device.
     */
    const val AI_BASE_URL = EMULATOR_AI_BASE_URL

    /**
     * Direct FastAPI AI Chat endpoint path.
     * Full URL becomes: http://10.0.2.2:8000/api/v1/chat (Emulator)
     *                   http://172.23.176.99:8000/api/v1/chat (Physical Device)
     */
    const val CHAT_ENDPOINT = "api/v1/chat"
}


