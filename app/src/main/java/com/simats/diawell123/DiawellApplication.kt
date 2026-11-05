package com.simats.diawell123

import android.app.Application
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.util.DiaWellNotificationHelper

class DiawellApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        DiaWellNotificationHelper.createNotificationChannels(this)
    }
}

