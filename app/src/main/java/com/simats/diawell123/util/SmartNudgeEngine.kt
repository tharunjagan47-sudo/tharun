package com.simats.diawell123.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.simats.diawell123.receiver.NudgeActionReceiver
import java.util.Calendar

/**
 * Swiggy/Zomato-style Smart Contextual Health Nudge Engine.
 * Dispatches engaging, time-aware English & Tanglish nudges strictly on the standard
 * interactive notification channel (never triggering MedAlarmActivity).
 */
object SmartNudgeEngine {

    private const val TAG = "SmartNudgeEngine"
    private const val KEY_DEMO_INDEX = "demo_nudge_cycle_index"

    enum class NudgeCategory {
        HYDRATION,
        LOW_GI_MEAL,
        EVENING_SNACK,
        POST_MEAL_WALK,
        SUGAR_CHECK
    }

    data class SmartNudgeTemplate(
        val id: Int,
        val category: NudgeCategory,
        val title: String,
        val body: String,
        val primaryActionLabel: String,
        val primaryActionType: String,
        val secondaryActionLabel: String? = null,
        val secondaryActionType: String? = null,
        val targetScreen: String = "home"
    )

    private data class ScheduledNudgeSlot(
        val slotIndex: Int,
        val hour: Int,
        val minute: Int,
        val category: NudgeCategory
    )

    val demoTemplates: List<SmartNudgeTemplate> = listOf(
        SmartNudgeTemplate(
            id = 9101,
            category = NudgeCategory.HYDRATION,
            title = "💧 Thirsty? Your kidneys called!",
            body = "One glass of water right now helps flush out excess glucose naturally. Kudichacha?",
            primaryActionLabel = "✅ Drank 1 Glass (+250ml)",
            primaryActionType = NudgeActionReceiver.ACTION_QUICK_LOG_WATER,
            targetScreen = "home"
        ),
        SmartNudgeTemplate(
            id = 9102,
            category = NudgeCategory.LOW_GI_MEAL,
            title = "🍽️ Hungry? Skip the white rice spike!",
            body = "Craving a heavy meal? Pair your lunch with extra poriyal, greens & buttermilk to keep post-meal sugar steady.",
            primaryActionLabel = "🥗 Log My Meal",
            primaryActionType = DiaWellNotificationHelper.NUDGE_ACTION_OPEN_DIET,
            secondaryActionLabel = "🤖 Ask Dr. Jarvis",
            secondaryActionType = DiaWellNotificationHelper.NUDGE_ACTION_OPEN_AI_CHAT,
            targetScreen = "daily_diet_plan"
        ),
        SmartNudgeTemplate(
            id = 9103,
            category = NudgeCategory.EVENING_SNACK,
            title = "☕ Tea time cravings hitting hard?",
            body = "Say no to bajji & biscuits! Grab a handful of roasted chana, almonds, or sundal instead.",
            primaryActionLabel = "👍 Got It, Staying Healthy!",
            primaryActionType = NudgeActionReceiver.ACTION_DISMISS_NUDGE,
            targetScreen = "daily_diet_plan"
        ),
        SmartNudgeTemplate(
            id = 9104,
            category = NudgeCategory.POST_MEAL_WALK,
            title = "👟 Couch vs. 15-Min Walk — Who wins today?",
            body = "Just a 15-minute brisk walk after meals drops your glucose spike by up to 25 mg/dL. Polama?",
            primaryActionLabel = "🏃 Start 15m Walk",
            primaryActionType = NudgeActionReceiver.ACTION_QUICK_LOG_WALK,
            targetScreen = "activity_tracking"
        ),
        SmartNudgeTemplate(
            id = 9105,
            category = NudgeCategory.SUGAR_CHECK,
            title = "🩸 Quick 10-second check-in?",
            body = "Curious what your numbers look like today? Log your blood sugar now so Dr. Jarvis can track your streak!",
            primaryActionLabel = "🩸 Log Sugar Now",
            primaryActionType = DiaWellNotificationHelper.NUDGE_ACTION_OPEN_GLUCOSE,
            targetScreen = "log_glucose"
        )
    )

    private val dailySlots: List<ScheduledNudgeSlot> = listOf(
        ScheduledNudgeSlot(slotIndex = 0, hour = 8, minute = 0, category = NudgeCategory.SUGAR_CHECK),
        ScheduledNudgeSlot(slotIndex = 1, hour = 10, minute = 30, category = NudgeCategory.HYDRATION),
        ScheduledNudgeSlot(slotIndex = 2, hour = 13, minute = 0, category = NudgeCategory.LOW_GI_MEAL),
        ScheduledNudgeSlot(slotIndex = 3, hour = 15, minute = 0, category = NudgeCategory.SUGAR_CHECK),
        ScheduledNudgeSlot(slotIndex = 4, hour = 15, minute = 30, category = NudgeCategory.HYDRATION),
        ScheduledNudgeSlot(slotIndex = 5, hour = 16, minute = 30, category = NudgeCategory.EVENING_SNACK),
        ScheduledNudgeSlot(slotIndex = 6, hour = 18, minute = 15, category = NudgeCategory.POST_MEAL_WALK),
        ScheduledNudgeSlot(slotIndex = 7, hour = 20, minute = 0, category = NudgeCategory.LOW_GI_MEAL),
        ScheduledNudgeSlot(slotIndex = 8, hour = 21, minute = 0, category = NudgeCategory.POST_MEAL_WALK)
    )

    /**
     * Cycles through the 5 catchy nudge templates (Water -> Meal -> Tea Snack -> Walk -> Sugar Check)
     * and dispatches an interactive notification banner immediately for live demo / review.
     */
    fun triggerNextDemoNudge(context: Context): SmartNudgeTemplate {
        val prefs = context.getSharedPreferences(NudgeActionReceiver.PREFS_NAME, Context.MODE_PRIVATE)
        val currentIdx = prefs.getInt(KEY_DEMO_INDEX, 0).coerceAtLeast(0) % demoTemplates.size
        val nextIdx = (currentIdx + 1) % demoTemplates.size
        prefs.edit().putInt(KEY_DEMO_INDEX, nextIdx).apply()

        val template = demoTemplates[currentIdx]
        dispatchTemplate(context, template, uniqueTagId = template.id)
        return template
    }

    /**
     * Schedules all 9 daily time-window nudges via AlarmManager.setWindow().
     * Past times strictly roll over to tomorrow so nothing fires uninvited on startup.
     */
    fun scheduleDailyNudges(context: Context) {
        for (slot in dailySlots) {
            scheduleSlot(context, slot, forceTomorrow = false)
        }
    }

    /**
     * Called by NudgeActionReceiver when a scheduled window fires; displays the matching nudge
     * and schedules tomorrow's window for the same slot.
     */
    fun triggerScheduledSlotAndReschedule(context: Context, slotIndex: Int) {
        val slot = dailySlots.firstOrNull { it.slotIndex == slotIndex } ?: return
        val template = demoTemplates.firstOrNull { it.category == slot.category } ?: return
        dispatchTemplate(context, template, uniqueTagId = 9200 + slot.slotIndex)
        scheduleSlot(context, slot, forceTomorrow = true)
    }

    private fun scheduleSlot(context: Context, slot: ScheduledNudgeSlot, forceTomorrow: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, slot.hour)
            set(Calendar.MINUTE, slot.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val minTrigger = System.currentTimeMillis() + 10_000L
        if (forceTomorrow || cal.timeInMillis <= minTrigger) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, NudgeActionReceiver::class.java).apply {
            action = NudgeActionReceiver.ACTION_TRIGGER_SCHEDULED_NUDGE
            putExtra("nudge_slot_index", slot.slotIndex)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9200 + slot.slotIndex,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                15 * 60 * 1000L, // 15-min battery-friendly window
                pendingIntent
            )
            Log.d(TAG, "Scheduled nudge slot ${slot.slotIndex} (${slot.category}) for ${cal.time}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to schedule nudge slot ${slot.slotIndex}: ${e.message}")
        }
    }

    private fun dispatchTemplate(context: Context, template: SmartNudgeTemplate, uniqueTagId: Int) {
        DiaWellNotificationHelper.sendInteractiveNudge(
            context = context,
            nudgeId = uniqueTagId,
            title = template.title,
            body = template.body,
            primaryActionLabel = template.primaryActionLabel,
            primaryActionType = template.primaryActionType,
            secondaryActionLabel = template.secondaryActionLabel,
            secondaryActionType = template.secondaryActionType,
            targetScreen = template.targetScreen
        )
    }
}

