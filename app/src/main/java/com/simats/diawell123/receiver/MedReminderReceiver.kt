package com.simats.diawell123.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.simats.diawell123.util.DiaWellNotificationHelper

/**
 * BroadcastReceiver triggered by Android AlarmManager when a scheduled medication reminder fires.
 */
class MedReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getIntExtra("med_id", (System.currentTimeMillis() % 10000).toInt())
        val medName = intent.getStringExtra("med_name") ?: "Medication"
        val medDose = intent.getStringExtra("med_dose") ?: ""

        Log.d("MedReminderReceiver", "Alarm triggered for medId=$medId, medName=$medName, dose=$medDose")

        val title = "💊 Time for your Medication!"
        val message = if (medDose.isNotBlank()) {
            "Please take your prescribed dose of $medName ($medDose) to maintain healthy blood glucose levels."
        } else {
            "Please take your prescribed dose of $medName now."
        }

        DiaWellNotificationHelper.showMedicationNotification(
            context = context,
            title = title,
            message = message,
            medId = medId
        )
    }
}

