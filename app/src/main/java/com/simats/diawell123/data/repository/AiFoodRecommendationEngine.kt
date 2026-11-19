package com.simats.diawell123.data.repository

import android.content.Context
import com.simats.diawell123.data.model.HealthFoodItem
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
 * while ensuring daily variety through deterministic epoch-day seeding and recent history suppression.
 */
object AiFoodRecommendationEngine {

    private const val PREFS_NAME = "prefs_ai_food_engine"
    private const val KEY_RECENT_IDS = "recently_recommended_food_ids"
    private const val MAX_RECENT_HISTORY = 10

    /**
     * Comprehensive diabetes-friendly health food dataset across all meal slots and diet preferences.
     */
    val foodDataset: List<HealthFoodItem> = listOf(
        // ==================== BREAKFAST ====================
        HealthFoodItem(
            id = 101,
            name = "Ragi Vegetable Idli with Mint Chutney",
            mealSlot = "Breakfast",
            calories = 180,
            carbsGrams = 28f,
            proteinGrams = 6f,
            fiberGrams = 5.5f,
            fatGrams = 3f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Finger millet provides slow-digesting polyphenols and complex carbs that prevent morning glucose spikes.",
            portion = "2 idlis with 2 tbsp chutney",
            icon = "🥣"
        ),
        HealthFoodItem(
            id = 102,
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
            icon = "🥞"
        ),
        HealthFoodItem(
            id = 103,
            name = "Steel-Cut Oats Upma with Mixed Veggies",
            mealSlot = "Breakfast",
            calories = 210,
            carbsGrams = 32f,
            proteinGrams = 7f,
            fiberGrams = 6f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Gluten"),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Beta-glucan soluble fiber forms a natural gel in the gut, substantially flattening post-meal glucose absorption.",
            portion = "1 medium bowl",
            icon = "🍲"
        ),
        HealthFoodItem(
            id = 104,
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
            icon = "🍳"
        ),
        HealthFoodItem(
            id = 105,
            name = "Tofu & Bell Pepper Bhurji with Avocado",
            mealSlot = "Breakfast",
            calories = 205,
            carbsGrams = 10f,
            proteinGrams = 15f,
            fiberGrams = 5f,
            fatGrams = 9f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Soy"),
            healthTags = listOf("HighSugarSafe", "LowCarb"),
            healthReason = "Soy isoflavones and heart-healthy monounsaturated fats optimize cellular glucose uptake.",
            portion = "1 bowl with 2 slices avocado",
            icon = "🥗"
        ),
        HealthFoodItem(
            id = 106,
            name = "Methi (Fenugreek) Thepla with Low-Fat Curd",
            mealSlot = "Breakfast",
            calories = 210,
            carbsGrams = 26f,
            proteinGrams = 8f,
            fiberGrams = 5f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Gluten", "Dairy"),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Fenugreek galactomannan delays carbohydrate absorption and stimulates basal insulin activity.",
            portion = "2 thin theplas + 1/2 cup curd",
            icon = "🫓"
        ),
        HealthFoodItem(
            id = 107,
            name = "Boiled Eggs with Sautéed Mushrooms & Tomatoes",
            mealSlot = "Breakfast",
            calories = 180,
            carbsGrams = 6f,
            proteinGrams = 14f,
            fiberGrams = 2.5f,
            fatGrams = 10f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Eggs"),
            healthTags = listOf("HighSugarSafe", "LowCarb"),
            healthReason = "Near-zero glycemic impact with rich choline and antioxidant ergothioneine for steady morning energy.",
            portion = "2 whole eggs + 1/2 cup veggies",
            icon = "🥚"
        ),
        HealthFoodItem(
            id = 108,
            name = "Chia Seed & Almond Milk Pudding with Cinnamon",
            mealSlot = "Breakfast",
            calories = 170,
            carbsGrams = 12f,
            proteinGrams = 6f,
            fiberGrams = 8f,
            fatGrams = 9f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Nuts"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "Balanced"),
            healthReason = "Mucilaginous soluble fiber paired with cinnamaldehyde naturally promotes insulin receptor responsiveness.",
            portion = "1 jar (150g)",
            icon = "🍨"
        ),
        HealthFoodItem(
            id = 109,
            name = "Foxtail Millet Pongal with Low-Fat Chutney",
            mealSlot = "Breakfast",
            calories = 230,
            carbsGrams = 34f,
            proteinGrams = 7f,
            fiberGrams = 5f,
            fatGrams = 5f,
            glycemicLoad = "Medium",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("Balanced"),
            healthReason = "Traditional anti-inflammatory millet rich in resistant starch for smooth, prolonged glucose release.",
            portion = "1 cup cooked pongal",
            icon = "🥣"
        ),
        HealthFoodItem(
            id = 110,
            name = "Soya Chunks & Vegetable Poha",
            mealSlot = "Breakfast",
            calories = 225,
            carbsGrams = 28f,
            proteinGrams = 12f,
            fiberGrams = 4.5f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Soy"),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "High-density plant protein balances the light glycemic index of flattened rice.",
            portion = "1 medium plate",
            icon = "🍚"
        ),

        // ==================== LUNCH ====================
        HealthFoodItem(
            id = 201,
            name = "Brown Rice & Palak Dal with Cucumber Salad",
            mealSlot = "Lunch",
            calories = 320,
            carbsGrams = 44f,
            proteinGrams = 14f,
            fiberGrams = 8.5f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("Balanced", "HighSugarSafe"),
            healthReason = "Folate-rich spinach and whole lentils blunt carbohydrate breakdown and support hepatic glucose regulation.",
            portion = "1 cup brown rice + 1 cup dal",
            icon = "🍛"
        ),
        HealthFoodItem(
            id = 202,
            name = "Grilled Herb Chicken Breast with Steamed Broccoli",
            mealSlot = "Lunch",
            calories = 340,
            carbsGrams = 16f,
            proteinGrams = 38f,
            fiberGrams = 6f,
            fatGrams = 8f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Lean poultry protein maximizes post-meal thermogenesis without placing any strain on insulin reserves.",
            portion = "150g chicken + 1 cup broccoli",
            icon = "🍗"
        ),
        HealthFoodItem(
            id = 203,
            name = "Paneer Tikka with Grilled Bell Peppers & Roti",
            mealSlot = "Lunch",
            calories = 330,
            carbsGrams = 26f,
            proteinGrams = 18f,
            fiberGrams = 6f,
            fatGrams = 12f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy", "Gluten"),
            healthTags = listOf("LowCarb", "Balanced"),
            healthReason = "Slow-digesting casein protein and fiber prevent rapid gastric emptying and postprandial glycemic spikes.",
            portion = "5 paneer cubes + 1 roti",
            icon = "🍢"
        ),
        HealthFoodItem(
            id = 204,
            name = "Soya Chunks & Vegetable Biryani with Flax Raita",
            mealSlot = "Lunch",
            calories = 310,
            carbsGrams = 38f,
            proteinGrams = 17f,
            fiberGrams = 9f,
            fatGrams = 6f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Soy", "Dairy"),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Omega-3 alpha-linolenic acid in flaxseed reduces systemic insulin resistance alongside wholesome soy protein.",
            portion = "1 plate biryani + 1/2 cup raita",
            icon = "🥘"
        ),
        HealthFoodItem(
            id = 205,
            name = "Pan-Seared Salmon Fillet with Asparagus & Cauli-Mash",
            mealSlot = "Lunch",
            calories = 360,
            carbsGrams = 8f,
            proteinGrams = 32f,
            fiberGrams = 4.5f,
            fatGrams = 18f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Seafood"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Marine EPA & DHA fatty acids reduce vascular inflammation and stabilize cellular insulin signaling.",
            portion = "1 fillet (140g) + 1 cup greens",
            icon = "🐟"
        ),
        HealthFoodItem(
            id = 206,
            name = "Horse Gram (Kollu) Sambar with Red Rice & Cabbage",
            mealSlot = "Lunch",
            calories = 305,
            carbsGrams = 42f,
            proteinGrams = 13f,
            fiberGrams = 10f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Phenolic compounds in horse gram significantly reduce intestinal alpha-glucosidase activity.",
            portion = "3/4 cup red rice + 1 cup sambar",
            icon = "🍲"
        ),
        HealthFoodItem(
            id = 207,
            name = "Chickpea & Grilled Mediterranean Salad with Olive Oil",
            mealSlot = "Lunch",
            calories = 290,
            carbsGrams = 32f,
            proteinGrams = 12f,
            fiberGrams = 9f,
            fatGrams = 10f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("Balanced", "HighSugarSafe"),
            healthReason = "Extra virgin olive oil oleic acid paired with legume fiber maintains flat glycemic curves.",
            portion = "1 large salad bowl",
            icon = "🥗"
        ),
        HealthFoodItem(
            id = 208,
            name = "Egg Curry (2 Boiled Eggs) with Millet Roti",
            mealSlot = "Lunch",
            calories = 315,
            carbsGrams = 22f,
            proteinGrams = 16f,
            fiberGrams = 6f,
            fatGrams = 13f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Eggs"),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "High satiety index with zero sugar spikes from refined wheat or high-starch fillers.",
            portion = "2 eggs in gravy + 1 millet roti",
            icon = "🍛"
        ),
        HealthFoodItem(
            id = 209,
            name = "Rajma (Kidney Beans) Masala with Cauliflower Rice",
            mealSlot = "Lunch",
            calories = 270,
            carbsGrams = 28f,
            proteinGrams = 14f,
            fiberGrams = 11f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LowCarb"),
            healthReason = "Very high soluble fiber and resistant starch nourish gut microbiota that produce insulin-sensitizing short-chain fatty acids.",
            portion = "1 cup rajma + 1 cup cauli-rice",
            icon = "🫘"
        ),
        HealthFoodItem(
            id = 210,
            name = "Grilled Tofu Steak with Mushroom Pepper Sauce",
            mealSlot = "Lunch",
            calories = 280,
            carbsGrams = 12f,
            proteinGrams = 22f,
            fiberGrams = 6f,
            fatGrams = 12f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Soy"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Minimal net carbohydrates and rich micronutrients optimal for postprandial glucose recovery.",
            portion = "150g tofu + 1 cup stir-fry",
            icon = "🥩"
        ),

        // ==================== SNACKS ====================
        HealthFoodItem(
            id = 301,
            name = "Roasted Chana (Bengal Gram) with Chaat Masala",
            mealSlot = "Snack",
            calories = 130,
            carbsGrams = 18f,
            proteinGrams = 7f,
            fiberGrams = 5f,
            fatGrams = 3f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Crunchy legume protein with very slow digestion prevents late-afternoon energy crashes.",
            portion = "1/2 cup (40g)",
            icon = "🥜"
        ),
        HealthFoodItem(
            id = 302,
            name = "Greek Yogurt with Toasted Walnuts & Cinnamon",
            mealSlot = "Snack",
            calories = 150,
            carbsGrams = 8f,
            proteinGrams = 12f,
            fiberGrams = 2f,
            fatGrams = 7f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy", "Nuts"),
            healthTags = listOf("HighSugarSafe", "LowCarb"),
            healthReason = "Probiotics support microbiome health while dairy protein dampens hunger hormones.",
            portion = "1 cup yogurt + 4 walnut halves",
            icon = "🥣"
        ),
        HealthFoodItem(
            id = 303,
            name = "Sprouted Kala Chana Sundal",
            mealSlot = "Snack",
            calories = 140,
            carbsGrams = 20f,
            proteinGrams = 8f,
            fiberGrams = 6f,
            fatGrams = 3f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Sprouting increases bioavailability of enzymes and vitamins while further lowering glycemic impact.",
            portion = "3/4 cup sundal",
            icon = "🥗"
        ),
        HealthFoodItem(
            id = 304,
            name = "Boiled Egg White Chaat with Onion & Lemon",
            mealSlot = "Snack",
            calories = 90,
            carbsGrams = 3f,
            proteinGrams = 12f,
            fiberGrams = 1f,
            fatGrams = 1f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Eggs"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Pure bioavailable protein with zero carbohydrate impact to curb hunger before dinner.",
            portion = "3 egg whites diced with spices",
            icon = "🥚"
        ),
        HealthFoodItem(
            id = 305,
            name = "Mixed Roasted Seeds (Pumpkin, Sunflower, Flax)",
            mealSlot = "Snack",
            calories = 145,
            carbsGrams = 6f,
            proteinGrams = 6f,
            fiberGrams = 4f,
            fatGrams = 10f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LowCarb"),
            healthReason = "Magnesium-dense seeds help optimize insulin action and cellular glucose metabolism.",
            portion = "2 tablespoons (25g)",
            icon = "🌻"
        ),
        HealthFoodItem(
            id = 306,
            name = "Cucumber & Carrot Slices with Roasted Garlic Hummus",
            mealSlot = "Snack",
            calories = 120,
            carbsGrams = 14f,
            proteinGrams = 4f,
            fiberGrams = 4.5f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Hydrating, alkalizing raw vegetables with chickpea tahini for sustained micronutrient delivery.",
            portion = "1 cup crudites + 2 tbsp hummus",
            icon = "🥕"
        ),
        HealthFoodItem(
            id = 307,
            name = "Sautéed Edamame with Himalayan Pink Salt",
            mealSlot = "Snack",
            calories = 135,
            carbsGrams = 9f,
            proteinGrams = 11f,
            fiberGrams = 5f,
            fatGrams = 5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Soy"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "PostWorkout"),
            healthReason = "Whole green soybeans deliver complete plant protein and natural plant sterols.",
            portion = "1 cup in pods",
            icon = "🫛"
        ),
        HealthFoodItem(
            id = 308,
            name = "Fresh Guava Slices with Black Salt",
            mealSlot = "Snack",
            calories = 70,
            carbsGrams = 14f,
            proteinGrams = 2f,
            fiberGrams = 5.5f,
            fatGrams = 0.5f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "Balanced"),
            healthReason = "Very high in vitamin C and insoluble skin pectin that slows sugar absorption compared to tropical fruits.",
            portion = "1 medium guava sliced",
            icon = "🍈"
        ),
        HealthFoodItem(
            id = 309,
            name = "Masala Buttermilk (Chaas) with Ginger & Curry Leaves",
            mealSlot = "Snack",
            calories = 60,
            carbsGrams = 5f,
            proteinGrams = 3f,
            fiberGrams = 1f,
            fatGrams = 1.5f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("HighSugarSafe", "LightEvening"),
            healthReason = "Hydrating probiotic electrolyte drink that aids digestive transit with negligible carbs.",
            portion = "1 tall glass (250ml)",
            icon = "🥛"
        ),
        HealthFoodItem(
            id = 310,
            name = "Steamed Methi Muthia (Non-Fried) with Green Chutney",
            mealSlot = "Snack",
            calories = 125,
            carbsGrams = 18f,
            proteinGrams = 4f,
            fiberGrams = 4f,
            fatGrams = 3f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Gluten"),
            healthTags = listOf("Balanced"),
            healthReason = "Bitter fenugreek greens provide natural trigonelline, supporting healthy lipid and glycemic profiles.",
            portion = "3 steamed pieces",
            icon = "🥟"
        ),

        // ==================== DINNER ====================
        HealthFoodItem(
            id = 401,
            name = "Bottle Gourd (Lauki) & Moong Dal Soup",
            mealSlot = "Dinner",
            calories = 160,
            carbsGrams = 18f,
            proteinGrams = 8f,
            fiberGrams = 5f,
            fatGrams = 3f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LightEvening"),
            healthReason = "Ultra-light hydration and gentle digestion prevent the nocturnal glucose dawn phenomenon.",
            portion = "1 large soup bowl",
            icon = "🥣"
        ),
        HealthFoodItem(
            id = 402,
            name = "Grilled Lemon Pepper Fish with French Beans",
            mealSlot = "Dinner",
            calories = 260,
            carbsGrams = 6f,
            proteinGrams = 28f,
            fiberGrams = 4f,
            fatGrams = 10f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Seafood"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "High-protein low-carb evening plate prevents overnight glycemic surges and restless sleep.",
            portion = "140g fish + 1 cup beans",
            icon = "🐟"
        ),
        HealthFoodItem(
            id = 403,
            name = "Sautéed Paneer & Broccoli Stir-Fry with Garlic",
            mealSlot = "Dinner",
            calories = 250,
            carbsGrams = 10f,
            proteinGrams = 16f,
            fiberGrams = 5f,
            fatGrams = 14f,
            glycemicLoad = "Low",
            dietType = "Vegetarian",
            allergens = listOf("Dairy"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Slow-release casein protein maintains steady blood sugar throughout the 8-hour fasting window.",
            portion = "1 plate (150g)",
            icon = "🥦"
        ),
        HealthFoodItem(
            id = 404,
            name = "Mixed Vegetable Clear Soup with Black Chana",
            mealSlot = "Dinner",
            calories = 180,
            carbsGrams = 22f,
            proteinGrams = 9f,
            fiberGrams = 7f,
            fatGrams = 3f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LightEvening"),
            healthReason = "Warm antioxidant-rich broth with whole legume fiber quenches late-night cravings.",
            portion = "1.5 cups soup",
            icon = "🍲"
        ),
        HealthFoodItem(
            id = 405,
            name = "Grilled Chicken & Spinach Wrap in Lettuce Leaves",
            mealSlot = "Dinner",
            calories = 230,
            carbsGrams = 5f,
            proteinGrams = 26f,
            fiberGrams = 3f,
            fatGrams = 8f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Replaces high-carb flatbreads with crisp lettuce leaves to keep evening insulin demands minimal.",
            portion = "2 lettuce wraps",
            icon = "🥬"
        ),
        HealthFoodItem(
            id = 406,
            name = "Ragi Dosa with Tomato-Onion Chutney",
            mealSlot = "Dinner",
            calories = 190,
            carbsGrams = 30f,
            proteinGrams = 5f,
            fiberGrams = 4f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("Balanced", "LightEvening"),
            healthReason = "Calcium and iron-rich finger millet fermented for low glycemic index and gentle nighttime transit.",
            portion = "1 thin dosa + 2 tbsp chutney",
            icon = "🥞"
        ),
        HealthFoodItem(
            id = 407,
            name = "Tofu & Bok Choy Clear Noodle Soup (Shirataki)",
            mealSlot = "Dinner",
            calories = 150,
            carbsGrams = 8f,
            proteinGrams = 14f,
            fiberGrams = 6f,
            fatGrams = 6f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = listOf("Soy"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Glucomannan konjac noodles provide zero-glycemic volume with comforting warm amino-acid broth.",
            portion = "1 large ramen bowl",
            icon = "🍜"
        ),
        HealthFoodItem(
            id = 408,
            name = "Baked Zucchini Boats with Spiced Moong Dal",
            mealSlot = "Dinner",
            calories = 175,
            carbsGrams = 16f,
            proteinGrams = 9f,
            fiberGrams = 5f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Low calorie density with generous potassium to support healthy nocturnal blood pressure.",
            portion = "2 stuffed zucchini halves",
            icon = "🥒"
        ),
        HealthFoodItem(
            id = 409,
            name = "Sautéed Mushroom & Asparagus with Scrambled Whites",
            mealSlot = "Dinner",
            calories = 165,
            carbsGrams = 7f,
            proteinGrams = 18f,
            fiberGrams = 3.5f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Non-Vegetarian",
            allergens = listOf("Eggs"),
            healthTags = listOf("HighSugarSafe", "LowCarb", "LightEvening"),
            healthReason = "Lean protein with minimal digestive load supports deep restorative sleep and pancreatic rest.",
            portion = "1 plate (200g)",
            icon = "🍳"
        ),
        HealthFoodItem(
            id = 410,
            name = "Moong Dal & Spinach Khichdi with Flaxseed",
            mealSlot = "Dinner",
            calories = 220,
            carbsGrams = 32f,
            proteinGrams = 10f,
            fiberGrams = 6f,
            fatGrams = 4f,
            glycemicLoad = "Low",
            dietType = "Vegan",
            allergens = emptyList(),
            healthTags = listOf("Balanced", "LightEvening"),
            healthReason = "Balanced 2:1 lentil-to-grain ratio ensures steady, non-spiking overnight glucose release.",
            portion = "1 medium bowl (200g)",
            icon = "🍲"
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
     * Determines current meal slot from system clock if no category filter is applied.
     */
    fun resolveCurrentMealSlot(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> "Breakfast"
            in 11..15 -> "Lunch"
            in 16..18 -> "Snack"
            else -> "Dinner"
        }
    }

    /**
     * Computes the personalized daily rotating non-repeating food recommendation.
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
        // 1. Normalize user constraints
        val allergiesNorm = userAllergies.map { it.trim().lowercase(Locale.ROOT) }.filter { it.isNotEmpty() }
        val avoidNorm = userFoodsToAvoid.map { it.trim().lowercase(Locale.ROOT) }.filter { it.isNotEmpty() }
        val dietNorm = userDietType?.trim()?.lowercase(Locale.ROOT) ?: ""

        val slot = if (!targetMealSlot.isNullOrBlank() && !targetMealSlot.equals("All", ignoreCase = true)) {
            targetMealSlot
        } else {
            resolveCurrentMealSlot()
        }

        // 2. Filter dataset by hard constraints
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

        // 4. Glucose-based prioritization & scoring
        val glucose = currentGlucose ?: 120f
        val prioritizedPool = slotPool.sortedByDescending { item ->
            var score = 100
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
}
