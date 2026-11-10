package com.simats.diawell123.ui.ai

import android.content.Context
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.AlertType
import com.simats.diawell123.data.repository.GlucoseRepository
import com.simats.diawell123.data.repository.MedsRepository
import com.simats.diawell123.util.DiaWellNotificationHelper
import com.simats.diawell123.util.MedicationStatusUpdater
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

sealed class VoiceActionResult {
    data object NotACommand : VoiceActionResult()

    data class Executed(
        val displayReply: String,
        val spokenReply: String,
        val alertType: AlertType = AlertType.NONE
    ) : VoiceActionResult()
}

/**
 * Parses natural-language English and Tanglish voice commands for hands-free
 * blood glucose logging and medication alarm scheduling.
 */
object JarvisVoiceCommandHandler {

    suspend fun tryExecuteCommand(
        context: Context,
        utterance: String,
        glucoseRepository: GlucoseRepository,
        medsRepository: MedsRepository
    ): VoiceActionResult {
        val text = utterance.trim()
        if (text.isBlank()) return VoiceActionResult.NotACommand
        val lower = text.lowercase(Locale.US)

        // 1. Check Medication Alarm Scheduling intent first
        if (isMedicationScheduleIntent(lower)) {
            return handleMedicationScheduleCommand(context, text, lower, medsRepository)
        }

        // 2. Check Blood Sugar Logging intent
        if (isGlucoseLogIntent(lower)) {
            return handleGlucoseLogCommand(context, lower, glucoseRepository)
        }

        return VoiceActionResult.NotACommand
    }

    private fun isMedicationScheduleIntent(lower: String): Boolean {
        val hasTriggerWord = lower.contains("remind") ||
            lower.contains("alarm") ||
            lower.contains("schedule") ||
            lower.contains("set a ") ||
            lower.startsWith("set ")
        val hasMedContext = lower.contains("med") ||
            lower.contains("tablet") ||
            lower.contains("pill") ||
            lower.contains("capsule") ||
            lower.contains("insulin") ||
            lower.contains("dose") ||
            lower.contains("metformin") ||
            lower.contains("glimepiride") ||
            lower.contains("take ") ||
            lower.contains("to take")
        val hasTimePhrase = Pattern.compile(
            "\\b(?:at\\s+)?\\d{1,2}(?:[:.]\\d{2})?\\s*(?:a\\.?m\\.?|p\\.?m\\.?|o'?clock)\\b|\\b(?:in|after)\\s+\\d{1,3}\\s*(?:minutes?|mins?)\\b"
        ).matcher(lower).find()
        return hasTriggerWord && (hasMedContext || (hasTimePhrase && !lower.contains("glucose") && !lower.contains("sugar")))
    }

    private fun isGlucoseLogIntent(lower: String): Boolean {
        val isQuestion = lower.startsWith("what") ||
            lower.startsWith("why") ||
            lower.startsWith("how") ||
            lower.startsWith("when") ||
            lower.contains("what to do") ||
            lower.contains("should i") ||
            lower.contains("normal range")

        val hasExplicitVerb = lower.contains("log ") ||
            lower.contains("record ") ||
            lower.contains("save ") ||
            lower.contains("add ") ||
            lower.contains("enter ") ||
            lower.contains("update ")

        if (isQuestion && !hasExplicitVerb) return false

        val hasSugarWord = lower.contains("glucose") ||
            lower.contains("blood sugar") ||
            lower.contains("sugar") ||
            lower.contains("sarkarai") ||
            lower.contains("sarkkarai") ||
            lower.contains("reading")

        val hasDeclarativePattern = hasExplicitVerb ||
            lower.contains("my sugar is") ||
            lower.contains("my blood sugar is") ||
            lower.contains("my glucose is") ||
            lower.contains("sugar is") ||
            lower.contains("glucose is") ||
            lower.contains("sugar level") ||
            lower.contains("en sarkarai") ||
            lower.contains("fasting sugar") ||
            lower.contains("post meal sugar")

        return hasSugarWord && hasDeclarativePattern && Pattern.compile("\\b\\d{2,3}(?:\\.\\d+)?\\b").matcher(lower).find()
    }

    private suspend fun handleGlucoseLogCommand(
        context: Context,
        lower: String,
        glucoseRepository: GlucoseRepository
    ): VoiceActionResult {
        val matcher = Pattern.compile("\\b(\\d{2,3}(?:\\.\\d+)?)\\b").matcher(lower)
        val valueFloat = if (matcher.find()) matcher.group(1)?.toFloatOrNull() else null

        if (valueFloat == null || valueFloat < 40f || valueFloat > 600f) {
            val msg = "Please specify a valid blood glucose reading between 40 and 600 mg/dL, for example: 'Log my fasting glucose 115'."
            return VoiceActionResult.Executed(displayReply = msg, spokenReply = msg)
        }

        val readingType = when {
            lower.contains("fasting") || lower.contains("before breakfast") ||
                lower.contains("kalaila") || lower.contains("kaalaila") ||
                lower.contains("empty stomach") -> "Fasting"
            lower.contains("post") || lower.contains("after meal") ||
                lower.contains("after lunch") || lower.contains("after dinner") ||
                lower.contains("after breakfast") || lower.contains("after food") -> "Post-Meal"
            lower.contains("bedtime") || lower.contains("before sleep") ||
                lower.contains("night") || lower.contains("rathiri") -> "Bedtime"
            else -> "Random"
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val nowTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())

        val result = glucoseRepository.logGlucose(
            readingValue = valueFloat,
            readingType = readingType,
            readingDate = todayDate,
            readingTime = nowTime,
            notes = "Logged hands-free via Dr. Jarvis Voice Assistant"
        )

        return result.fold(
            onSuccess = {
                val valInt = valueFloat.toInt()
                val advice: String
                val spokenAdvice: String
                val alertType: AlertType

                when {
                    valInt < 70 -> {
                        alertType = AlertType.HYPOGLYCEMIA_WARNING
                        advice = "🚨 **Low Blood Sugar Alert (< 70 mg/dL):** Take 15g fast-acting carbs (half cup fruit juice or 3 glucose tablets) immediately and recheck in 15 minutes."
                        spokenAdvice = "Warning: $valInt milligrams per deciliter is dangerously low. Have 15 grams of fast-acting carbs like half a cup of fruit juice right away and recheck in 15 minutes."
                        DiaWellNotificationHelper.showCriticalHealthAlert(
                            context = context,
                            title = "⚠️ Critical: Low Blood Sugar ($valInt mg/dL)",
                            message = "Have 15g fast-acting carbs immediately and recheck in 15 minutes."
                        )
                    }
                    valInt > 180 -> {
                        alertType = AlertType.HYPERGLYCEMIA_WARNING
                        advice = "⚠️ **Elevated Glucose Warning (> 180 mg/dL):** Hydrate with water and take a gentle 15-minute walk if safe. Avoid high-sugar foods."
                        spokenAdvice = "Caution: $valInt milligrams per deciliter is elevated. Drink water and take a gentle 15-minute walk if safe."
                        DiaWellNotificationHelper.showCriticalHealthAlert(
                            context = context,
                            title = "⚠️ High Blood Sugar Warning ($valInt mg/dL)",
                            message = "Your $readingType reading is elevated. Drink water and check ketones if persistent."
                        )
                    }
                    else -> {
                        alertType = AlertType.NONE
                        advice = "✅ **In Healthy Range:** Great job staying consistent with your DiaWell check-ins!"
                        spokenAdvice = "Great job, this reading is in a healthy range."
                        DiaWellNotificationHelper.showGeneralNotification(
                            context = context,
                            title = "🩸 Blood Sugar Logged: $valInt mg/dL",
                            message = "Logged $readingType reading ($valInt mg/dL) via Dr. Jarvis Voice Assistant."
                        )
                    }
                }

                val displayMsg = "✅ **Blood Glucose Logged via Voice**\n" +
                    "• **Reading:** $valInt mg/dL ($readingType)\n" +
                    "• **Time:** Today, $nowTime\n\n$advice"

                val spokenMsg = "Logged your $readingType blood sugar reading of $valInt milligrams per deciliter. $spokenAdvice"

                VoiceActionResult.Executed(
                    displayReply = displayMsg,
                    spokenReply = spokenMsg,
                    alertType = alertType
                )
            },
            onFailure = { error ->
                val errMsg = "I couldn't save your blood sugar reading right now: ${error.message ?: "Server unreachable"}."
                VoiceActionResult.Executed(displayReply = "⚠️ $errMsg", spokenReply = errMsg)
            }
        )
    }

    private suspend fun handleMedicationScheduleCommand(
        context: Context,
        originalText: String,
        lower: String,
        medsRepository: MedsRepository
    ): VoiceActionResult {
        val parsedSchedule = parseScheduleTime(lower)
        if (parsedSchedule == null) {
            val askMsg = "Sure! What time should I set the medication alarm for? Say something like: 'Remind me to take Metformin at 8 PM'."
            return VoiceActionResult.Executed(displayReply = askMsg, spokenReply = askMsg)
        }

        val (triggerMillis, formattedTime, timeMatchedSpan) = parsedSchedule

        val mealTiming = when {
            lower.contains("before meal") || lower.contains("before food") ||
                lower.contains("before breakfast") || lower.contains("before lunch") ||
                lower.contains("before dinner") -> "Before Meal"
            lower.contains("with meal") || lower.contains("with food") -> "With Meal"
            lower.contains("bedtime") || lower.contains("before sleep") -> "At Bedtime"
            lower.contains("empty stomach") -> "Empty Stomach"
            else -> "After Meal"
        }

        // Extract dosage if spoken (e.g., 500mg, 500 mg, 10 units, 1 tablet)
        val doseMatcher = Pattern.compile("\\b(\\d+(?:\\.\\d+)?\\s*(?:mg|ml|mcg|g|units?|tablets?|capsules?|drops?))\\b", Pattern.CASE_INSENSITIVE)
            .matcher(originalText)
        val extractedDose = if (doseMatcher.find()) doseMatcher.group(1)?.trim() ?: "Prescribed Dose" else "Prescribed Dose"

        val medName = extractMedicationName(originalText, timeMatchedSpan)
        val todayDisplay = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(triggerMillis))

        val result = medsRepository.addMedication(
            name = medName,
            dose = extractedDose,
            reminderTime = formattedTime,
            reminderDate = todayDisplay,
            timing = mealTiming,
            frequency = "Daily",
            notes = "Scheduled hands-free via Dr. Jarvis Voice Assistant"
        )

        return result.fold(
            onSuccess = { savedMed ->
                val resolvedUserId = try {
                    SessionManager.init(context.applicationContext)
                    SessionManager.getInstance().getUserId() ?: -1
                } catch (_: Exception) { -1 }

                val realMedId = if (savedMed.id > 0) savedMed.id else ((medName.hashCode() and 0x7FFFFFFF) % 10000 + 1000)
                DiaWellNotificationHelper.scheduleExactMedicationAlarm(
                    context = context,
                    medId = realMedId,
                    medName = savedMed.name.ifBlank { medName },
                    dose = savedMed.dose.ifBlank { extractedDose },
                    triggerAtMillis = triggerMillis,
                    userId = resolvedUserId
                )
                DiaWellNotificationHelper.showGeneralNotification(
                    context = context,
                    title = "💊 Medication Reminder Scheduled",
                    message = "Voice alarm set for ${savedMed.name.ifBlank { medName }} at $formattedTime ($mealTiming).",
                    notifId = realMedId
                )
                MedicationStatusUpdater.emitLocalChange(savedMed.id, savedMed.name.ifBlank { medName }, "Upcoming")

                val finalName = savedMed.name.ifBlank { medName }
                val finalDose = savedMed.dose.ifBlank { extractedDose }
                val displayMsg = "✅ **Medication Alarm Scheduled via Voice**\n" +
                    "• **Medicine:** $finalName ($finalDose)\n" +
                    "• **Alarm Time:** $formattedTime • $todayDisplay\n" +
                    "• **Meal Timing:** $mealTiming"

                val spokenMsg = "Medication alarm scheduled for $finalName at $formattedTime, $mealTiming."
                VoiceActionResult.Executed(displayReply = displayMsg, spokenReply = spokenMsg)
            },
            onFailure = { error ->
                val errMsg = "I couldn't schedule that medication reminder: ${error.message ?: "Server error"}."
                VoiceActionResult.Executed(displayReply = "⚠️ $errMsg", spokenReply = errMsg)
            }
        )
    }

    private data class ParsedSchedule(
        val triggerAtMillis: Long,
        val displayTime: String,
        val matchedText: String
    )

    private fun parseScheduleTime(lower: String): ParsedSchedule? {
        // 1. Relative minutes: "in 5 minutes" / "after 10 mins"
        val relMatcher = Pattern.compile("\\b(?:in|after)\\s+(\\d{1,3})\\s*(?:minutes?|mins?)\\b").matcher(lower)
        if (relMatcher.find()) {
            val mins = relMatcher.group(1)?.toIntOrNull()?.coerceIn(1, 720) ?: 5
            val targetMillis = System.currentTimeMillis() + (mins * 60_000L)
            val label = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(targetMillis))
            return ParsedSchedule(targetMillis, label, relMatcher.group(0) ?: "")
        }

        // 2. Clock time: "at 8:30 pm", "8 pm", "9.15 a.m.", "at 14:00", "8 o'clock"
        val timeMatcher = Pattern.compile(
            "(?:at\\s+)?(\\d{1,2})(?:[:.](\\d{2}))?\\s*(a\\.?m\\.?|p\\.?m\\.?|o'?clock)?"
        ).matcher(lower)

        while (timeMatcher.find()) {
            val rawHour = timeMatcher.group(1)?.toIntOrNull() ?: continue
            val rawMin = timeMatcher.group(2)?.toIntOrNull() ?: 0
            val amPmRaw = timeMatcher.group(3)?.replace(".", "")?.lowercase(Locale.US)

            val hasContext = amPmRaw != null || timeMatcher.group(2) != null ||
                lower.contains("at ${timeMatcher.group(1)}")
            if (!hasContext || rawHour > 23 || rawMin > 59) continue

            var hour24 = rawHour
            when {
                amPmRaw == "pm" && rawHour in 1..11 -> hour24 = rawHour + 12
                amPmRaw == "am" && rawHour == 12 -> hour24 = 0
                amPmRaw == null && rawHour in 1..12 && (lower.contains("night") || lower.contains("evening") || lower.contains("dinner")) -> {
                    if (rawHour < 12) hour24 = rawHour + 12
                }
            }

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, rawMin)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis <= System.currentTimeMillis() + 5000L) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            val displayTime = SimpleDateFormat("h:mm a", Locale.US).format(cal.time)
            return ParsedSchedule(cal.timeInMillis, displayTime, timeMatcher.group(0) ?: "")
        }
        return null
    }

    private fun extractMedicationName(originalText: String, matchedTimeSpan: String): String {
        var cleaned = originalText
        if (matchedTimeSpan.isNotBlank()) {
            cleaned = cleaned.replace(matchedTimeSpan, " ", ignoreCase = true)
        }
        cleaned = cleaned
            .replace(Regex("\\b\\d+(?:\\.\\d+)?\\s*(?:mg|ml|mcg|g|units?|tablets?|capsules?|drops?)\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\b(before|after|with)\\s+(meal|food|breakfast|lunch|dinner)\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\b(at\\s+bedtime|bedtime|empty\\s+stomach|from\\s+now|daily|every\\s+day)\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("^(please\\s+)?(hey\\s+jarvis|dr\\.?\\s+jarvis|jarvis)?\\s*,?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b(set|schedule|create|add|please|me|an|a|the|to|for|take|have|my|medication|medicine|tablet|pill|capsule|alarm|reminder|on|at)\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()

        if (cleaned.length < 2) return "Daily Medication"

        return cleaned.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { ch -> ch.uppercase(Locale.US) }
            }
    }
}
