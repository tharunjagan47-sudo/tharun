package com.simats.diawell123.util

/**
 * Sanitizes technical exceptions, IP addresses, database/server jargon, and stack traces
 * into calm, consumer-friendly copy suitable for production.
 */
fun Throwable?.toCleanErrorMessage(fallback: String = "Something went wrong. Please try again."): String {
    if (this == null) return fallback
    val msg = this.localizedMessage ?: this.message ?: ""
    return msg.toCleanErrorMessage(fallback)
}

fun String?.toCleanErrorMessage(fallback: String = "Something went wrong. Please try again."): String {
    if (this.isNullOrBlank()) return fallback
    if (contains("ConnectException", ignoreCase = true) ||
        contains("SocketTimeoutException", ignoreCase = true) ||
        contains("UnknownHostException", ignoreCase = true) ||
        contains("Failed to connect", ignoreCase = true) ||
        contains("127.0.0.1") || contains("localhost") || contains("10.0.2.2") ||
        contains("HTTP 5") || contains("HTTP 4") ||
        contains("User not specified", ignoreCase = true) ||
        contains("Ollama", ignoreCase = true) ||
        contains("MySQL", ignoreCase = true) ||
        contains("PHP", ignoreCase = true) ||
        contains("JSON", ignoreCase = true) ||
        contains("Retrofit", ignoreCase = true) ||
        contains("OkHttp", ignoreCase = true) ||
        contains("SQL", ignoreCase = true) ||
        contains("token", ignoreCase = true) ||
        contains("route", ignoreCase = true)
    ) {
        return fallback
    }
    return if (length > 90 || contains("Exception") || contains("at com.") || contains("NullPointer")) fallback else this
}
