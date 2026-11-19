package com.simats.diawell123.data.model

import android.graphics.Bitmap
import android.net.Uri
import java.util.UUID

/**
 * Individual component of a recognized meal (e.g. Poori, Aloo Masala, Chole Curry).
 */
data class SnapFoodSubItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val portion: String,
    val calories: Int,
    val carbsG: Float,
    val proteinG: Float,
    val fatG: Float,
    val fiberG: Float = 0f
)

/**
 * Detailed photo analysis & nutrition breakdown model for Snap Food.
 */
data class SnapFoodAnalysis(
    val detectedDishName: String,
    val mealSlot: String = "Lunch", // "Breakfast", "Lunch", "Snack", "Dinner"
    val baseCalories: Int,
    val baseCarbs: Float,
    val baseProtein: Float,
    val baseFat: Float,
    val baseFiber: Float,
    val glycemicScore: Float, // e.g. 7.5
    val glycemicHealthExplanation: String,
    val subItems: List<SnapFoodSubItem> = emptyList(),
    val imageBitmap: Bitmap? = null,
    val imageUri: Uri? = null,
    val icon: String = "🍽️",
    val needsUserSelection: Boolean = false
)
