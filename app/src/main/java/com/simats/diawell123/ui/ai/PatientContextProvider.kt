package com.simats.diawell123.ui.ai

import android.content.Context
import com.simats.diawell123.data.local.HealthMetricsStore
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.ChatPatientContext
import com.simats.diawell123.data.repository.ActivityRepository
import com.simats.diawell123.data.repository.ActivityRepositoryImpl
import com.simats.diawell123.data.repository.DietRepository
import com.simats.diawell123.data.repository.DietRepositoryImpl
import com.simats.diawell123.data.repository.GlucoseRepository
import com.simats.diawell123.data.repository.GlucoseRepositoryImpl
import com.simats.diawell123.data.repository.MedsRepository
import com.simats.diawell123.data.repository.MedsRepositoryImpl
import com.simats.diawell123.ui.profile.ProfileState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Aggregates live patient health metrics, medications, nutrition, and food preferences
 * into a single structured ChatPatientContext for Dr. Jarvis Personal Health RAG.
 */
object PatientContextProvider {

    suspend fun getLivePatientContext(
        context: Context?,
        glucoseRepo: GlucoseRepository = GlucoseRepositoryImpl(),
        medsRepo: MedsRepository = MedsRepositoryImpl(),
        dietRepo: DietRepository = DietRepositoryImpl(),
        activityRepo: ActivityRepository = ActivityRepositoryImpl()
    ): ChatPatientContext = coroutineScope {
        val sessionManager = try {
            SessionManager.getInstance()
        } catch (_: Exception) {
            context?.let { SessionManager.init(it.applicationContext) }
        }

        val name = sessionManager?.getFullName()?.takeIf { it.isNotBlank() }
            ?: ProfileState.userName.takeIf { it.isNotBlank() } ?: "User"

        // 1. Local water intake from HealthMetricsStore
        val waterStr = context?.let {
            val store = HealthMetricsStore(it.applicationContext)
            val glasses = store.getWaterGlasses()
            val ml = (store.getWaterLiters() * 1000).toInt()
            "$glasses glasses ($ml ml)"
        } ?: "0 glasses (0 ml)"

        // 2. Food preferences
        val foodPrefs = sessionManager?.getCachedFoodPreferences()
        val foodPrefsStr = foodPrefs?.let {
            val diet = it.preferredDiet.ifBlank { "Balanced" }
            val allergies = if (it.allergies.isNotEmpty()) it.allergies.joinToString(", ") else "None"
            val avoid = if (it.foodsToAvoid.isNotEmpty()) it.foodsToAvoid.joinToString(", ") else "None"
            "Diet: $diet | Allergies: $allergies | Avoid: $avoid"
        } ?: "Diet: Balanced | Allergies: None | Avoid: None"

        // Parallel fetch for live data with 1.8s timeout to never delay chat UI
        val glucoseDeferred = async {
            withTimeoutOrNull(1800L) {
                try {
                    glucoseRepo.getGlucoseHistory("7 days").getOrNull()
                } catch (_: Exception) {
                    null
                }
            }
        }

        val medsDeferred = async {
            withTimeoutOrNull(1800L) {
                try {
                    medsRepo.getMedications().getOrNull()
                } catch (_: Exception) {
                    null
                }
            }
        }

        val dietDeferred = async {
            withTimeoutOrNull(1800L) {
                try {
                    dietRepo.getNutritionSummary().getOrNull()
                } catch (_: Exception) {
                    null
                }
            }
        }

        val foodLogDeferred = async {
            withTimeoutOrNull(1800L) {
                try {
                    dietRepo.getFoodLog().getOrNull()
                } catch (_: Exception) {
                    null
                }
            }
        }

        val activityDeferred = async {
            withTimeoutOrNull(1800L) {
                try {
                    activityRepo.getActivityHistory("All").getOrNull()
                } catch (_: Exception) {
                    null
                }
            }
        }

        val glucoseHistory = glucoseDeferred.await()
        val medsData = medsDeferred.await()
        val nutritionSummary = dietDeferred.await()
        val foodLog = foodLogDeferred.await()
        val activityData = activityDeferred.await()

        // 3. Glucose metrics
        val readings = glucoseHistory?.readings ?: emptyList()
        val latestReading = readings.firstOrNull()
        val latestGlucoseStr = if (latestReading != null) {
            val numVal = latestReading.value.toFloatOrNull()?.toInt() ?: latestReading.value
            val timeStr = latestReading.time.takeIf { it.isNotBlank() } ?: ""
            val timePart = if (timeStr.isNotBlank()) " • $timeStr" else ""
            "$numVal mg/dL (${latestReading.readingType}$timePart)"
        } else {
            "No readings logged yet"
        }

        val avgGlucose = glucoseHistory?.stats?.averageNum?.takeIf { it > 0f }
            ?: if (readings.isNotEmpty()) {
                val valid = readings.mapNotNull { it.value.toFloatOrNull() }
                if (valid.isNotEmpty()) (valid.sum() / valid.size) else 0f
            } else 0f

        val estimatedHba1c = if (avgGlucose > 0f) {
            String.format(Locale.US, "%.1f", (avgGlucose + 46.7f) / 28.7f).toFloatOrNull()
        } else null

        // 4. Medications
        val activeMeds = medsData?.medications ?: emptyList()
        val medsStr = if (activeMeds.isNotEmpty()) {
            activeMeds.joinToString("; ") { med ->
                val time = med.reminderTime.takeIf { it.isNotBlank() } ?: "Daily"
                val timing = if (med.timing.isNotBlank()) " • ${med.timing}" else ""
                "${med.name} ${med.dose} ($time$timing)"
            }
        } else {
            "No active medications scheduled"
        }

        // 5. Calories in & meals
        val calIn = nutritionSummary?.calories?.consumed
            ?: foodLog?.summary?.totalCaloriesNum
            ?: 0
        val mealNames = foodLog?.meals?.map { it.title }?.filter { it.isNotBlank() }?.distinct()?.take(4) ?: emptyList()
        val calInStr = if (calIn > 0 || mealNames.isNotEmpty()) {
            val mealsList = if (mealNames.isNotEmpty()) " (Meals: ${mealNames.joinToString(", ")})" else ""
            "$calIn kcal$mealsList"
        } else {
            "0 kcal"
        }

        // 6. Calories burned & workout
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayWorkouts = activityData?.workouts?.filter {
            it.rawDate == todayDate || it.date.startsWith("Today") || it.date.contains(todayDate)
        } ?: emptyList()

        val burnedCal = if (todayWorkouts.isNotEmpty()) {
            todayWorkouts.sumOf { it.caloriesKcal }
        } else {
            activityData?.summary?.totalCaloriesKcal ?: 0
        }

        val activeMins = if (todayWorkouts.isNotEmpty()) {
            todayWorkouts.sumOf { it.durationMins }
        } else {
            activityData?.summary?.averageDurationMins ?: 0
        }

        val calBurnedStr = if (burnedCal > 0 || activeMins > 0) {
            "$burnedCal kcal ($activeMins mins active)"
        } else {
            "0 kcal (0 mins active)"
        }

        ChatPatientContext(
            patientName = name,
            latestGlucose = latestGlucoseStr,
            avgGlucose = if (avgGlucose > 0f) String.format(Locale.US, "%.1f", avgGlucose).toFloatOrNull() else null,
            estimatedHba1c = estimatedHba1c,
            glucoseHistoryCount = readings.size,
            activeMedications = medsStr,
            todayWaterGlasses = waterStr,
            todayCaloriesIn = calInStr,
            todayCaloriesBurned = calBurnedStr,
            foodPreferences = foodPrefsStr
        )
    }
}
