package com.simats.diawell123.ui.ai

import com.simats.diawell123.data.model.ChatPatientContext
import java.util.regex.Pattern

/**
 * Intelligent on-device fallback responder for Dr. Jarvis.
 * Uses live local ChatPatientContext (Personal Health RAG) to accurately answer
 * user questions regarding blood sugar, medications, diet, and water even without server connectivity.
 */
object OfflineJarvisResponder {

    fun generateResponse(userPrompt: String, patientContext: ChatPatientContext?): String {
        val trimmed = userPrompt.trim()
        val lower = trimmed.lowercase()
        val patientName = patientContext?.patientName?.takeIf { it.isNotBlank() } ?: "User"

        // 1. Greetings, Thanks & Goodbyes
        if (Pattern.compile("^(hi|hello|hey|hai|vanakkam|good\\s*(morning|afternoon|evening)|halo|dr\\s*jarvis|jarvis)($|\\s)", Pattern.CASE_INSENSITIVE).matcher(lower).find() && lower.length < 25) {
            return "Hello $patientName! I am Dr. Jarvis, your DiaWell diabetes wellness companion. How can I assist you with your blood sugar, medications, diet, or workout today?"
        }

        if (Pattern.compile("^(thank\\s*you|thanks|thanku|thx|nandri|romba\\s*nandri)($|\\s)", Pattern.CASE_INSENSITIVE).matcher(lower).find() && lower.length < 30) {
            return "You're very welcome, $patientName! Stay healthy, monitor your glucose levels regularly, and feel free to ask anytime. Take care!"
        }

        if (Pattern.compile("^(bye|goodbye|see\\s*you|tata|good\\s*night)($|\\s)", Pattern.CASE_INSENSITIVE).matcher(lower).find() && lower.length < 20) {
            return "Goodbye $patientName! Take great care of your health and reach out anytime you need support."
        }

        // 2. Personal Intent A: Blood Sugar / Glucose / HbA1c
        val isGlucoseIntent = Pattern.compile(
            "\\b(my\\s*(glucose|sugar|blood\\s*sugar|reading|readings|level|levels|hba1c|a1c)|en\\s*sugar|enoda\\s*sugar|sugar\\s*evlo|check\\s*my\\s*sugar|what\\s*(is|are)\\s*my\\s*(glucose|sugar|blood\\s*sugar|hba1c|reading)|how\\s*is\\s*my\\s*(glucose|sugar)|tell\\s*me\\s*my\\s*(glucose|sugar)|sugar\\s*level|glucose\\s*level)\\b",
            Pattern.CASE_INSENSITIVE
        ).matcher(lower).find()

        if (isGlucoseIntent) {
            val latest = patientContext?.latestGlucose
            if (!latest.isNullOrBlank() && latest != "No readings logged yet") {
                val numMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(latest)
                val num = if (numMatcher.find()) numMatcher.group(1)?.toFloatOrNull() ?: 120f else 120f

                val interp = when {
                    num < 70 -> "⚠️ **Low Blood Glucose (Hypoglycemia):** Blood sugar is critically below 70 mg/dL. Take 15g of fast-acting carbs (e.g., fruit juice, 3 glucose tablets, or sugar in water) immediately and re-test in 15 minutes."
                    num <= 130 -> "🎯 **In Target Range:** Excellent glucose control! Your reading is aligned with recommended fasting diabetes targets (80-130 mg/dL). Keep adhering to your nutrition and activity routine."
                    num <= 180 -> "📊 **Mildly Elevated (Post-Meal Range):** Normal if taken within 1 to 2 hours after a meal. Stay well hydrated and consider a gentle 15-minute walk to help insulin clear glucose into muscles."
                    else -> "⚠️ **Elevated Glucose (Hyperglycemia):** Your reading is above 180 mg/dL. Drink ample plain water, avoid high-glycemic carbohydrates or sugary foods, and engage in light walking if feeling well."
                }

                val avgStr = patientContext.avgGlucose?.let { "$it mg/dL" } ?: "${num.toInt()} mg/dL"
                val a1cStr = patientContext.estimatedHba1c?.let { "$it%" } ?: String.format("%.1f%%", (num + 46.7f) / 28.7f)

                return "Hello $patientName, based on your DiaWell logs:\n\n" +
                        "• **Latest Blood Glucose:** $latest\n" +
                        "• **Average Glucose:** $avgStr\n" +
                        "• **Estimated HbA1c (eA1c):** $a1cStr\n" +
                        "• **Clinical Status:** $interp"
            } else {
                return "You haven't logged any blood glucose readings yet today, $patientName. You can log one right now on the Glucose screen or just say **'Log my blood sugar 130'** and I will record it for you!"
            }
        }

        // 3. Personal Intent B: Medications / Tablets / Prescriptions
        val isMedsIntent = Pattern.compile(
            "\\b(my\\s*(medication|medications|medicine|medicines|tablet|tablets|pills?|dose|doses|prescription|prescriptions)|what\\s*(medication|medications|medicine|medicines|tablet|tablets|pill|pills)|what\\s*(am\\s*i\\s*taking|do\\s*i\\s*take)|en\\s*(tablet|marundhu)|next\\s*dose|medication\\s*schedule|what\\s*(are|is)\\s*my\\s*(medicine|medication|tablet|tablets|pills))\\b",
            Pattern.CASE_INSENSITIVE
        ).matcher(lower).find()

        if (isMedsIntent) {
            val meds = patientContext?.activeMedications
            if (!meds.isNullOrBlank() && meds != "No active medications scheduled") {
                return "Hello $patientName, here are your scheduled DiaWell medications:\n\n" +
                        "• **Active Prescriptions:** $meds\n\n" +
                        "⚡ **Doctor's Note:** Take all prescribed medications consistently with water. If taking Metformin, take it with or immediately after a meal to prevent stomach upset. Say **'Remind me to take [Medication] at [Time]'** anytime to add a new alarm!"
            } else {
                return "You have no active medications scheduled in your DiaWell routine, $patientName. Would you like me to set a reminder? Just say **'Remind me to take Metformin at 8 PM'** or tap the Medication tab!"
            }
        }

        // 4. Personal Intent C: Diet / Calories / Water / Exercise / Health Summary
        val isSummaryIntent = Pattern.compile(
            "\\b(my\\s*(diet|calories|meals?|food|water|workout|activity|exercise|report|summary|health\\s*today)|what\\s*did\\s*i\\s*eat|how\\s*am\\s*i\\s*doing|today'?s?\\s*(status|summary|health|report)|(how\\s*much|how\\s*many)\\s*(water|calories|glasses)|summary\\s*of\\s*my\\s*health)\\b",
            Pattern.CASE_INSENSITIVE
        ).matcher(lower).find()

        if (isSummaryIntent) {
            val latest = patientContext?.latestGlucose ?: "No readings logged yet"
            val meds = patientContext?.activeMedications ?: "No active medications scheduled"
            val water = patientContext?.todayWaterGlasses ?: "0 glasses (0 ml)"
            val calIn = patientContext?.todayCaloriesIn ?: "0 kcal"
            val calBurned = patientContext?.todayCaloriesBurned ?: "0 kcal (0 mins active)"
            val prefs = patientContext?.foodPreferences ?: "Diet: Balanced | Allergies: None | Avoid: None"

            return "Hello $patientName, here is your DiaWell health summary for today:\n\n" +
                    "• **Blood Glucose:** $latest\n" +
                    "• **Active Medications:** $meds\n" +
                    "• **Water Intake:** $water\n" +
                    "• **Calories Consumed:** $calIn\n" +
                    "• **Calories Burned:** $calBurned\n" +
                    "• **Food Preferences:** $prefs\n\n" +
                    "⚡ **Doctor's Tip:** Maintain a balanced low-glycemic diet, drink water regularly, and aim for a 15-20 minute brisk walk after your largest meal to improve insulin sensitivity!"
        }

        // 5. General Clinical Questions Customized with Patient Profile
        val prefs = patientContext?.foodPreferences ?: ""
        return when {
            lower.contains("diet") || lower.contains("food") || lower.contains("eat") || lower.contains("dinner") || lower.contains("lunch") || lower.contains("sapadu") ->
                "Hello $patientName! For optimal diabetes control, prioritize high-fiber, low-glycemic foods: sprouted legumes, green vegetables, methi roti, and lean proteins. Minimize refined white rice and sugary snacks. ($prefs)"

            lower.contains("walk") || lower.contains("exercise") || lower.contains("workout") || lower.contains("nadai") ->
                "Hello $patientName! A brisk 15 to 30-minute walk after lunch or dinner directly stimulates skeletal muscle glucose uptake and prevents post-meal blood sugar spikes."

            lower.contains("sugar") || lower.contains("glucose") || lower.contains("sarkarai") ->
                "For steady blood sugar control, target fasting between 80-130 mg/dL and post-meal under 180 mg/dL. If you feel shaky or dizzy (<70 mg/dL), take 15g fast carbs and re-test in 15 minutes."

            lower.contains("metformin") || lower.contains("medicine") || lower.contains("tablet") || lower.contains("insulin") ->
                "Take all prescribed diabetes medications consistently as directed. Metformin should be taken with or immediately after meals to minimize gastrointestinal discomfort."

            else ->
                "Hello $patientName! I am monitoring your DiaWell health profile. Maintain balanced low-glycemic nutrition, stay hydrated with water, and track your readings regularly. Feel free to ask me about your sugar, medications, or diet anytime!"
        }
    }
}
