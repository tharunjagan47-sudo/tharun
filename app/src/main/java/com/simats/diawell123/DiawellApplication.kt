package com.simats.diawell123

import android.app.Application
import com.simats.diawell123.data.local.SessionManager

class DiawellApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}

