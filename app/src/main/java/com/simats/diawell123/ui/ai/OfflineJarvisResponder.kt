package com.simats.diawell123.ui.ai

import com.simats.diawell123.data.model.ChatHistoryMessage
import com.simats.diawell123.data.model.ChatPatientContext
import java.util.regex.Pattern

/**
 * Intelligent on-device fallback responder for Dr. Jarvis.
 * Features:
 * 1. Verified Chennai Hospital Routing (Zero "Chuwati"/Guwahati hallucinations)
 * 2. Multi-turn conversation awareness & follow-up question resolution
 * 3. Live local ChatPatientContext (Personal Health RAG)
 * 4. Concise, question-specific answers (2-3 short sentences max)
 */
object OfflineJarvisResponder {

    fun generateResponse(
        userPrompt: String,
        patientContext: ChatPatientContext?,
        history: List<ChatHistoryMessage>? = null
    ): String {
        val trimmed = userPrompt.trim()
        val lower = trimmed.lowercase()
        val patientName = patientContext?.patientName?.takeIf { it.isNotBlank() } ?: "User"

        // Find last user and assistant turns for contextual follow-ups
        val lastAssistantTurn = history?.lastOrNull { it.role.equals("assistant", ignoreCase = true) || it.role.equals("ai", ignoreCase = true) }?.content?.lowercase() ?: ""
        val lastUserTurn = history?.lastOrNull { it.role.equals("user", ignoreCase = true) }?.content?.lowercase() ?: ""

        // 1. Greetings, Thanks & Goodbyes
        if (Pattern.compile("^(hi|hello|hey|hai|vanakkam|good\\s*(morning|afternoon|evening)|halo|dr\\s*jarvis|jarvis)($|\\s)", Pattern.CASE_INSENSITIVE).matcher(lower).find() && lower.length < 25) {
            return "Hello $patientName! I am Dr. Jarvis, your DiaWell diabetes wellness companion. How can I assist you with your blood sugar, medications, diet, or hospital guidance today?"
        }

        if (Pattern.compile("^(thank\\s*you|thanks|thanku|thx|nandri|romba\\s*nandri)($|\\s)", Pattern.CASE_INSENSITIVE).matcher(lower).find() && lower.length < 30) {
            return "You're very welcome, $patientName! Stay healthy, monitor your glucose levels regularly, and feel free to ask anytime. Take care!"
        }

        if (Pattern.compile("^(bye|goodbye|see\\s*you|tata|good\\s*night)($|\\s)", Pattern.CASE_INSENSITIVE).matcher(lower).find() && lower.length < 20) {
            return "Goodbye $patientName! Take great care of your health and reach out anytime you need support."
        }

        // 2. Verified Chennai Hospital & Specialist Routing (Primary Query)
        val isHospitalQuery = Pattern.compile(
            "\\b(hospital|hospitals|clinic|clinics|doctor|doctors|specialist|specialists|diabetologist|diabetologists|chennai|saveetha|simats|mohan|apollo|mv\\s*hospital|maruthuvamanai)\\b",
            Pattern.CASE_INSENSITIVE
        ).matcher(lower).find()

        if (isHospitalQuery) {
            // Check specific sub-queries
            if (lower.contains("anna nagar")) {
                return "In Anna Nagar, **Dr. Mohan's Diabetes Specialities Centre** (Plot No. 1221, 15th Main Rd, Anna Nagar West) and **Apollo Sugar Clinic** are the nearest top choices for diabetes care."
            }
            if (lower.contains("royapuram") || lower.contains("foot") || lower.contains("wound") || lower.contains("ulcer")) {
                return "**M.V. Hospital for Diabetes** in Royapuram is internationally recognized for diabetic foot care, limb salvage, and neuropathy treatment. Contact: 044-25954913."
            }
            if (lower.contains("saveetha") || lower.contains("simats") || lower.contains("thandalam") || lower.contains("poonamallee")) {
                return "**Saveetha Medical College Hospital (SIMATS)** in Thandalam offers comprehensive 24/7 super-speciality diabetes care, ICU, and affordable clinical consultations. Contact: 044-66726620."
            }
            if (lower.contains("contact") || lower.contains("number") || lower.contains("phone") || lower.contains("call") || lower.contains("emergency")) {
                return "Verified Chennai Medical Contacts:\n" +
                        "• Medical Emergency: **108**\n" +
                        "• Dr. Mohan's Diabetes: **044-43968888**\n" +
                        "• Saveetha Hospital (SIMATS): **044-66726620**\n" +
                        "• M.V. Hospital (Royapuram): **044-25954913**\n" +
                        "• Apollo Sugar: **1800-103-4460**"
            }

            // General Chennai Diabetes Center recommendation
            return "Top verified diabetes centres in Chennai:\n" +
                    "• **Dr. Mohan's Diabetes Specialities Centre** (Gopalapuram, Anna Nagar, Velachery - 044-43968888)\n" +
                    "• **Saveetha Medical College Hospital (SIMATS)** (Thandalam - 044-66726620)\n" +
                    "• **M.V. Hospital for Diabetes** (Royapuram - 044-25954913, Foot care specialist)\n" +
                    "• **Apollo Sugar Clinics** (Greams Road, OMR - 1800-103-4460)\n" +
                    "For medical emergencies, call **108** immediately."
        }

        // 3. Multi-Turn Follow-Up Handling (Contextual History Awareness)
        val wasHospitalContext = lastAssistantTurn.contains("hospital") || lastAssistantTurn.contains("centre") ||
                lastAssistantTurn.contains("clinic") || lastAssistantTurn.contains("chennai") || lastAssistantTurn.contains("dr. mohan") ||
                lastUserTurn.contains("hospital") || lastUserTurn.contains("doctor")

        if (wasHospitalContext) {
            if (lower.contains("anna nagar") || lower.contains("near me") || lower.contains("which one is near") || lower.contains("closer")) {
                return "In Anna Nagar, **Dr. Mohan's Diabetes Specialities Centre** (15th Main Rd, Anna Nagar West) and **Apollo Sugar Clinic** are the nearest recommended centres."
            }
            if (lower.contains("royapuram") || lower.contains("foot") || lower.contains("wound")) {
                return "For diabetic foot care or Royapuram area, visit **M.V. Hospital for Diabetes** (Royapuram). Contact: 044-25954913."
            }
            if (lower.contains("number") || lower.contains("phone") || lower.contains("contact") || lower.contains("call") || lower.contains("appointment")) {
                return "Key Chennai Contacts:\n• Dr. Mohan's: 044-43968888\n• Saveetha Hospital: 044-66726620\n• M.V. Hospital: 044-25954913\n• Emergency: 108"
            }
            if (lower.contains("eye") || lower.contains("retina") || lower.contains("vision")) {
                return "For diabetic eye care, visit **Dr. Mohan's Retinopathy Dept** (Gopalapuram / Anna Nagar) or **Sankara Nethralaya** (College Road, Nungambakkam)."
            }
        }

        // Contextual follow-up for Diet / Meals
        val wasDietContext = lastAssistantTurn.contains("diet") || lastAssistantTurn.contains("food") ||
                lastAssistantTurn.contains("calories") || lastUserTurn.contains("diet") || lastUserTurn.contains("eat")

        if (wasDietContext || lower.contains("dinner") || lower.contains("lunch") || lower.contains("breakfast")) {
            if (lower.contains("dinner") || lower.contains("night") || lower.contains("rathiri")) {
                return "For dinner, choose low-glycemic, light options: 2 methi (fenugreek) or multigrain rotis with dal and vegetable salad, or grilled paneer with sautéed veggies. Avoid heavy white rice at night."
            }
            if (lower.contains("lunch") || lower.contains("afternoon") || lower.contains("madhiyam")) {
                return "For lunch, fill half your plate with green vegetables/poriyal, one cup of brown rice, quinoa or foxtail millet, and a generous portion of dal, sambar, or curd."
            }
            if (lower.contains("breakfast") || lower.contains("morning") || lower.contains("kalai")) {
                return "For breakfast, opt for high-protein, high-fiber options: sprouted moong salad, vegetable oats upma, or 2 ragi dosas with mint or tomato chutney. Avoid sugary beverages."
            }
        }

        // Contextual follow-up for Blood Sugar ("is that normal?", "is it high?")
        val wasSugarContext = lastAssistantTurn.contains("glucose") || lastAssistantTurn.contains("blood sugar") ||
                lastUserTurn.contains("glucose") || lastUserTurn.contains("sugar")

        if (wasSugarContext && (lower.contains("normal") || lower.contains("good") || lower.contains("bad") || lower.contains("high") || lower.contains("ok"))) {
            val latest = patientContext?.latestGlucose
            if (!latest.isNullOrBlank() && latest != "No readings logged yet") {
                val numMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(latest)
                val num = if (numMatcher.find()) numMatcher.group(1)?.toFloatOrNull() ?: 120f else 120f
                return when {
                    num < 70 -> "⚠️ At $num mg/dL, your blood sugar is low (hypoglycemia). Consume 15g fast-acting carbs immediately and re-check in 15 minutes."
                    num <= 130 -> "✅ Yes, $num mg/dL is within the target fasting range (80-130 mg/dL). Great job maintaining control!"
                    num <= 180 -> "📊 $num mg/dL is acceptable if taken 1-2 hours after a meal (target <180 mg/dL). Stay active with a short walk."
                    else -> "⚠️ At $num mg/dL, it is elevated (target <130 fasting, <180 post-meal). Drink plenty of water and avoid sugary foods."
                }
            } else {
                return "Standard target ranges are 80-130 mg/dL for fasting and under 180 mg/dL within 2 hours after a meal. Log your reading to see where you stand!"
            }
        }

        // 4. Personal Intent A: Blood Sugar / Glucose / HbA1c
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
                    num < 70 -> "⚠️ Low (<70 mg/dL). Take 15g fast carbs immediately."
                    num <= 130 -> "🎯 In target fasting range (80-130 mg/dL)."
                    num <= 180 -> "📊 Post-meal range (target <180 mg/dL). Normal if tested after food."
                    else -> "⚠️ Elevated (>180 mg/dL). Drink water and take a light walk."
                }

                val avgStr = patientContext.avgGlucose?.let { "$it mg/dL" } ?: "${num.toInt()} mg/dL"
                val a1cStr = patientContext.estimatedHba1c?.let { "$it%" } ?: String.format("%.1f%%", (num + 46.7f) / 28.7f)

                return "• Latest Blood Glucose: $latest\n" +
                        "• Average Glucose: $avgStr | eA1c: $a1cStr\n" +
                        "• Status: $interp"
            } else {
                return "No blood glucose readings logged yet today, $patientName. Tap the Glucose tab or say **'Log my blood sugar 125'** to record one."
            }
        }

        // 5. Personal Intent B: Medications / Tablets / Prescriptions
        val isMedsIntent = Pattern.compile(
            "\\b(my\\s*(medication|medications|medicine|medicines|tablet|tablets|pills?|dose|doses|prescription|prescriptions)|what\\s*(medication|medications|medicine|medicines|tablet|tablets|pill|pills)|what\\s*(am\\s*i\\s*taking|do\\s*i\\s*take)|en\\s*(tablet|marundhu)|next\\s*dose|medication\\s*schedule|what\\s*(are|is)\\s*my\\s*(medicine|medication|tablet|tablets|pills))\\b",
            Pattern.CASE_INSENSITIVE
        ).matcher(lower).find()

        if (isMedsIntent) {
            val meds = patientContext?.activeMedications
            return if (!meds.isNullOrBlank() && meds != "No active medications scheduled") {
                "Active Prescriptions for $patientName:\n• $meds\n\nTake medications on schedule with water after meals to prevent stomach upset."
            } else {
                "You have no active medications scheduled in your DiaWell routine, $patientName. Say **'Remind me to take Metformin at 8 PM'** to set an alarm."
            }
        }

        // 6. Personal Intent C: Diet / Calories / Water / Exercise / Health Summary
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

            return "DiaWell Summary for $patientName today:\n" +
                    "• Glucose: $latest\n" +
                    "• Medications: $meds\n" +
                    "• Water: $water | Calories In: $calIn | Burned: $calBurned\n" +
                    "Aim for a brisk 15-20 minute walk after your main meal to improve insulin sensitivity."
        }

        // 7. General Clinical Questions Customized with Patient Profile (Concise: 2-3 short lines)
        val prefs = patientContext?.foodPreferences ?: ""
        return when {
            lower.contains("diet") || lower.contains("food") || lower.contains("eat") || lower.contains("sapadu") ->
                "Focus on low-glycemic, high-fiber Indian foods: sprouted legumes, methi roti, green vegetables, and millets. Minimize polished white rice and sweets. ($prefs)"

            lower.contains("walk") || lower.contains("exercise") || lower.contains("workout") || lower.contains("nadai") ->
                "A brisk 15 to 30-minute walk after meals stimulates skeletal muscle glucose uptake and prevents post-meal sugar spikes."

            lower.contains("sugar") || lower.contains("glucose") || lower.contains("sarkarai") ->
                "Recommended targets: Fasting 80-130 mg/dL, post-meal <180 mg/dL. If you feel shaky (<70 mg/dL), take 15g fast carbs immediately."

            lower.contains("metformin") || lower.contains("medicine") || lower.contains("tablet") || lower.contains("insulin") ->
                "Take all prescribed diabetes medications consistently as directed. Metformin should be taken with or immediately after meals."

            else ->
                "Hello $patientName! Maintain balanced low-glycemic nutrition, stay hydrated, and log your readings regularly. Ask me about your glucose, medications, or diet anytime!"
        }
    }
}
