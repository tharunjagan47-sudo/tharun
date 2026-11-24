package com.simats.diawell123.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.simats.diawell123.receiver.NudgeActionReceiver
import java.util.Calendar

data class SmartNudgeVariant(
    val title: String,
    val body: String,
)

data class SmartNudgeSlot(
    val slotId: Int,
    val hour: Int,
    val minute: Int,
    val variants: List<SmartNudgeVariant>,
    val primaryActionLabel: String,
    val primaryActionType: String,
    val secondaryActionLabel: String? = null,
    val secondaryActionType: String? = null,
    val targetScreen: String = "home",
)

object SmartNudgeEngine {

    private const val TAG = "SmartNudgeEngine"
    private const val PREFS_NAME = "diawell_smart_nudges_prefs"
    private const val KEY_ROTATION_INDEX = "nudge_rotation_index"
    private const val KEY_PERIODIC_INDEX = "periodic_pulse_index"

    const val PERIODIC_PULSE_REQUEST_CODE = 2099
    private const val PERIODIC_INTERVAL_MS = 30 * 60 * 1000L

    private val dailySlots = listOf(
        // 2001: 8:00 AM — Morning Hydration Nudge
        SmartNudgeSlot(
            slotId = 2001,
            hour = 8,
            minute = 0,
            variants = listOf(
                SmartNudgeVariant(
                    title = "Morning Hydration Check 💧",
                    body = "Start your morning with a fresh glass of water! Hydration supports metabolism and flushes excess glucose.",
                ),
                SmartNudgeVariant(
                    title = "Rise & Shine Hydration 💧",
                    body = "Good morning! Drinking water before your routine keeps energy high and blood sugar balanced.",
                ),
            ),
            primaryActionLabel = "Got It, Drinking Water 💧",
            primaryActionType = NudgeActionReceiver.ACTION_DISMISS_NUDGE,
            targetScreen = "home",
        ),
        // 2002: 10:30 AM — Mid-Morning Hydration Nudge
        SmartNudgeSlot(
            slotId = 2002,
            hour = 10,
            minute = 30,
            variants = listOf(
                SmartNudgeVariant(
                    title = "Thirsty? Your kidneys called! 💧",
                    body = "Mid-morning hydration check! One glass of water right now flushes excess glucose. Drink up!",
                ),
                SmartNudgeVariant(
                    title = "Mid-Morning Water Reminder 💧",
                    body = "Time for a glass of water! Staying hydrated keeps your energy and blood sugar steady.",
                ),
            ),
            primaryActionLabel = "Got It, Drinking Water 💧",
            primaryActionType = NudgeActionReceiver.ACTION_DISMISS_NUDGE,
            targetScreen = "home",
        ),
        // 2003: 1:30 PM — Post-Lunch Hydration Nudge
        SmartNudgeSlot(
            slotId = 2003,
            hour = 13,
            minute = 30,
            variants = listOf(
                SmartNudgeVariant(
                    title = "Afternoon Hydration 💧",
                    body = "Time for a sip! A glass of water after your meal supports gentle digestion and metabolic balance.",
                ),
                SmartNudgeVariant(
                    title = "Stay Hydrated this Afternoon 💧",
                    body = "Beat the afternoon slump! A glass of clean water keeps you active and focused.",
                ),
            ),
            primaryActionLabel = "Got It, Drinking Water 💧",
            primaryActionType = NudgeActionReceiver.ACTION_DISMISS_NUDGE,
            targetScreen = "home",
        ),
        // 2004: 4:30 PM — Evening Water Reminder
        SmartNudgeSlot(
            slotId = 2004,
            hour = 16,
            minute = 30,
            variants = listOf(
                SmartNudgeVariant(
                    title = "Evening Hydration Check 💧",
                    body = "Before your evening tea or snacks, drink a glass of water to keep your hydration on track.",
                ),
                SmartNudgeVariant(
                    title = "Hydrate & Recharge 💧",
                    body = "A glass of water right now helps flush excess sugar and maintain steady evening energy.",
                ),
            ),
            primaryActionLabel = "Got It, Drinking Water 💧",
            primaryActionType = NudgeActionReceiver.ACTION_DISMISS_NUDGE,
            targetScreen = "home",
        ),
        // 2005: 7:00 PM — Pre-Dinner Hydration Nudge
        SmartNudgeSlot(
            slotId = 2005,
            hour = 19,
            minute = 0,
            variants = listOf(
                SmartNudgeVariant(
                    title = "Pre-Dinner Glass of Water 💧",
                    body = "Drinking water before dinner aids healthy digestion and helps prevent glucose spikes.",
                ),
                SmartNudgeVariant(
                    title = "Evening Water Check 💧",
                    body = "Keep up your hydration habit! A glass of water now prepares your body for dinner.",
                ),
            ),
            primaryActionLabel = "Got It, Drinking Water 💧",
            primaryActionType = NudgeActionReceiver.ACTION_DISMISS_NUDGE,
            targetScreen = "home",
        ),
        // 2006: 9:30 PM — Bedtime Hydration Nudge
        SmartNudgeSlot(
            slotId = 2006,
            hour = 21,
            minute = 30,
            variants = listOf(
                SmartNudgeVariant(
                    title = "Bedtime Hydration 💧",
                    body = "A gentle glass of water before winding down keeps you comfortable and hydrated through the night.",
                ),
                SmartNudgeVariant(
                    title = "Night Hydration Reminder 💧",
                    body = "Stay hydrated before sleep to support your body's overnight glucose balance.",
                ),
            ),
            primaryActionLabel = "Got It, Drinking Water 💧",
            primaryActionType = NudgeActionReceiver.ACTION_DISMISS_NUDGE,
            targetScreen = "home",
        ),
    )

    /**
     * Silently schedules all 6 daily exact health nudge alarms (2001..2006) plus the
     * rolling 30-minute contextual engagement pulse (2099). Never fires immediately on launch.
     */
    fun scheduleAllDailyNudges(context: Context) {
        DiaWellNotificationHelper.createNotificationChannels(context)
        dailySlots.forEach { slot ->
            scheduleSlotAlarm(context, slot)
        }
        schedulePeriodicEngagementPulse(context)
    }

    fun triggerDailySlotAndReschedule(context: Context, slotId: Int) {
        val slot = dailySlots.firstOrNull { it.slotId == slotId } ?: return
        DiaWellNotificationHelper.createNotificationChannels(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val daySeed = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val storedOffset = prefs.getInt("${KEY_ROTATION_INDEX}_$slotId", 0)
        val variantIndex = (daySeed + storedOffset) % slot.variants.size
        prefs.edit().putInt("${KEY_ROTATION_INDEX}_$slotId", storedOffset + 1).apply()

        val variant = slot.variants[variantIndex]
        DiaWellNotificationHelper.sendInteractiveNudge(
            context = context,
            nudgeId = slot.slotId,
            title = variant.title,
            body = variant.body,
            primaryActionLabel = slot.primaryActionLabel,
            primaryActionType = slot.primaryActionType,
            secondaryActionLabel = slot.secondaryActionLabel,
            secondaryActionType = slot.secondaryActionType,
            targetScreen = slot.targetScreen,
        )

        // Schedule exact tomorrow occurrence at the same clock time
        scheduleSlotAlarm(context, slot, forceTomorrow = true)
    }

    fun schedulePeriodicEngagementPulse(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextTriggerAt = computeNextDaytimePulseMillis()
        val pendingIntent = buildPeriodicPendingIntent(context)
        scheduleExactCompat(alarmManager, nextTriggerAt, pendingIntent)
    }

    fun triggerPeriodicEngagementPulseAndReschedule(context: Context) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val minute = Calendar.getInstance().get(Calendar.MINUTE)
        val isQuietHours = hour < 7 || (hour == 22 && minute >= 30) || hour >= 23

        if (!isQuietHours) {
            DiaWellNotificationHelper.createNotificationChannels(context)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pulseIdx = prefs.getInt(KEY_PERIODIC_INDEX, 0)
            prefs.edit().putInt(KEY_PERIODIC_INDEX, pulseIdx + 1).apply()

            val contextualSlot = pickContextualSlotForHour(hour)
            val variant = contextualSlot.variants[pulseIdx % contextualSlot.variants.size]
            DiaWellNotificationHelper.sendInteractiveNudge(
                context = context,
                nudgeId = PERIODIC_PULSE_REQUEST_CODE,
                title = variant.title,
                body = variant.body,
                primaryActionLabel = contextualSlot.primaryActionLabel,
                primaryActionType = contextualSlot.primaryActionType,
                secondaryActionLabel = contextualSlot.secondaryActionLabel,
                secondaryActionType = contextualSlot.secondaryActionType,
                targetScreen = contextualSlot.targetScreen,
            )
        }

        schedulePeriodicEngagementPulse(context)
    }

    private fun pickContextualSlotForHour(hour: Int): SmartNudgeSlot {
        val targetSlotId = when {
            hour in 7..9 -> 2001
            hour in 10..11 -> 2002
            hour in 12..14 -> 2003
            hour in 15..16 -> 2004
            hour in 17..19 -> 2005
            else -> 2006
        }
        return dailySlots.first { it.slotId == targetSlotId }
    }

    private fun computeNextDaytimePulseMillis(): Long {
        val candidate = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis() + PERIODIC_INTERVAL_MS
        }
        val hour = candidate.get(Calendar.HOUR_OF_DAY)
        val minute = candidate.get(Calendar.MINUTE)
        if (hour < 7 || (hour == 22 && minute >= 30) || hour >= 23) {
            if (hour >= 22) {
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
            candidate.set(Calendar.HOUR_OF_DAY, 7)
            candidate.set(Calendar.MINUTE, 30)
            candidate.set(Calendar.SECOND, 0)
            candidate.set(Calendar.MILLISECOND, 0)
        }
        return candidate.timeInMillis
    }

    private fun scheduleSlotAlarm(
        context: Context,
        slot: SmartNudgeSlot,
        forceTomorrow: Boolean = false,
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, slot.hour)
            set(Calendar.MINUTE, slot.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (forceTomorrow || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        scheduleExactCompat(alarmManager, cal.timeInMillis, buildSlotPendingIntent(context, slot.slotId))
    }

    private fun scheduleExactCompat(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
    ) {
        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
        } catch (se: SecurityException) {
            Log.w(TAG, "Exact alarm permission unavailable, falling back to inexact idle alarm", se)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    fun cancelAllNudges(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        dailySlots.forEach { slot ->
            val pi = buildSlotPendingIntent(context, slot.slotId)
            alarmManager.cancel(pi)
        }
        val pulsePi = buildPeriodicPendingIntent(context)
        alarmManager.cancel(pulsePi)
    }

    private fun buildSlotPendingIntent(context: Context, slotId: Int): PendingIntent {
        val intent = Intent(context, NudgeActionReceiver::class.java).apply {
            action = NudgeActionReceiver.ACTION_TRIGGER_SCHEDULED_NUDGE
            putExtra(NudgeActionReceiver.EXTRA_SLOT_ID, slotId)
        }
        return PendingIntent.getBroadcast(
            context,
            slotId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildPeriodicPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, NudgeActionReceiver::class.java).apply {
            action = NudgeActionReceiver.ACTION_TRIGGER_PERIODIC_NUDGE
        }
        return PendingIntent.getBroadcast(
            context,
            PERIODIC_PULSE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
