package com.simats.diawell123.data.repository

import android.content.Context
import com.simats.diawell123.data.model.DailyPlanData
import com.simats.diawell123.data.model.GlycemicDetails
import com.simats.diawell123.data.model.HealthFoodItem
import com.simats.diawell123.data.model.RecommendedFoodItem
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

data class HealthRecommendationResult(
    val item: HealthFoodItem,
    val matchedReason: String,
    val glucoseBadgeText: String,
    val dailyRotationLabel: String,
    val totalAvailableMatches: Int,
    val currentIndex: Int
)

/**
 * Dataset-Driven Daily Rotating & Non-Repeating AI Food Recommendation Engine.
 * Matches user allergies, dietary preferences (Veg/Non-Veg/Vegan), and live glucose levels
 * while prioritizing authentic South Indian diabetic-friendly meals and ensuring daily rotation.
 */
object AiFoodRecommendationEngine {

    private const val PREFS_NAME = "prefs_ai_food_engine"
    private const val KEY_RECENT_IDS = "recently_recommended_food_ids"
    private const val MAX_RECENT_HISTORY = 10

    /**
     * Comprehensive diabetes-friendly health food dataset with strong South Indian representation.
     */
    val foodDataset: List<HealthFoodItem> = listOf(
        // ==================== BREAKFAST ====================
        HealthFoodItem(
            id = 101,
            name = "Ragi Idli with Sambar",
            mealSlot = "Breakfast",
            calories = 185,
            carbsGrams = 28f,
            proteinGrams = 6.5f,
            fiberGrams = 6f,
            fatGrams = 2.5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced"),
            healthReason = "Finger millet (ragi) provides slow-digesting polyphenols and complex carbs that prevent morning glucose spikes.",
            portion = "2 ragi idlis with 1 bowl vegetable sambar",
            icon = "⚪",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 102,
            name = "Moong Dal Pesarattu with Mint Chutney",
            mealSlot = "Breakfast",
            calories = 215,
            carbsGrams = 22f,
            proteinGrams = 13f,
            fiberGrams = 6.5f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb", "Balanced"),
            healthReason = "Whole green gram provides high plant protein and resistant starch, yielding a remarkably flat post-meal glucose response.",
            portion = "2 pesarattus with 2 tbsp mint-coriander chutney",
            icon = "🥞",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 103,
            name = "Oats & Rava Upma with Mixed Vegetables",
            mealSlot = "Breakfast",
            calories = 205,
            carbsGrams = 30f,
            proteinGrams = 7f,
            fiberGrams = 5.5f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Gluten"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced"),
            healthReason = "Beta-glucan soluble fiber paired with carrots, beans, and mustard tempering blunts carbohydrate absorption speed.",
            portion = "1 medium bowl (220g)",
            icon = "🍲",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 104,
            name = "Kambu (Pearl Millet) Dosa with Tomato Chutney",
            mealSlot = "Breakfast",
            calories = 190,
            carbsGrams = 27f,
            proteinGrams = 6f,
            fiberGrams = 5.5f,
            fatGrams = 3.5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb", "Balanced"),
            healthReason = "Pearl millet (kambu) has high mineral magnesium content and a very low glycemic load that supports insulin receptor sensitivity.",
            portion = "2 dosas with 2 tbsp tomato chutney",
            icon = "🫓",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 105,
            name = "Semi-Boiled Egg Whites with Steamed Idli & Sambar",
            mealSlot = "Breakfast",
            calories = 195,
            carbsGrams = 22f,
            proteinGrams = 15f,
            fiberGrams = 3.5f,
            fatGrams = 2.5f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Eggs"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "PostWorkout", "Balanced"),
            healthReason = "Traditional soft steamed idlis paired with bioavailable albumin egg protein slow gastric emptying and prevent insulin spikes.",
            portion = "2 idlis + 3 boiled egg whites + 1/2 bowl sambar",
            icon = "🍳",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 106,
            name = "Foxtail Millet (Thinai) Pongal with Low-Fat Chutney",
            mealSlot = "Breakfast",
            calories = 210,
            carbsGrams = 29f,
            proteinGrams = 7.5f,
            fiberGrams = 6.5f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced"),
            healthReason = "Thinai millet cooked with yellow moong dal, ginger, and crushed pepper provides continuous, sustained morning energy.",
            portion = "1 medium bowl + 1 tbsp coriander chutney",
            icon = "🥣",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 107,
            name = "Sprouted Moong Dal Chilla with Paneer",
            mealSlot = "Breakfast",
            calories = 220,
            carbsGrams = 20f,
            proteinGrams = 14f,
            fiberGrams = 6f,
            fatGrams = 6f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "Balanced"),
            healthReason = "High plant protein and active dietary fiber stabilize fasting sugar and sustain morning satiety.",
            portion = "2 chillas",
            icon = "🥞",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 108,
            name = "Spinach & Egg White Scramble with Multigrain Toast",
            mealSlot = "Breakfast",
            calories = 195,
            carbsGrams = 14f,
            proteinGrams = 16f,
            fiberGrams = 3.5f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Eggs", "Gluten"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Ultra-low carbohydrate load with pure bioavailable albumin protein protects morning insulin sensitivity.",
            portion = "3 whites scrambled + 1 toast",
            icon = "🍳",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 109,
            name = "Tofu & Bell Pepper Bhurji with Avocado",
            mealSlot = "Breakfast",
            calories = 205,
            carbsGrams = 10f,
            proteinGrams = 15f,
            fiberGrams = 5f,
            fatGrams = 8f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Soy"),
            healthTags = listOf("HighSugarSafe", "LowCarb"),
            healthReason = "Near-zero glycemic load meal combining soy isoflavones and heart-healthy monounsaturated fats.",
            portion = "1 bowl (200g)",
            icon = "🥗",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 110,
            name = "Methi (Fenugreek) Thepla with Low-Fat Curd",
            mealSlot = "Breakfast",
            calories = 190,
            carbsGrams = 26f,
            proteinGrams = 7f,
            fiberGrams = 5.5f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Gluten", "Dairy"),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Fenugreek alkaloid trigonelline has clinically proven hypoglycemic properties that enhance insulin sensitivity.",
            portion = "2 theplas with 1/2 cup curd",
            icon = "🫓",
            isSouthIndian = false
        ),

        // ==================== LUNCH ====================
        HealthFoodItem(
            id = 201,
            name = "Brown/Matta Rice with Drumstick Sambar, Keerai Poriyal & Curd",
            mealSlot = "Lunch",
            calories = 320,
            carbsGrams = 42f,
            proteinGrams = 13f,
            fiberGrams = 8.5f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced"),
            healthReason = "Unpolished red matta rice paired with drumstick toor dal and amaranth keerai poriyal prevents sharp postprandial glycemic excursions.",
            portion = "1 small cup rice + 1 cup sambar + 1 cup keerai + 1/2 cup curd",
            icon = "🍛",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 202,
            name = "Foxtail Millet (Thinai) Sambar Rice with Keerai Poriyal & Curd",
            mealSlot = "Lunch",
            calories = 310,
            carbsGrams = 38f,
            proteinGrams = 14f,
            fiberGrams = 9f,
            fatGrams = 4.5f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced", "LowCarb"),
            healthReason = "Foxtail millet delivers complex amylose starch that digests slowly, sustaining balanced glucose levels for 4+ hours.",
            portion = "1 bowl thinai sambar + 1 cup keerai poriyal + curd",
            icon = "🍲",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 203,
            name = "Lemon Coriander Millet Rice with Vendakkai (Okra) Fry & Buttermilk",
            mealSlot = "Lunch",
            calories = 290,
            carbsGrams = 36f,
            proteinGrams = 9f,
            fiberGrams = 7f,
            fatGrams = 6f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced"),
            healthReason = "Okra (vendakkai) contains soluble mucilage fiber that delays carbohydrate absorption, while ginger neer mor aids metabolic gut balance.",
            portion = "1 bowl millet rice + 1 cup shallow okra fry + 1 glass neer mor",
            icon = "🍋",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 204,
            name = "Pepper Rasam with Cabbage-Beans Poriyal & Grilled Paneer",
            mealSlot = "Lunch",
            calories = 280,
            carbsGrams = 18f,
            proteinGrams = 17f,
            fiberGrams = 7.5f,
            fatGrams = 9f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb", "Balanced"),
            healthReason = "Piperine-rich South Indian pepper rasam stimulates natural digestive enzymes, while paneer protein keeps carbohydrate intake low.",
            portion = "1 bowl rasam + 1 cup cabbage beans poriyal + 100g tawa paneer",
            icon = "🧀",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 205,
            name = "Pepper Rasam with Cabbage-Beans Poriyal & Grilled Chicken",
            mealSlot = "Lunch",
            calories = 275,
            carbsGrams = 14f,
            proteinGrams = 28f,
            fiberGrams = 6.5f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Pure lean chicken protein paired with traditional cumin-pepper rasam and fibrous vegetables keeps glucose completely steady.",
            portion = "1 bowl rasam + 1 cup poriyal + 150g grilled chicken",
            icon = "🍗",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 206,
            name = "Horse Gram (Kollu) Sambar with Red Rice & Cabbage Poriyal",
            mealSlot = "Lunch",
            calories = 310,
            carbsGrams = 40f,
            proteinGrams = 14f,
            fiberGrams = 9f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced"),
            healthReason = "Kollu (horse gram) has traditional Ayurvedic prominence for enhancing insulin sensitivity and reducing insulin resistance.",
            portion = "1 cup kollu sambar + 1 small cup red rice + cabbage poriyal",
            icon = "🍛",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 207,
            name = "South Indian Spiced Fish Curry with Samai (Little Millet) Rice",
            mealSlot = "Lunch",
            calories = 320,
            carbsGrams = 32f,
            proteinGrams = 26f,
            fiberGrams = 6f,
            fatGrams = 6.5f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Seafood"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced"),
            healthReason = "Omega-3 rich fish curry prepared with fenugreek and tomato simmer paired with little millet gives steady glycemic control.",
            portion = "1 bowl fish curry (1 fillet) + 1 small bowl samai rice",
            icon = "🐟",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 208,
            name = "Paneer Tikka with Grilled Bell Peppers & Roti",
            mealSlot = "Lunch",
            calories = 310,
            carbsGrams = 24f,
            proteinGrams = 19f,
            fiberGrams = 6.5f,
            fatGrams = 11f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy", "Gluten"),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "High dairy calcium and casein protein combined with non-starchy peppers create a slow, gradual glycemic release.",
            portion = "6 paneer cubes + 1 roti",
            icon = "🧀",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 209,
            name = "Soya Chunks & Vegetable Biryani with Flax Raita",
            mealSlot = "Lunch",
            calories = 295,
            carbsGrams = 36f,
            proteinGrams = 20f,
            fiberGrams = 8f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Soy", "Dairy"),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Textured soy protein absorbs cooking broth, reducing overall net starch content while supplying complete essential amino acids.",
            portion = "1 medium bowl + 1/2 cup raita",
            icon = "🍚",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 210,
            name = "Pan-Seared Salmon Fillet with Cauli-Mash & Asparagus",
            mealSlot = "Lunch",
            calories = 315,
            carbsGrams = 8f,
            proteinGrams = 28f,
            fiberGrams = 4.5f,
            fatGrams = 13f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Seafood"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Zero net glycemic impact meal rich in anti-inflammatory EPA and DHA omega-3 fatty acids for cardiovascular health.",
            portion = "1 fillet (180g) + 1 cup cauli-mash",
            icon = "🐟",
            isSouthIndian = false
        ),

        // ==================== SNACK ====================
        HealthFoodItem(
            id = 301,
            name = "Boiled Chickpeas / Moong Sundal",
            mealSlot = "Snack",
            calories = 140,
            carbsGrams = 18f,
            proteinGrams = 8f,
            fiberGrams = 6.5f,
            fatGrams = 3f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb", "Balanced"),
            healthReason = "Traditional temple-style sundal tempered with mustard, curry leaves, and minimal coconut provides satiating slow carbs and fiber.",
            portion = "1 small cup (100g)",
            icon = "🥗",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 302,
            name = "Roasted Fox Nuts (Makhana) & Peanuts",
            mealSlot = "Snack",
            calories = 155,
            carbsGrams = 15f,
            proteinGrams = 6f,
            fiberGrams = 4f,
            fatGrams = 7f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Nuts"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb"),
            healthReason = "Crunchy makhana and roasted groundnuts tossed in turmeric and curry leaves stabilize mid-day hunger without glucose spikes.",
            portion = "1/2 cup (40g)",
            icon = "🥜",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 303,
            name = "Sprouted Moong Salad with Lemon & Black Salt",
            mealSlot = "Snack",
            calories = 120,
            carbsGrams = 16f,
            proteinGrams = 9f,
            fiberGrams = 6f,
            fatGrams = 1.5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "Balanced"),
            healthReason = "Sprouted whole moong contains active enzymes, vitamin C, and soluble fiber that keep pre-dinner blood sugar level.",
            portion = "1 medium bowl (150g)",
            icon = "🌱",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 304,
            name = "Masala Buttermilk (Neer Mor) with Ginger & Curry Leaves",
            mealSlot = "Snack",
            calories = 65,
            carbsGrams = 5f,
            proteinGrams = 4f,
            fiberGrams = 1f,
            fatGrams = 1.5f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb"),
            healthReason = "Traditional South Indian spiced buttermilk hydrates, delivers digestive probiotics, and has near-zero glycemic impact.",
            portion = "1 tall glass (250ml)",
            icon = "🥛",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 305,
            name = "Unsweetened South Indian Filter Coffee",
            mealSlot = "Snack",
            calories = 55,
            carbsGrams = 4f,
            proteinGrams = 3f,
            fiberGrams = 0.5f,
            fatGrams = 2f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb"),
            healthReason = "Rich chicory filter decoction with low-fat cow's milk and zero sugar delivers antioxidants without raising blood glucose.",
            portion = "1 davarah tumbler (120ml)",
            icon = "☕",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 306,
            name = "Roasted Chana (Bengal Gram) with Curry Leaves",
            mealSlot = "Snack",
            calories = 135,
            carbsGrams = 16f,
            proteinGrams = 8.5f,
            fiberGrams = 5.5f,
            fatGrams = 2.5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb"),
            healthReason = "Low glycemic index whole roasted chickpeas satisfy afternoon salt cravings while keeping glucose flat.",
            portion = "1/2 cup (50g)",
            icon = "🧆",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 307,
            name = "Boiled Egg White Chaat with Onion & Lemon",
            mealSlot = "Snack",
            calories = 110,
            carbsGrams = 4f,
            proteinGrams = 14f,
            fiberGrams = 1.5f,
            fatGrams = 1f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Eggs"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Pure protein snack with near-zero sugar content, perfect for preventing pre-dinner hypoglycemia without adding excess calories.",
            portion = "3 whites chopped",
            icon = "🥚",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 308,
            name = "Greek Yogurt with Toasted Walnuts & Cinnamon",
            mealSlot = "Snack",
            calories = 150,
            carbsGrams = 8f,
            proteinGrams = 12f,
            fiberGrams = 2f,
            fatGrams = 6f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy", "Nuts"),
            healthTags = listOf("HighSugarSafe", "LowCarb"),
            healthReason = "Cinnamon contains cinnamaldehyde which mimics insulin, while walnuts contribute cardioprotective alpha-linolenic acid.",
            portion = "1 small bowl (120g)",
            icon = "🥣",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 309,
            name = "Fresh Guava Slices with Black Salt",
            mealSlot = "Snack",
            calories = 70,
            carbsGrams = 14f,
            proteinGrams = 2.5f,
            fiberGrams = 5.5f,
            fatGrams = 1f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Very low glycemic index (GI 12) fruit packed with 4x the vitamin C of an orange and high pectin soluble fiber.",
            portion = "1 medium guava sliced",
            icon = "🍐",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 310,
            name = "Mixed Roasted Seeds (Pumpkin, Sunflower, Flax)",
            mealSlot = "Snack",
            calories = 135,
            carbsGrams = 6f,
            proteinGrams = 6f,
            fiberGrams = 4f,
            fatGrams = 9f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LowCarb"),
            healthReason = "Abundant magnesium and lignans assist glucose cellular uptake and modulate post-snack insulin demand.",
            portion = "2 tbsp (25g)",
            icon = "🌻",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 311,
            name = "Herbal Green Tea with Mint & Lemon",
            mealSlot = "Snack",
            calories = 15,
            carbsGrams = 1.5f,
            proteinGrams = 0.5f,
            fiberGrams = 0.5f,
            fatGrams = 0.2f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LowCarb", "Hydration"),
            healthReason = "Rich in EGCG catechins and polyphenols that naturally improve insulin sensitivity and glycemic stability.",
            portion = "1 cup (200ml)",
            icon = "🍵",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 312,
            name = "Unsweetened Masala Chai (Spiced Tea)",
            mealSlot = "Snack",
            calories = 45,
            carbsGrams = 3.5f,
            proteinGrams = 2.5f,
            fiberGrams = 0.5f,
            fatGrams = 1.8f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb"),
            healthReason = "Brewed with ginger, cardamom, and cinnamon which enhances glucose uptake and digestion without spikes.",
            portion = "1 cup (150ml)",
            icon = "🍵",
            isSouthIndian = true
        ),

        // ==================== DINNER ====================
        HealthFoodItem(
            id = 401,
            name = "Multigrain Chapati with Lauki (Bottle Gourd) & Chana Dal Kootu",
            mealSlot = "Dinner",
            calories = 240,
            carbsGrams = 32f,
            proteinGrams = 10f,
            fiberGrams = 7.5f,
            fatGrams = 4.5f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Gluten"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LightEvening", "Balanced"),
            healthReason = "High-water-content bottle gourd combined with chana dal provides a light, easily digestible evening meal that supports overnight glucose control.",
            portion = "2 phulkas with 1 cup lauki kootu",
            icon = "🫓",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 402,
            name = "Broken Wheat (Godhumai Rava / Dalia) Khichdi with Mixed Veggies",
            mealSlot = "Dinner",
            calories = 220,
            carbsGrams = 30f,
            proteinGrams = 8f,
            fiberGrams = 7f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Gluten"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LightEvening", "Balanced"),
            healthReason = "Cracked whole wheat retains germ and aleurone layers, yielding an ultra-low glycemic response that prevents fasting hyperglycemia (Dawn Phenomenon).",
            portion = "1 medium bowl (250g)",
            icon = "🍲",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 403,
            name = "South Indian Spiced Grilled Fish with Stir-Fried Keerai",
            mealSlot = "Dinner",
            calories = 230,
            carbsGrams = 5f,
            proteinGrams = 30f,
            fiberGrams = 4.5f,
            fatGrams = 6.5f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Seafood"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Seared fish fillet in South Indian turmeric-garlic rub with local greens keeps carbohydrates minimal before bedtime.",
            portion = "1 seared fillet (180g) + 1 cup spinach poriyal",
            icon = "🐟",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 404,
            name = "Grilled Chicken with Stir-Fried Vendakkai & Greens",
            mealSlot = "Dinner",
            calories = 245,
            carbsGrams = 6f,
            proteinGrams = 32f,
            fiberGrams = 5f,
            fatGrams = 6f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "High lean poultry protein with soluble okra fiber ensures prolonged satiety and prevents midnight hypoglycemia or morning spikes.",
            portion = "150g spiced chicken breast + 1 cup okra & spinach",
            icon = "🍗",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 405,
            name = "Ragi Dosa with Tomato-Onion Chutney & Veggie Soup",
            mealSlot = "Dinner",
            calories = 210,
            carbsGrams = 28f,
            proteinGrams = 6f,
            fiberGrams = 6f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LightEvening", "Balanced"),
            healthReason = "Crisp calcium-rich finger millet dosa digests gently, avoiding overnight liver glycogen dump and morning insulin surges.",
            portion = "2 thin ragi dosas + 1 cup clear veggie soup",
            icon = "🥞",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 406,
            name = "Kollu (Horse Gram) Rasam with Steamed Veggies & 2 Egg Whites",
            mealSlot = "Dinner",
            calories = 170,
            carbsGrams = 12f,
            proteinGrams = 16f,
            fiberGrams = 4f,
            fatGrams = 2f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Eggs"),
            healthTags = listOf("SouthIndian", "HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Calorie-controlled dinner providing soothing horse gram polyphenols and clean protein for overnight muscular & glycemic repair.",
            portion = "1 large bowl rasam + steamed beans/carrots + 2 whites",
            icon = "🍲",
            isSouthIndian = true
        ),
        HealthFoodItem(
            id = 407,
            name = "Sautéed Paneer & Broccoli Stir-Fry with Garlic",
            mealSlot = "Dinner",
            calories = 240,
            carbsGrams = 8f,
            proteinGrams = 18f,
            fiberGrams = 5f,
            fatGrams = 11f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Broccoli contains sulforaphane which reduces hepatic glucose output, coupled with slow-digesting paneer casein.",
            portion = "1 medium plate (220g)",
            icon = "🥦",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 408,
            name = "Mixed Vegetable Clear Soup with Black Chana",
            mealSlot = "Dinner",
            calories = 175,
            carbsGrams = 22f,
            proteinGrams = 9f,
            fiberGrams = 7f,
            fatGrams = 2f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LightEvening", "Balanced"),
            healthReason = "Calorie-controlled, high-volume broth that stretches the stomach wall, stimulating natural satiety peptides (GLP-1).",
            portion = "1 large bowl (300ml)",
            icon = "🍲",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 409,
            name = "Moong Dal & Spinach Khichdi with Flaxseed",
            mealSlot = "Dinner",
            calories = 210,
            carbsGrams = 28f,
            proteinGrams = 9.5f,
            fiberGrams = 6.5f,
            fatGrams = 3.5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("Balanced", "LightEvening"),
            healthReason = "Balanced 2:1 lentil-to-grain ratio ensures steady, non-spiking overnight glucose release.",
            portion = "1 medium bowl (200g)",
            icon = "🍲",
            isSouthIndian = false
        ),
        HealthFoodItem(
            id = 410,
            name = "Tofu & Bok Choy Clear Noodle Soup (Shirataki)",
            mealSlot = "Dinner",
            calories = 160,
            carbsGrams = 6f,
            proteinGrams = 14f,
            fiberGrams = 5f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Soy"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Shirataki konjac noodles contain zero-calorie glucomannan fiber that stabilizes nighttime sugar without heavy carbs.",
            portion = "1 large bowl (300ml)",
            icon = "🍜",
            isSouthIndian = false
        )
    )

    /**
     * Retrieves the list of recently recommended food IDs from SharedPreferences.
     */
    fun getRecentlyRecommendedIds(context: Context): List<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECENT_IDS, "") ?: ""
        return raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }

    /**
     * Records a food ID as recently recommended to prevent daily repetition.
     */
    fun recordRecommendedId(context: Context, id: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getRecentlyRecommendedIds(context).filter { it != id }
        val updated = (listOf(id) + current).take(MAX_RECENT_HISTORY)
        prefs.edit().putString(KEY_RECENT_IDS, updated.joinToString(",")).apply()
    }

    /**
     * Selects a single daily rotating recommendation matching constraints with on-demand shuffle.
     */
    fun getDailyRecommendation(
        context: Context,
        userDietType: String? = null,
        userAllergies: List<String> = emptyList(),
        userFoodsToAvoid: List<String> = emptyList(),
        currentGlucose: Float? = null,
        targetMealSlot: String? = null,
        shuffleOffset: Int = 0
    ): HealthRecommendationResult {
        // 1. Determine active meal slot based on current hour if not explicitly set
        val slot = targetMealSlot ?: run {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 5..10 -> "Breakfast"
                in 11..15 -> "Lunch"
                in 16..18 -> "Snack"
                else -> "Dinner"
            }
        }

        // 2. Strict User Constraint Filtering
        val dietNorm = userDietType?.lowercase(Locale.ROOT) ?: ""
        val allergiesNorm = userAllergies.map { it.lowercase(Locale.ROOT).trim() }.filter { it.isNotEmpty() }
        val avoidNorm = userFoodsToAvoid.map { it.lowercase(Locale.ROOT).trim() }.filter { it.isNotEmpty() }

        var candidates = foodDataset.filter { item ->
            // Filter by Diet Type
            val matchesDiet = when {
                dietNorm.contains("vegan") -> item.dietType.equals("Vegan", ignoreCase = true)
                dietNorm.contains("veg") -> item.dietType.equals("Vegetarian", ignoreCase = true) || item.dietType.equals("Vegan", ignoreCase = true)
                dietNorm.contains("non") -> true // Non-veg user can eat all
                else -> true
            }
            if (!matchesDiet) return@filter false

            // Filter out user Allergies
            val hasAllergen = item.allergens.any { allergen ->
                allergiesNorm.any { userAllergy ->
                    allergen.lowercase(Locale.ROOT).contains(userAllergy) || userAllergy.contains(allergen.lowercase(Locale.ROOT))
                }
            }
            if (hasAllergen) return@filter false

            // Filter out Foods To Avoid
            val itemNameLower = item.name.lowercase(Locale.ROOT)
            val isAvoided = avoidNorm.any { avoidTerm ->
                itemNameLower.contains(avoidTerm)
            }
            if (isAvoided) return@filter false

            true
        }

        // Fallback to full dataset if strict filters eliminated everything
        if (candidates.isEmpty()) {
            candidates = foodDataset
        }

        // 3. Filter by Meal Slot (prefer matching meal slot)
        val slotMatches = candidates.filter { it.mealSlot.equals(slot, ignoreCase = true) }
        val slotPool = if (slotMatches.isNotEmpty()) slotMatches else candidates

        // 4. Prioritization & scoring (South Indian preference + Glucose level)
        val glucose = currentGlucose ?: 120f
        val prioritizedPool = slotPool.sortedByDescending { item ->
            var score = 100
            // Prioritize South Indian diabetic-friendly meals by default for Chennai familiarity
            if (item.isSouthIndian || item.healthTags.contains("SouthIndian")) {
                score += 50
            }
            when {
                glucose > 180f -> {
                    // Elevated glucose: Reward Low GL, HighSugarSafe, LowCarb, and High Fiber
                    if (item.glycemicLoad.equals("Low", ignoreCase = true)) score += 60
                    if (item.healthTags.contains("HighSugarSafe")) score += 40
                    if (item.healthTags.contains("LowCarb")) score += 30
                    if (item.fiberGrams >= 5f) score += 20
                    if (item.carbsGrams <= 25f) score += 20
                }
                glucose < 70f -> {
                    // Hypoglycemic risk: Reward balanced stabilizing carbs and protein
                    if (item.carbsGrams in 20f..35f) score += 60
                    if (item.proteinGrams >= 8f) score += 40
                    if (item.healthTags.contains("Balanced")) score += 20
                }
                else -> {
                    // Normal range (70 - 180): Balanced, high fiber, high satiety
                    if (item.healthTags.contains("Balanced")) score += 40
                    if (item.healthTags.contains("HighSugarSafe")) score += 20
                    if (item.glycemicLoad.equals("Low", ignoreCase = true)) score += 20
                }
            }
            score
        }

        // 5. Daily Rotation & Recent History Suppression
        val cal = Calendar.getInstance()
        val epochDay = (cal.get(Calendar.YEAR) * 365L) + cal.get(Calendar.DAY_OF_YEAR)
        val recentIds = getRecentlyRecommendedIds(context)

        // Split pool into fresh items vs recently shown items
        val freshItems = prioritizedPool.filter { it.id !in recentIds }
        val poolForSelection = if (freshItems.isNotEmpty()) freshItems else prioritizedPool

        // Deterministic daily shuffle seed
        val random = Random(epochDay + 7919L)
        val rotatedList = poolForSelection.shuffled(random)

        // 6. Apply on-demand shuffle offset ("Try Another")
        val effectiveIndex = ((shuffleOffset % rotatedList.size) + rotatedList.size) % rotatedList.size
        val chosenItem = rotatedList[effectiveIndex]

        // Record chosen item to recent history
        recordRecommendedId(context, chosenItem.id)

        // 7. Compose dynamic contextual explanation and badges
        val (reasonText, glucoseBadge) = when {
            glucose > 180f -> {
                val reason = "Tailored for your current glucose (${glucose.toInt()} mg/dL): Low glycemic load with ${chosenItem.fiberGrams.toInt()}g fiber and ${chosenItem.proteinGrams.toInt()}g protein slows carbohydrate digestion to help safely stabilize your sugar levels."
                reason to "🔴 High Glucose Safe"
            }
            glucose < 70f -> {
                val reason = "Selected for low blood sugar (${glucose.toInt()} mg/dL): Delivers ${chosenItem.carbsGrams.toInt()}g of gentle stabilizing complex carbohydrates paired with protein to safely recover normal glycemic balance."
                reason to "🟡 Glucose Stabilizer"
            }
            else -> {
                val reason = if (chosenItem.healthReason.isNotBlank()) {
                    "Optimized for your in-range glucose (${glucose.toInt()} mg/dL): ${chosenItem.healthReason}"
                } else {
                    "Nutrient-balanced pick for your healthy glucose level (${glucose.toInt()} mg/dL) with ${chosenItem.fiberGrams.toInt()}g fiber and steady macro release."
                }
                reason to "🟢 Glycemic In-Range"
            }
        }

        val rotationLabel = if (shuffleOffset == 0) {
            "📅 Today's Daily Pick"
        } else {
            "🔀 Suggestion #${effectiveIndex + 1} of ${rotatedList.size}"
        }

        return HealthRecommendationResult(
            item = chosenItem,
            matchedReason = reasonText,
            glucoseBadgeText = glucoseBadge,
            dailyRotationLabel = rotationLabel,
            totalAvailableMatches = rotatedList.size,
            currentIndex = effectiveIndex
        )
    }

    /**
     * Converts a HealthFoodItem to a full RecommendedFoodItem for UI display.
     */
    fun HealthFoodItem.toRecommendedFoodItem(glucose: Float = 120f): RecommendedFoodItem {
        val glBadge = when {
            glycemicLoad.equals("Low", ignoreCase = true) -> GlycemicDetails(
                badge = "Low GL Impact",
                color = "#2E7D32",
                bgColor = "#E8F5E9"
            )
            glycemicLoad.equals("Medium", ignoreCase = true) -> GlycemicDetails(
                badge = "Medium GL Impact",
                color = "#F57C00",
                bgColor = "#FFF3E0"
            )
            else -> GlycemicDetails(
                badge = "Gentle Carbs",
                color = "#1565C0",
                bgColor = "#E3F2FD"
            )
        }

        val dynamicHighlights = buildList {
            if (isSouthIndian) {
                add("South Indian")
            }
            add("${carbsGrams.toInt()}g Carbs")
            add("${proteinGrams.toInt()}g Protein")
            add("${fiberGrams.toInt()}g Fiber")
            val primaryTag = healthTags.firstOrNull { it != "SouthIndian" }
            if (primaryTag != null) {
                add(primaryTag)
            }
        }

        val reason = when {
            glucose > 180f -> "Low glycemic load meal ($portion) with ${fiberGrams.toInt()}g fiber and ${proteinGrams.toInt()}g protein prevents sharp glucose surges."
            glucose < 70f -> "Gentle stabilizing meal providing ${carbsGrams.toInt()}g complex carbs and steady protein to safely restore blood sugar."
            else -> if (healthReason.isNotBlank()) healthReason else "Nutrient-dense $mealSlot option providing steady glycemic control and balanced macros."
        }

        return RecommendedFoodItem(
            id = id,
            title = name,
            dishName = name,
            calories = calories.toFloat(),
            caloriesLabel = "$calories kcal",
            carbsG = carbsGrams,
            proteinG = proteinGrams,
            fatG = fatGrams,
            freeSugarG = 1f,
            fibreG = fiberGrams,
            sodiumMg = 180f,
            category = mealSlot,
            dietType = dietType,
            portion = portion,
            icon = icon,
            highlights = dynamicHighlights,
            glycemicImpact = glBadge,
            recommendationScore = if (isSouthIndian) 96 else 90,
            recommendationReason = reason
        )
    }

    /**
     * Generates a complete daily rotating meal plan (Breakfast, Lunch, Snack, Dinner)
     * with non-repeating combinations that change every single day based on calendar epoch day seed,
     * user dietary constraints (diet type, allergies, foods to avoid), and live blood glucose levels,
     * strongly prioritizing South Indian diabetic-friendly foods.
     */
    fun getDailyRotatingMealPlan(
        userDietType: String? = null,
        userAllergies: List<String> = emptyList(),
        userFoodsToAvoid: List<String> = emptyList(),
        currentGlucose: Float? = 120f,
        calendar: Calendar? = null,
        dayOffset: Int = 0,
        shuffleOffset: Int = 0
    ): DailyPlanData {
        val cal = (calendar?.clone() as? Calendar) ?: Calendar.getInstance()
        if (dayOffset != 0) {
            cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        }
        val year = cal.get(Calendar.YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val epochDay = (year * 365L) + dayOfYear

        val glucose = currentGlucose ?: 120f
        val dietNorm = userDietType?.lowercase(Locale.ROOT) ?: ""
        val allergiesNorm = userAllergies.map { it.lowercase(Locale.ROOT).trim() }.filter { it.isNotEmpty() }
        val avoidNorm = userFoodsToAvoid.map { it.lowercase(Locale.ROOT).trim() }.filter { it.isNotEmpty() }

        // Filter dataset by user constraints (Diet, Allergies, Avoid list)
        val filtered = foodDataset.filter { item ->
            val matchesDiet = when {
                dietNorm.contains("vegan") -> item.dietType.equals("Vegan", ignoreCase = true)
                dietNorm.contains("veg") -> item.dietType.equals("Vegetarian", ignoreCase = true) || item.dietType.equals("Vegan", ignoreCase = true)
                dietNorm.contains("non") -> true
                else -> true
            }
            if (!matchesDiet) return@filter false

            val hasAllergen = item.allergens.any { allergen ->
                allergiesNorm.any { userAllergy ->
                    allergen.lowercase(Locale.ROOT).contains(userAllergy) || userAllergy.contains(allergen.lowercase(Locale.ROOT))
                }
            }
            if (hasAllergen) return@filter false

            val nameLower = item.name.lowercase(Locale.ROOT)
            if (avoidNorm.any { nameLower.contains(it) }) return@filter false

            true
        }.ifEmpty { foodDataset }

        fun pickRotatingSlot(slot: String, count: Int, slotSalt: Long): List<RecommendedFoodItem> {
            val matching = filtered.filter { it.mealSlot.equals(slot, ignoreCase = true) }
                .ifEmpty { foodDataset.filter { it.mealSlot.equals(slot, ignoreCase = true) } }
                .ifEmpty { filtered }

            // Score items by current glucose suitability & South Indian preference
            val scored = matching.sortedByDescending { item ->
                var score = 100
                // Strong priority bonus for authentic South Indian diabetic dishes
                if (item.isSouthIndian || item.healthTags.contains("SouthIndian")) {
                    score += 50
                }
                when {
                    glucose > 180f -> {
                        if (item.glycemicLoad.equals("Low", ignoreCase = true)) score += 60
                        if (item.healthTags.contains("HighSugarSafe")) score += 40
                        if (item.healthTags.contains("LowCarb")) score += 30
                        if (item.fiberGrams >= 5f) score += 20
                    }
                    glucose < 70f -> {
                        if (item.carbsGrams in 20f..35f) score += 60
                        if (item.proteinGrams >= 8f) score += 40
                        if (item.healthTags.contains("Balanced")) score += 20
                    }
                    else -> {
                        if (item.healthTags.contains("Balanced")) score += 40
                        if (item.healthTags.contains("HighSugarSafe")) score += 20
                        if (item.glycemicLoad.equals("Low", ignoreCase = true)) score += 20
                    }
                }
                score
            }

            // Shuffle deterministically based on date seed + slot salt + on-demand shuffleOffset
            val slotRandom = Random((epochDay + shuffleOffset) * 31L + slotSalt)
            val candidatePool = scored.take((count * 3).coerceAtLeast(6))
            val rotated = candidatePool.shuffled(slotRandom)
            return rotated.take(count).map { it.toRecommendedFoodItem(glucose) }
        }

        val breakfast = pickRotatingSlot("Breakfast", 2, 1013L)
        val lunch = pickRotatingSlot("Lunch", 2, 2027L)
        val snack = pickRotatingSlot("Snack", 1, 3037L)
        val dinner = pickRotatingSlot("Dinner", 2, 4049L)

        return DailyPlanData(
            breakfast = breakfast,
            lunch = lunch,
            snack = snack,
            dinner = dinner
        )
    }
}
