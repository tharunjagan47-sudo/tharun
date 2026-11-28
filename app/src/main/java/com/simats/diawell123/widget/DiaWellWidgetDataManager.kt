package com.simats.diawell123.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.simats.diawell123.data.local.MedicationAlarmStorage
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.DailyPlanData
import com.simats.diawell123.data.model.DashboardData
import com.simats.diawell123.data.model.MedicationItem
import com.simats.diawell123.data.repository.AiFoodRecommendationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fast, offline-first data manager for the DiaWell Glance App Widget.
 * Persists latest glucose readings and upcoming medication reminders in SharedPreferences
 * and triggers immediate widget updates whenever health data is modified.
 */
object DiaWellWidgetDataManager {

    private const val TAG = "DiaWellWidgetData"
    private const val PREFS_NAME = "diawell_home_widget_prefs"

    // Glucose keys
    private const val KEY_GLUCOSE_VALUE = "widget_glucose_value"
    private const val KEY_GLUCOSE_UNIT = "widget_glucose_unit"
    private const val KEY_GLUCOSE_STATUS = "widget_glucose_status"
    private const val KEY_GLUCOSE_TIME = "widget_glucose_time"
    private const val KEY_HAS_GLUCOSE = "widget_has_glucose"

    // Medication keys
    private const val KEY_MED_NAME = "widget_med_name"
    private const val KEY_MED_DOSE = "widget_med_dose"
    private const val KEY_MED_TIME = "widget_med_time"
    private const val KEY_HAS_MED = "widget_has_med"
    private const val KEY_MED_ALL_CAUGHT_UP = "widget_med_all_caught_up"

    // Activity keys
    private const val KEY_ACTIVITY_TITLE = "widget_activity_title"
    private const val KEY_ACTIVITY_STATS = "widget_activity_stats"
    private const val KEY_HAS_ACTIVITY = "widget_has_activity"

    // Next Diet keys
    private const val KEY_DIET_MEAL_TYPE = "widget_diet_meal_type"
    private const val KEY_DIET_DISH_NAME = "widget_diet_dish_name"
    private const val KEY_DIET_CALORIES = "widget_diet_calories"
    private const val KEY_HAS_DIET = "widget_has_diet"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Reads current widget display data.
     * Includes automated fallback to local MedicationAlarmStorage and SessionManager
     * so newly placed widgets render actual data immediately.
     */
    fun getWidgetData(context: Context): DiaWellWidgetData {
        val prefs = getPrefs(context)

        var hasGlucose = prefs.getBoolean(KEY_HAS_GLUCOSE, false)
        var glucoseValue = prefs.getString(KEY_GLUCOSE_VALUE, "") ?: ""
        var glucoseUnit = prefs.getString(KEY_GLUCOSE_UNIT, "mg/dL") ?: "mg/dL"
        var glucoseStatus = prefs.getString(KEY_GLUCOSE_STATUS, "Normal") ?: "Normal"
        var glucoseTime = prefs.getString(KEY_GLUCOSE_TIME, "") ?: ""

        // Fallback to SessionManager user session if no recent glucose logged in widget prefs
        if (!hasGlucose || glucoseValue.isBlank()) {
            try {
                SessionManager.init(context.applicationContext)
                val userSession = SessionManager.getInstance().getUserSession()
                val currentGlucose = userSession?.currentGlucoseMgDl
                if (currentGlucose != null && currentGlucose > 0f) {
                    hasGlucose = true
                    glucoseValue = currentGlucose.toInt().toString()
                    glucoseUnit = SessionManager.getInstance().getGlucoseUnit()
                    glucoseStatus = resolveGlucoseStatus(currentGlucose, "Fasting")
                    glucoseTime = "Latest"
                }
            } catch (_: Exception) {}
        }

        var hasMed = prefs.getBoolean(KEY_HAS_MED, false)
        var medName = prefs.getString(KEY_MED_NAME, "") ?: ""
        var medDose = prefs.getString(KEY_MED_DOSE, "") ?: ""
        var medTime = prefs.getString(KEY_MED_TIME, "") ?: ""
        var isCaughtUp = prefs.getBoolean(KEY_MED_ALL_CAUGHT_UP, false)

        // Fallback to active alarms stored in MedicationAlarmStorage
        if (!hasMed || medName.isBlank()) {
            val upcomingAlarm = findNextUpcomingAlarm(context)
            if (upcomingAlarm != null) {
                hasMed = true
                medName = upcomingAlarm.first
                medDose = upcomingAlarm.second
                medTime = upcomingAlarm.third
                isCaughtUp = false
            } else if (!isCaughtUp) {
                isCaughtUp = true
            }
        }

        return DiaWellWidgetData(
            glucoseValue = glucoseValue,
            glucoseUnit = glucoseUnit,
            glucoseStatus = glucoseStatus,
            glucoseTime = glucoseTime,
            hasGlucoseReading = hasGlucose,
            nextMedName = medName,
            nextMedDose = medDose,
            nextMedTime = medTime,
            hasUpcomingMed = hasMed,
            isMedAllCaughtUp = isCaughtUp,
            activityTitle = prefs.getString(KEY_ACTIVITY_TITLE, "")?.ifBlank { "20 min Walk" } ?: "20 min Walk",
            activityStats = prefs.getString(KEY_ACTIVITY_STATS, "")?.ifBlank { "Tap to track" } ?: "Tap to track",
            hasActivityToday = prefs.getBoolean(KEY_HAS_ACTIVITY, false),
            nextDietMealType = resolveDietFallback(context, prefs).first,
            nextDietDishName = resolveDietFallback(context, prefs).second,
            nextDietCalories = resolveDietFallback(context, prefs).third,
            hasNextDiet = true
        )
    }

    /**
     * Saves a newly logged glucose reading and triggers widget refresh.
     */
    fun saveGlucoseReading(
        context: Context,
        value: Float,
        readingType: String = "Fasting",
        dateStr: String = "",
        timeStr: String = ""
    ) {
        try {
            val status = resolveGlucoseStatus(value, readingType)
            val timeLabel = formatRelativeTime(dateStr, timeStr)
            val unit = try {
                SessionManager.init(context.applicationContext)
                SessionManager.getInstance().getGlucoseUnit()
            } catch (_: Exception) { "mg/dL" }

            getPrefs(context).edit()
                .putBoolean(KEY_HAS_GLUCOSE, true)
                .putString(KEY_GLUCOSE_VALUE, value.toInt().toString())
                .putString(KEY_GLUCOSE_UNIT, unit)
                .putString(KEY_GLUCOSE_STATUS, status)
                .putString(KEY_GLUCOSE_TIME, timeLabel)
                .apply()

            Log.d(TAG, "Saved glucose reading for widget: $value $unit, status=$status, time=$timeLabel")
            updateWidget(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving glucose for widget: ${e.message}", e)
        }
    }

    /**
     * Saves glucose reading from Home Dashboard metric card.
     */
    fun saveGlucoseFromMetric(
        context: Context,
        valueStr: String,
        statusText: String,
        subtitle: String
    ) {
        try {
            if (valueStr.isBlank()) return
            val cleanValue = valueStr.replace("mg/dL", "", ignoreCase = true).trim()
            val cleanStatus = statusText.ifBlank { "Normal" }

            getPrefs(context).edit()
                .putBoolean(KEY_HAS_GLUCOSE, true)
                .putString(KEY_GLUCOSE_VALUE, cleanValue)
                .putString(KEY_GLUCOSE_UNIT, "mg/dL")
                .putString(KEY_GLUCOSE_STATUS, cleanStatus)
                .putString(KEY_GLUCOSE_TIME, subtitle)
                .apply()

            updateWidget(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving glucose metric for widget: ${e.message}", e)
        }
    }

    /**
     * Saves upcoming medication details and triggers widget refresh.
     */
    fun saveNextMedication(
        context: Context,
        name: String,
        dose: String = "",
        time: String = ""
    ) {
        try {
            getPrefs(context).edit()
                .putBoolean(KEY_HAS_MED, true)
                .putBoolean(KEY_MED_ALL_CAUGHT_UP, false)
                .putString(KEY_MED_NAME, name)
                .putString(KEY_MED_DOSE, dose)
                .putString(KEY_MED_TIME, time)
                .apply()

            Log.d(TAG, "Saved next medication for widget: $name ($dose) at $time")
            updateWidget(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving next medication for widget: ${e.message}", e)
        }
    }

    /**
     * Updates widget medication state from full medication list.
     * Automatically identifies the next upcoming dose or marks all caught up.
     */
    fun saveMedsList(context: Context, medications: List<MedicationItem>) {
        try {
            val upcoming = medications.firstOrNull {
                it.status.equals("Upcoming", ignoreCase = true) ||
                it.status.equals("Scheduled", ignoreCase = true)
            }

            if (upcoming != null) {
                saveNextMedication(
                    context = context,
                    name = upcoming.name,
                    dose = upcoming.dose,
                    time = upcoming.reminderTime
                )
            } else {
                // If there were medications and none are upcoming, all doses today are caught up
                val hasAnyMed = medications.isNotEmpty()
                getPrefs(context).edit()
                    .putBoolean(KEY_HAS_MED, false)
                    .putBoolean(KEY_MED_ALL_CAUGHT_UP, hasAnyMed)
                    .apply()

                updateWidget(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving meds list for widget: ${e.message}", e)
        }
    }

    /**
     * Refreshes medication state from MedicationAlarmStorage when status changes.
     */
    fun refreshFromAlarmStorage(context: Context) {
        try {
            val upcomingAlarm = findNextUpcomingAlarm(context)
            if (upcomingAlarm != null) {
                saveNextMedication(
                    context = context,
                    name = upcomingAlarm.first,
                    dose = upcomingAlarm.second,
                    time = upcomingAlarm.third
                )
            } else {
                getPrefs(context).edit()
                    .putBoolean(KEY_HAS_MED, false)
                    .putBoolean(KEY_MED_ALL_CAUGHT_UP, true)
                    .apply()
                updateWidget(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing from alarm storage: ${e.message}", e)
        }
    }

    /**
     * Syncs both glucose and medication state from Home Dashboard data.
     */
    fun syncFromDashboard(context: Context, data: DashboardData) {
        try {
            data.metrics?.glucose?.let { g ->
                saveGlucoseFromMetric(
                    context = context,
                    valueStr = g.value,
                    statusText = g.statusText,
                    subtitle = g.subtitle
                )
            }
            refreshFromAlarmStorage(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from dashboard: ${e.message}", e)
        }
    }

    /**
     * Dispatches an asynchronous widget refresh via Glance.
     */
    fun updateWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DiaWellWidget().updateAll(context.applicationContext)
                Log.d(TAG, "DiaWellWidget updateAll executed successfully")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update widget: ${e.message}")
            }
        }
    }

    private fun resolveGlucoseStatus(value: Float, readingType: String): String {
        return when {
            value < 70f -> "Low"
            readingType.contains("Post", ignoreCase = true) && value > 180f -> "High"
            !readingType.contains("Post", ignoreCase = true) && value > 140f -> "High"
            else -> "Normal"
        }
    }

    private fun formatRelativeTime(dateStr: String, timeStr: String): String {
        val nowTime = if (timeStr.isNotBlank()) timeStr else SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return if (dateStr.isBlank() || dateStr.equals(todayDate, ignoreCase = true) || dateStr.contains("Today", ignoreCase = true)) {
            "Today, $nowTime"
        } else {
            nowTime
        }
    }

    private fun findNextUpcomingAlarm(context: Context): Triple<String, String, String>? {
        return try {
            val reminders = MedicationAlarmStorage.getAllActiveDailyReminders(context)
            if (reminders.isEmpty()) return null

            val nowCal = Calendar.getInstance()
            val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

            // Look for next reminder today
            val nextToday = reminders
                .filter { (it.hour * 60 + it.minute) >= currentMinutes }
                .minByOrNull { it.hour * 60 + it.minute }

            val target = nextToday ?: reminders.minByOrNull { it.hour * 60 + it.minute }
            if (target != null) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, target.hour)
                    set(Calendar.MINUTE, target.minute)
                }
                val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
                Triple(target.medName, target.dosage, formattedTime)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Saves a newly completed workout / activity and updates the widget.
     */
    fun saveActivity(
        context: Context,
        title: String,
        durationMins: Int,
        caloriesKcal: Int
    ) {
        try {
            val stats = "$durationMins min • $caloriesKcal kcal"
            getPrefs(context).edit()
                .putBoolean(KEY_HAS_ACTIVITY, true)
                .putString(KEY_ACTIVITY_TITLE, title.ifBlank { "Workout" })
                .putString(KEY_ACTIVITY_STATS, stats)
                .apply()

            Log.d(TAG, "Saved activity for widget: $title ($stats)")
            updateWidget(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving activity for widget: ${e.message}", e)
        }
    }

    /**
     * Updates activity summary from ActivityHistorySummary.
     */
    fun saveActivitySummary(
        context: Context,
        totalWorkouts: Int,
        totalCalories: Int,
        latestTitle: String? = null
    ) {
        try {
            if (totalWorkouts > 0) {
                val title = latestTitle?.takeIf { it.isNotBlank() } ?: "$totalWorkouts Workouts"
                val stats = "$totalCalories kcal burned"
                getPrefs(context).edit()
                    .putBoolean(KEY_HAS_ACTIVITY, true)
                    .putString(KEY_ACTIVITY_TITLE, title)
                    .putString(KEY_ACTIVITY_STATS, stats)
                    .apply()
                updateWidget(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving activity summary for widget: ${e.message}", e)
        }
    }

    /**
     * Picks the next upcoming meal from DailyPlanData based on time of day and saves it to the widget.
     */
    fun saveNextDietFromPlan(context: Context, dailyPlan: DailyPlanData?) {
        try {
            if (dailyPlan == null) return
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val (mealLabel, foodItem) = when {
                hour < 10 -> "Breakfast" to dailyPlan.breakfast.firstOrNull()
                hour in 10..14 -> "Lunch" to dailyPlan.lunch.firstOrNull()
                hour in 15..17 -> "Snack" to dailyPlan.snack.firstOrNull()
                else -> "Dinner" to dailyPlan.dinner.firstOrNull()
            }
            val dishName = (foodItem?.dishName ?: foodItem?.title)?.takeIf { it.isNotBlank() }
                ?: return
            val calLabel = foodItem?.caloriesLabel?.ifBlank {
                val calVal = foodItem.calories?.toInt() ?: 0
                if (calVal > 0) "$calVal kcal" else "Low GI"
            } ?: "Low GI"

            getPrefs(context).edit()
                .putBoolean(KEY_HAS_DIET, true)
                .putString(KEY_DIET_MEAL_TYPE, mealLabel)
                .putString(KEY_DIET_DISH_NAME, dishName)
                .putString(KEY_DIET_CALORIES, calLabel)
                .apply()

            Log.d(TAG, "Saved next diet for widget: $mealLabel - $dishName ($calLabel)")
            updateWidget(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving next diet for widget: ${e.message}", e)
        }
    }

    private fun resolveDietFallback(
        context: Context,
        prefs: SharedPreferences
    ): Triple<String, String, String> {
        val savedHas = prefs.getBoolean(KEY_HAS_DIET, false)
        val savedMeal = prefs.getString(KEY_DIET_MEAL_TYPE, "") ?: ""
        val savedDish = prefs.getString(KEY_DIET_DISH_NAME, "") ?: ""
        val savedCal = prefs.getString(KEY_DIET_CALORIES, "") ?: ""

        if (savedHas && savedDish.isNotBlank()) {
            return Triple(savedMeal.ifBlank { "Next Meal" }, savedDish, savedCal.ifBlank { "Low GI" })
        }

        // Offline personalized fallback from AiFoodRecommendationEngine
        return try {
            SessionManager.init(context.applicationContext)
            val session = SessionManager.getInstance()
            val foodPrefs = session.getCachedFoodPreferences()
            val userSession = session.getUserSession()
            val plan = AiFoodRecommendationEngine.getDailyRotatingMealPlan(
                userDietType = foodPrefs?.preferredDiet ?: "Vegetarian",
                userAllergies = foodPrefs?.allergies ?: emptyList(),
                userFoodsToAvoid = foodPrefs?.foodsToAvoid ?: emptyList(),
                currentGlucose = userSession?.currentGlucoseMgDl,
                calendar = Calendar.getInstance(),
                shuffleOffset = 0
            )
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val (mealLabel, item) = when {
                hour < 10 -> "Breakfast" to plan.breakfast.firstOrNull()
                hour in 10..14 -> "Lunch" to plan.lunch.firstOrNull()
                hour in 15..17 -> "Snack" to plan.snack.firstOrNull()
                else -> "Dinner" to plan.dinner.firstOrNull()
            }
            val dish = (item?.dishName ?: item?.title)?.ifBlank { "Balanced Low-GI Meal" } ?: "Balanced Low-GI Meal"
            val cal = item?.caloriesLabel?.ifBlank {
                val c = item.calories?.toInt() ?: 0
                if (c > 0) "$c kcal" else "Low GI"
            } ?: "Low GI"
            Triple(mealLabel, dish, cal)
        } catch (_: Exception) {
            Triple("Next Meal", "Balanced Low-GI Meal", "Low GI")
        }
    }
}
