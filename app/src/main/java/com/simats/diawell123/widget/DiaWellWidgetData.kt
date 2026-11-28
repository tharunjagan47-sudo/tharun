package com.simats.diawell123.widget

/**
 * Immutable snapshot of health data rendered in the DiaWell Home Screen Widget.
 */
data class DiaWellWidgetData(
    val glucoseValue: String = "",
    val glucoseUnit: String = "mg/dL",
    val glucoseStatus: String = "Normal",
    val glucoseTime: String = "",
    val hasGlucoseReading: Boolean = false,

    val nextMedName: String = "",
    val nextMedDose: String = "",
    val nextMedTime: String = "",
    val hasUpcomingMed: Boolean = false,
    val isMedAllCaughtUp: Boolean = false
)
