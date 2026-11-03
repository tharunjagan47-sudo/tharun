package com.simats.diawell123.data.api

/**
 * Centralized Network Configuration for DiaWell Backend Communication.
 *
 * Configurable endpoints:
 * - EMULATOR_BASE_URL: Loopback address for the standard Android Studio Emulator ("http://10.0.2.2/")
 * - PHYSICAL_DEVICE_LAN_URL: Host machine Wi-Fi IPv4 address when testing on a physical Android device (e.g., "http://192.168.1.100/")
 * - DEVTUNNEL_BASE_URL: Cloud DevTunnel proxy URL ("https://bhft6cwn-80.inc1.devtunnels.ms/")
 */
object NetworkConstants {

    // -------------------------------------------------------------------------
    // Target Environment Toggles
    // -------------------------------------------------------------------------
    const val EMULATOR_BASE_URL = "http://10.0.2.2/"
    const val PHYSICAL_DEVICE_LAN_URL = "http://192.168.1.100/" // Update with your PC's Wi-Fi IP from `ipconfig`
    const val DEVTUNNEL_BASE_URL = "https://bhft6cwn-80.inc1.devtunnels.ms/diawell_backend"

    /**
     * Active Base URL used by Retrofit across DiaWell.
     * Default: Android Studio Emulator loopback.
     */
    const val BASE_URL = EMULATOR_BASE_URL

    /**
     * Chat endpoint path relative to the root server.
     * Maps to: http://192.168.1.100/DiaWell_backend/ai/chat.php
     */
    const val CHAT_ENDPOINT = "DiaWell_backend/ai/chat.php"
}

