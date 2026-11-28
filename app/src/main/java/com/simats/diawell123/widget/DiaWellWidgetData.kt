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
    val isMedAllCaughtUp: Boolean = false,

    val activityTitle: String = "",
    val activityStats: String = "",
    val hasActivityToday: Boolean = false,

    val nextDietMealType: String = "",
    val nextDietDishName: String = "",
    val nextDietCalories: String = "",
    val hasNextDiet: Boolean = false
)
