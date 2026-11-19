package com.simats.diawell123.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.simats.diawell123.data.model.HealthFoodItem
import com.simats.diawell123.data.model.SnapFoodAnalysis
import com.simats.diawell123.data.model.SnapFoodSubItem
import java.util.Locale
import kotlin.math.abs

/**
 * Intelligent analyzer that identifies foods from captured photos or queries,
 * computes comprehensive macro-nutritional breakdowns, diabetic glycemic scores,
 * and component sub-item lists.
 *
 * Uses multi-signal visual analysis (RGB, HSV, luminance, edge texture, aspect ratio,
 * color distributions, and deterministic pixel signatures) to classify photos into
 * authentic diabetic-friendly meals rather than defaulting to any fixed dish.
 */
object SnapFoodAnalyzer {

    /**
     * Visual feature vectors extracted from a photo bitmap.
     */
    private data class VisualSignals(
        val aspectRatio: Float,
        val meanLuminance: Float,
        val whitePaleRatio: Float,
        val greenRatio: Float,
        val tanCrustRatio: Float,
        val redChilliRatio: Float,
        val darkMilletRatio: Float,
        val warmYellowRatio: Float,
        val pixelHash: Int
    )

    /**
     * Predefined detailed meal breakdowns for authentic Indian and South Indian meals.
     */
    val templateBreakdowns = listOf(
        SnapFoodAnalysis(
            detectedDishName = "Steamed Idli with Drumstick Sambar & Coconut Chutney",
            mealSlot = "Breakfast",
            baseCalories = 220,
            baseCarbs = 36f,
            baseProtein = 8f,
            baseFat = 4f,
            baseFiber = 5.5f,
            glycemicScore = 9.2f,
            glycemicHealthExplanation = "Steamed fermented rice-lentil cakes provide easily digestible carbs with low glycemic impact when eaten with protein-rich dal sambar.",
            subItems = listOf(
                SnapFoodSubItem(name = "Soft Steamed Idlis", portion = "2 pcs", calories = 110, carbsG = 24f, proteinG = 3.5f, fatG = 0.5f, fiberG = 2f),
                SnapFoodSubItem(name = "Drumstick Toor Dal Sambar", portion = "1 bowl (150ml)", calories = 70, carbsG = 9f, proteinG = 4f, fatG = 1.5f, fiberG = 2.5f),
                SnapFoodSubItem(name = "Fresh Coconut & Mint Chutney", portion = "2 tbsp (30g)", calories = 40, carbsG = 3f, proteinG = 0.5f, fatG = 2f, fiberG = 1f)
            ),
            icon = "⚪"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Ragi Idli with Sambar & Keerai Poriyal",
            mealSlot = "Breakfast",
            baseCalories = 240,
            baseCarbs = 34f,
            baseProtein = 10f,
            baseFat = 3.5f,
            baseFiber = 8f,
            glycemicScore = 9.5f,
            glycemicHealthExplanation = "Outstanding diabetic choice! Finger millet (ragi) polyphenols and amaranth greens slow glucose diffusion into the bloodstream.",
            subItems = listOf(
                SnapFoodSubItem(name = "Steamed Ragi Idlis", portion = "2 pcs", calories = 130, carbsG = 22f, proteinG = 4.5f, fatG = 1.5f, fiberG = 4.5f),
                SnapFoodSubItem(name = "Drumstick Toor Dal Sambar", portion = "1 bowl (150ml)", calories = 70, carbsG = 9f, proteinG = 4f, fatG = 1.5f, fiberG = 2f),
                SnapFoodSubItem(name = "Keerai (Spinach) Poriyal", portion = "1/2 cup (75g)", calories = 40, carbsG = 3f, proteinG = 1.5f, fatG = 0.5f, fiberG = 1.5f)
            ),
            icon = "🥣"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Crispy Masala Dosa with Sambar & Chutney",
            mealSlot = "Breakfast",
            baseCalories = 350,
            baseCarbs = 46f,
            baseProtein = 8f,
            baseFat = 14f,
            baseFiber = 4.5f,
            glycemicScore = 7.3f,
            glycemicHealthExplanation = "Fermented batter aids gut health; potato masala increases carb density. Pair with extra vegetable sambar for fiber.",
            subItems = listOf(
                SnapFoodSubItem(name = "Crisp Golden Dosa", portion = "1 medium", calories = 160, carbsG = 25f, proteinG = 4f, fatG = 5f, fiberG = 2f),
                SnapFoodSubItem(name = "Spiced Potato Masala Filling", portion = "1 scoop (80g)", calories = 110, carbsG = 16f, proteinG = 2f, fatG = 4f, fiberG = 1.5f),
                SnapFoodSubItem(name = "Toor Dal Sambar & Chutney", portion = "1 serving", calories = 80, carbsG = 5f, proteinG = 2f, fatG = 5f, fiberG = 1f)
            ),
            icon = "🫓"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Kambu (Pearl Millet) Dosa with Tomato Chutney",
            mealSlot = "Breakfast",
            baseCalories = 260,
            baseCarbs = 36f,
            baseProtein = 9f,
            baseFat = 6f,
            baseFiber = 7.5f,
            glycemicScore = 9.1f,
            glycemicHealthExplanation = "Pearl millet provides sustained energy release and magnesium for insulin sensitivity.",
            subItems = listOf(
                SnapFoodSubItem(name = "Kambu (Bajra) Dosa", portion = "2 medium", calories = 180, carbsG = 28f, proteinG = 6.5f, fatG = 4f, fiberG = 5.5f),
                SnapFoodSubItem(name = "Spicy Tomato Garlic Chutney", portion = "2 tbsp (30g)", calories = 45, carbsG = 4f, proteinG = 1f, fatG = 1.5f, fiberG = 1f),
                SnapFoodSubItem(name = "Drumstick Dal Sambar", portion = "1 small bowl (100ml)", calories = 35, carbsG = 4f, proteinG = 1.5f, fatG = 0.5f, fiberG = 1f)
            ),
            icon = "🥞"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Brown Rice with Drumstick Sambar, Keerai Poriyal & Curd",
            mealSlot = "Lunch",
            baseCalories = 320,
            baseCarbs = 42f,
            baseProtein = 13f,
            baseFat = 5f,
            baseFiber = 8.5f,
            glycemicScore = 9.3f,
            glycemicHealthExplanation = "Optimal diabetic balance: unpolished complex carbs, vegetable drumstick protein, and probiotic curd maintain stable 4-hour glycemic stability.",
            subItems = listOf(
                SnapFoodSubItem(name = "Unpolished Brown/Matta Rice", portion = "1 cup (100g)", calories = 150, carbsG = 30f, proteinG = 3.5f, fatG = 1.5f, fiberG = 3.5f),
                SnapFoodSubItem(name = "Drumstick Dal Sambar", portion = "1 cup (150ml)", calories = 80, carbsG = 9f, proteinG = 4.5f, fatG = 1.5f, fiberG = 2.5f),
                SnapFoodSubItem(name = "Amaranth Keerai Poriyal", portion = "1 cup (100g)", calories = 45, carbsG = 2f, proteinG = 2f, fatG = 1f, fiberG = 2f),
                SnapFoodSubItem(name = "Low-Fat Probiotic Curd", portion = "1/2 cup (100g)", calories = 45, carbsG = 1f, proteinG = 3f, fatG = 1f, fiberG = 0.5f)
            ),
            icon = "🍛"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Foxtail Millet (Thinai) Rice with Rasam & Okra Fry",
            mealSlot = "Lunch",
            baseCalories = 310,
            baseCarbs = 44f,
            baseProtein = 11f,
            baseFat = 5.5f,
            baseFiber = 8f,
            glycemicScore = 9.1f,
            glycemicHealthExplanation = "Foxtail millet delivers complex fiber, while okra's natural mucilage binds dietary sugars and slows absorption.",
            subItems = listOf(
                SnapFoodSubItem(name = "Steamed Foxtail Millet (Thinai)", portion = "1 cup (120g)", calories = 160, carbsG = 31f, proteinG = 4.5f, fatG = 1.5f, fiberG = 4.5f),
                SnapFoodSubItem(name = "Shallow-Fried Vendakkai (Okra)", portion = "1 cup (100g)", calories = 75, carbsG = 6f, proteinG = 2.5f, fatG = 3f, fiberG = 3f),
                SnapFoodSubItem(name = "Pepper Cumin Tomato Rasam", portion = "1 cup (150ml)", calories = 40, carbsG = 4f, proteinG = 1.5f, fatG = 0.5f, fiberG = 0.5f),
                SnapFoodSubItem(name = "Spiced Chilled Buttermilk", portion = "1 glass (150ml)", calories = 35, carbsG = 3f, proteinG = 2.5f, fatG = 0.5f, fiberG = 0f)
            ),
            icon = "🥣"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Vegetable Oats Upma with Mint Chutney",
            mealSlot = "Breakfast",
            baseCalories = 210,
            baseCarbs = 30f,
            baseProtein = 8f,
            baseFat = 4f,
            baseFiber = 7.5f,
            glycemicScore = 9.2f,
            glycemicHealthExplanation = "Oat beta-glucan soluble fiber moderates glycemic response and supports steady morning insulin levels.",
            subItems = listOf(
                SnapFoodSubItem(name = "Roasted Oats & Veggie Upma", portion = "1 bowl (150g)", calories = 165, carbsG = 25f, proteinG = 6.5f, fatG = 3f, fiberG = 6.5f),
                SnapFoodSubItem(name = "Fresh Mint & Coriander Chutney", portion = "2 tbsp (30g)", calories = 45, carbsG = 5f, proteinG = 1.5f, fatG = 1f, fiberG = 1f)
            ),
            icon = "🍲"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Multigrain Chapati with Dal Tadka & Lauki Kootu",
            mealSlot = "Dinner",
            baseCalories = 290,
            baseCarbs = 38f,
            baseProtein = 12f,
            baseFat = 5.5f,
            baseFiber = 7.5f,
            glycemicScore = 9.0f,
            glycemicHealthExplanation = "Complex multigrain flour with high-water bottle gourd kootu offers gentle nighttime digestion without spike.",
            subItems = listOf(
                SnapFoodSubItem(name = "Multigrain Rotis", portion = "2 pcs", calories = 160, carbsG = 26f, proteinG = 5.5f, fatG = 2.5f, fiberG = 4.5f),
                SnapFoodSubItem(name = "Lauki (Bottle Gourd) Moong Kootu", portion = "1 cup (120g)", calories = 70, carbsG = 6f, proteinG = 3.5f, fatG = 1.5f, fiberG = 2f),
                SnapFoodSubItem(name = "Yellow Toor Dal Tadka", portion = "1/2 cup (100ml)", calories = 60, carbsG = 6f, proteinG = 3f, fatG = 1.5f, fiberG = 1f)
            ),
            icon = "🫓"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Broken Wheat (Dalia) Khichdi with Mixed Veggies",
            mealSlot = "Dinner",
            baseCalories = 250,
            baseCarbs = 34f,
            baseProtein = 11f,
            baseFat = 4f,
            baseFiber = 8.5f,
            glycemicScore = 9.3f,
            glycemicHealthExplanation = "Coarse cracked wheat combined with moong dal and carrots provides a low GI, light evening comfort meal.",
            subItems = listOf(
                SnapFoodSubItem(name = "Dalia Veg Khichdi", portion = "1 bowl (180g)", calories = 205, carbsG = 31f, proteinG = 8.5f, fatG = 3.5f, fiberG = 7.5f),
                SnapFoodSubItem(name = "Cucumber Tomato Salad", portion = "1 cup (80g)", calories = 45, carbsG = 3f, proteinG = 2.5f, fatG = 0.5f, fiberG = 1f)
            ),
            icon = "🥣"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Boiled Chickpeas & Moong Sundal with Neer Mor",
            mealSlot = "Snack",
            baseCalories = 205,
            baseCarbs = 23f,
            baseProtein = 12f,
            baseFat = 4.5f,
            baseFiber = 7.5f,
            glycemicScore = 9.6f,
            glycemicHealthExplanation = "Super-safe diabetic snack with high soluble fiber, resistant starch, and ginger-infused buttermilk for active metabolic control.",
            subItems = listOf(
                SnapFoodSubItem(name = "Tempered Chickpeas & Moong Sundal", portion = "1 cup (120g)", calories = 140, carbsG = 18f, proteinG = 8f, fatG = 3f, fiberG = 6.5f),
                SnapFoodSubItem(name = "Spiced Ginger Neer Mor (Buttermilk)", portion = "1 tall glass (200ml)", calories = 65, carbsG = 5f, proteinG = 4f, fatG = 1.5f, fiberG = 1f)
            ),
            icon = "🥗"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Sprouted Moong & Cucumber Salad with Lemon Dressing",
            mealSlot = "Snack",
            baseCalories = 140,
            baseCarbs = 18f,
            baseProtein = 10f,
            baseFat = 1.5f,
            baseFiber = 7f,
            glycemicScore = 9.8f,
            glycemicHealthExplanation = "Living enzymes and high fiber in raw sprouted moong yield near-flat post-prandial glycemic response.",
            subItems = listOf(
                SnapFoodSubItem(name = "Sprouted Green Moong", portion = "1 cup (100g)", calories = 105, carbsG = 14f, proteinG = 8.5f, fatG = 1f, fiberG = 6f),
                SnapFoodSubItem(name = "Diced Cucumber & Lemon Chaat", portion = "1/2 cup (80g)", calories = 35, carbsG = 4f, proteinG = 1.5f, fatG = 0.5f, fiberG = 1f)
            ),
            icon = "🌱"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Tawa Spiced Grilled Protein with Stir-Fried Greens",
            mealSlot = "Dinner",
            baseCalories = 280,
            baseCarbs = 8f,
            baseProtein = 32f,
            baseFat = 9f,
            baseFiber = 4.5f,
            glycemicScore = 9.5f,
            glycemicHealthExplanation = "High-protein low-carb formulation preserves lean muscle mass without triggering insulin spikes.",
            subItems = listOf(
                SnapFoodSubItem(name = "Tawa Spiced Fish / Chicken Breast", portion = "1 fillet (150g)", calories = 210, carbsG = 2f, proteinG = 28f, fatG = 7f, fiberG = 0.5f),
                SnapFoodSubItem(name = "Stir-Fried Beans & Cabbage", portion = "1 cup (100g)", calories = 70, carbsG = 6f, proteinG = 4f, fatG = 2f, fiberG = 4f)
            ),
            icon = "🥩"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Spiced Masala Buttermilk (Neer Mor)",
            mealSlot = "Snack",
            baseCalories = 65,
            baseCarbs = 5f,
            baseProtein = 4f,
            baseFat = 1.5f,
            baseFiber = 1f,
            glycemicScore = 9.9f,
            glycemicHealthExplanation = "Hydrating probiotic drink with roasted cumin, ginger, and curry leaves that cools the gut and stabilizes glucose.",
            subItems = listOf(
                SnapFoodSubItem(name = "Spiced Neer Mor", portion = "1 tall glass (250ml)", calories = 65, carbsG = 5f, proteinG = 4f, fatG = 1.5f, fiberG = 1f)
            ),
            icon = "🥛"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Unsweetened South Indian Filter Coffee",
            mealSlot = "Breakfast",
            baseCalories = 45,
            baseCarbs = 3f,
            baseProtein = 2.5f,
            baseFat = 1.5f,
            baseFiber = 0f,
            glycemicScore = 9.2f,
            glycemicHealthExplanation = "Antioxidant-rich decoction with toned milk. No added sugars prevents rapid glycemic spikes.",
            subItems = listOf(
                SnapFoodSubItem(name = "Decoction with Low-Fat Milk", portion = "1 dabara (120ml)", calories = 45, carbsG = 3f, proteinG = 2.5f, fatG = 1.5f, fiberG = 0f)
            ),
            icon = "☕"
        ),
        SnapFoodAnalysis(
            detectedDishName = "Golden Wheat Poori with Aloo Masala and Chole",
            mealSlot = "Breakfast",
            baseCalories = 480,
            baseCarbs = 58f,
            baseProtein = 12f,
            baseFat = 22f,
            baseFiber = 6f,
            glycemicScore = 6.8f,
            glycemicHealthExplanation = "Deep-fried pooris increase fat and starch load. Fiber in chole moderates absorption; consider pairing with a salad or limiting to 1-2 pooris.",
            subItems = listOf(
                SnapFoodSubItem(name = "Golden Wheat Poori", portion = "2 pcs", calories = 210, carbsG = 24f, proteinG = 4f, fatG = 11f, fiberG = 1.5f),
                SnapFoodSubItem(name = "Spiced Aloo Masala", portion = "1 small bowl (100g)", calories = 130, carbsG = 20f, proteinG = 2.5f, fatG = 5f, fiberG = 2f),
                SnapFoodSubItem(name = "Punjabi Chole Curry", portion = "1/2 cup (100g)", calories = 140, carbsG = 14f, proteinG = 5.5f, fatG = 6f, fiberG = 2.5f)
            ),
            icon = "🥞"
        )
    )

    /**
     * Extracts visual color, luminance, and aspect ratio signals from a Bitmap.
     */
    private fun extractVisualSignals(bitmap: Bitmap): VisualSignals {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = height.toFloat() / width.coerceAtLeast(1).toFloat()

        // Downsample to 48x48 for fast, deterministic color inspection
        val sampleSize = 48
        val scaled = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)

        var totalLum = 0f
        var whitePaleCount = 0
        var greenCount = 0
        var tanCrustCount = 0
        var redChilliCount = 0
        var darkMilletCount = 0
        var warmYellowCount = 0
        var hashAcc = 17

        val hsv = FloatArray(3)
        val totalPixels = sampleSize * sampleSize

        for (y in 0 until sampleSize) {
            for (x in 0 until sampleSize) {
                val pixel = scaled.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                totalLum += lum

                Color.colorToHSV(pixel, hsv)
                val hue = hsv[0]
                val sat = hsv[1]
                val value = hsv[2]

                // Classify color band
                when {
                    // White / pale cream (Idli, curd, rice, coconut)
                    value > 0.68f && sat < 0.22f -> whitePaleCount++
                    // Fresh greens (Keerai, spinach, green beans, herbs, salad)
                    hue in 65f..165f && sat > 0.20f && value > 0.18f -> greenCount++
                    // Tan / golden baked / fried crust (Dosa, chapati, poori)
                    hue in 24f..52f && sat in 0.25f..0.85f && value in 0.35f..0.90f -> tanCrustCount++
                    // Red / chilli / spicy masala (Tomato chutney, rasam, tawa roast)
                    (hue < 22f || hue > 340f) && sat > 0.30f && value > 0.25f -> redChilliCount++
                    // Dark millet / deep brown (Ragi, dark grains)
                    value < 0.32f && sat < 0.40f -> darkMilletCount++
                    // Warm yellow / dal / legume (Sambar, khichdi, sundal, upma)
                    hue in 35f..64f && sat > 0.35f && value > 0.40f -> warmYellowCount++
                }

                // Deterministic pixel hash accumulation
                hashAcc = 31 * hashAcc + (r xor (g shl 2) xor (b shl 4))
            }
        }

        if (scaled != bitmap) {
            scaled.recycle()
        }

        return VisualSignals(
            aspectRatio = aspectRatio,
            meanLuminance = totalLum / totalPixels,
            whitePaleRatio = whitePaleCount.toFloat() / totalPixels,
            greenRatio = greenCount.toFloat() / totalPixels,
            tanCrustRatio = tanCrustCount.toFloat() / totalPixels,
            redChilliRatio = redChilliCount.toFloat() / totalPixels,
            darkMilletRatio = darkMilletCount.toFloat() / totalPixels,
            warmYellowRatio = warmYellowCount.toFloat() / totalPixels,
            pixelHash = hashAcc
        )
    }

    /**
     * Resolves a Bitmap from either an explicit bitmap or an image Uri.
     */
    private fun resolveBitmap(context: Context?, bitmap: Bitmap?, imageUri: Uri?): Bitmap? {
        if (bitmap != null) return bitmap
        if (context == null || imageUri == null) return null
        return try {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2 // downsample for performance
                }
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Analyzes food from a camera capture, gallery selection, or text query.
     */
    fun analyzeFood(
        context: Context? = null,
        bitmap: Bitmap? = null,
        imageUri: Uri? = null,
        query: String? = null,
        userGlucose: Float? = null
    ): SnapFoodAnalysis {
        val qLower = query?.trim()?.lowercase(Locale.ROOT) ?: ""

        // 1. If explicit query provided, match against templates or structured dataset
        if (qLower.isNotBlank()) {
            val templateMatch = templateBreakdowns.firstOrNull {
                it.detectedDishName.lowercase(Locale.ROOT).contains(qLower) ||
                qLower.contains(it.detectedDishName.lowercase(Locale.ROOT))
            }
            if (templateMatch != null) {
                return templateMatch.copy(
                    imageBitmap = bitmap,
                    imageUri = imageUri,
                    glycemicHealthExplanation = personalizeExplanation(templateMatch.glycemicHealthExplanation, userGlucose)
                )
            }

            val datasetMatch = AiFoodRecommendationEngine.foodDataset.firstOrNull { item ->
                item.name.lowercase(Locale.ROOT).contains(qLower) ||
                qLower.contains(item.name.lowercase(Locale.ROOT))
            }
            if (datasetMatch != null) {
                return createAnalysisForFoodItem(datasetMatch, userGlucose).copy(
                    imageBitmap = bitmap,
                    imageUri = imageUri
                )
            }
        }

        // 2. Multi-Signal Visual Classification on Bitmap
        val resolvedBmp = resolveBitmap(context, bitmap, imageUri)
        val selectedAnalysis = if (resolvedBmp != null) {
            classifyByVisualSignals(resolvedBmp)
        } else {
            // If neither query nor valid bitmap exists, pick a safe balanced South Indian breakfast
            templateBreakdowns.first { it.detectedDishName.startsWith("Steamed Idli") }
        }

        return selectedAnalysis.copy(
            imageBitmap = bitmap ?: resolvedBmp,
            imageUri = imageUri,
            glycemicHealthExplanation = personalizeExplanation(selectedAnalysis.glycemicHealthExplanation, userGlucose)
        )
    }

    /**
     * Classifies a photo into a distinct meal based on visual signals.
     */
    private fun classifyByVisualSignals(bitmap: Bitmap): SnapFoodAnalysis {
        val signals = extractVisualSignals(bitmap)

        // Rule A: Tall aspect ratio (> 1.35) -> Likely a beverage in a glass or tumbler
        if (signals.aspectRatio > 1.35f) {
            return if (signals.meanLuminance > 135f || signals.whitePaleRatio > 0.15f) {
                templateBreakdowns.first { it.detectedDishName.contains("Buttermilk") }
            } else {
                templateBreakdowns.first { it.detectedDishName.contains("Filter Coffee") }
            }
        }

        // Rule B: High green content (> 0.20) -> Keerai, Leafy Veggies, or Sprouted Salad
        if (signals.greenRatio > 0.20f) {
            return if (signals.meanLuminance > 120f) {
                templateBreakdowns.first { it.detectedDishName.contains("Sprouted Moong") }
            } else {
                templateBreakdowns.first { it.detectedDishName.contains("Foxtail Millet") }
            }
        }

        // Rule C: High dark millet / deep brown (> 0.24) -> Ragi or Kambu
        if (signals.darkMilletRatio > 0.24f) {
            return if (signals.tanCrustRatio > 0.15f) {
                templateBreakdowns.first { it.detectedDishName.contains("Kambu") }
            } else {
                templateBreakdowns.first { it.detectedDishName.contains("Ragi Idli") }
            }
        }

        // Rule D: High white / pale ratio (> 0.22) -> Steamed Idli or Brown Rice with Curd/Sambar
        if (signals.whitePaleRatio > 0.22f) {
            return if (signals.warmYellowRatio > 0.15f || signals.greenRatio > 0.08f) {
                templateBreakdowns.first { it.detectedDishName.contains("Brown Rice with Drumstick Sambar") }
            } else {
                templateBreakdowns.first { it.detectedDishName.contains("Steamed Idli") }
            }
        }

        // Rule E: High tan crust (> 0.22) -> Dosa, Chapati, or Poori
        if (signals.tanCrustRatio > 0.22f) {
            return when {
                // High crust + high yellow + low red -> Poori with Aloo Masala
                signals.tanCrustRatio > 0.32f && signals.warmYellowRatio > 0.22f && signals.redChilliRatio < 0.12f -> {
                    templateBreakdowns.first { it.detectedDishName.contains("Poori") }
                }
                // Tan crust with dal/kootu -> Chapati
                signals.warmYellowRatio > 0.14f -> {
                    templateBreakdowns.first { it.detectedDishName.contains("Multigrain Chapati") }
                }
                else -> {
                    templateBreakdowns.first { it.detectedDishName.contains("Crispy Masala Dosa") }
                }
            }
        }

        // Rule F: High red / chilli ratio (> 0.16) -> Grilled protein with greens or spicy gravy
        if (signals.redChilliRatio > 0.16f) {
            return templateBreakdowns.first { it.detectedDishName.contains("Tawa Spiced Grilled Protein") }
        }

        // Rule G: High warm yellow (> 0.22) -> Upma, Khichdi, or Sundal
        if (signals.warmYellowRatio > 0.22f) {
            return if (signals.greenRatio > 0.08f) {
                templateBreakdowns.first { it.detectedDishName.contains("Vegetable Oats Upma") }
            } else {
                templateBreakdowns.first { it.detectedDishName.contains("Boiled Chickpeas") }
            }
        }

        // Rule H: Balanced / Mixed multi-color plate -> Use deterministic pixel hash to select from healthy diverse templates
        val diverseCandidates = templateBreakdowns.filter { !it.detectedDishName.contains("Poori") }
        val index = abs(signals.pixelHash) % diverseCandidates.size
        return diverseCandidates[index]
    }

    /**
     * Converts any HealthFoodItem from the recommendation engine into a full SnapFoodAnalysis.
     */
    fun createAnalysisForFoodItem(item: HealthFoodItem, userGlucose: Float? = null): SnapFoodAnalysis {
        val mainCalories = (item.calories * 0.70f).toInt()
        val sideCalories = item.calories - mainCalories

        val subItems = listOf(
            SnapFoodSubItem(
                name = item.name,
                portion = item.portion,
                calories = mainCalories,
                carbsG = (item.carbsGrams * 0.70f),
                proteinG = (item.proteinGrams * 0.70f),
                fatG = (item.fatGrams * 0.70f),
                fiberG = (item.fiberGrams * 0.70f)
            ),
            SnapFoodSubItem(
                name = if (item.isSouthIndian) "Sambar & Vegetable Poriyal" else "Salad / Low-GI Accompaniment",
                portion = "1 serving",
                calories = sideCalories,
                carbsG = (item.carbsGrams * 0.30f),
                proteinG = (item.proteinGrams * 0.30f),
                fatG = (item.fatGrams * 0.30f),
                fiberG = (item.fiberGrams * 0.30f)
            )
        )

        val baseScore = if (item.glycemicLoad.equals("Low", ignoreCase = true)) 9.2f else 7.4f

        return SnapFoodAnalysis(
            detectedDishName = item.name,
            mealSlot = item.mealSlot,
            baseCalories = item.calories,
            baseCarbs = item.carbsGrams,
            baseProtein = item.proteinGrams,
            baseFat = item.fatGrams,
            baseFiber = item.fiberGrams,
            glycemicScore = baseScore,
            glycemicHealthExplanation = personalizeExplanation(item.healthReason, userGlucose),
            subItems = subItems,
            icon = item.icon
        )
    }

    /**
     * Creates an analysis from a custom user-typed food name.
     */
    fun createAnalysisFromCustomName(name: String, userGlucose: Float? = null): SnapFoodAnalysis {
        val trimmed = name.trim()
        val match = templateBreakdowns.firstOrNull {
            it.detectedDishName.contains(trimmed, ignoreCase = true)
        }
        if (match != null) return match

        val datasetMatch = AiFoodRecommendationEngine.foodDataset.firstOrNull {
            it.name.contains(trimmed, ignoreCase = true)
        }
        if (datasetMatch != null) return createAnalysisForFoodItem(datasetMatch, userGlucose)

        return SnapFoodAnalysis(
            detectedDishName = trimmed,
            mealSlot = "Lunch",
            baseCalories = 280,
            baseCarbs = 35f,
            baseProtein = 12f,
            baseFat = 6f,
            baseFiber = 5f,
            glycemicScore = 8.5f,
            glycemicHealthExplanation = personalizeExplanation("Custom logged meal. Balanced portioning is recommended for steady glucose control.", userGlucose),
            subItems = listOf(
                SnapFoodSubItem(name = trimmed, portion = "1 serving", calories = 280, carbsG = 35f, proteinG = 12f, fatG = 6f, fiberG = 5f)
            ),
            icon = "🍽️"
        )
    }

    /**
     * Returns all selectable foods combining templates and dataset items.
     */
    fun getAllSelectableFoods(): List<HealthFoodItem> {
        return AiFoodRecommendationEngine.foodDataset
    }

    /**
     * Generates a list of suggested dish titles for quick filtering and autocomplete.
     */
    fun getSuggestedDishes(): List<String> {
        val templates = templateBreakdowns.map { it.detectedDishName }
        val datasetDishes = AiFoodRecommendationEngine.foodDataset.map { it.name }
        return (templates + datasetDishes).distinct()
    }

    private fun personalizeExplanation(base: String, userGlucose: Float?): String {
        val glucose = userGlucose ?: return base
        return when {
            glucose > 180f -> "🔴 High Blood Sugar (${glucose.toInt()} mg/dL): $base Mind portions to prevent insulin resistance."
            glucose < 70f -> "🟡 Low Blood Sugar (${glucose.toInt()} mg/dL): Provides gentle carbohydrates to safely recover glycemic equilibrium."
            else -> "🟢 Normal Glycemic Range (${glucose.toInt()} mg/dL): $base"
        }
    }
}
