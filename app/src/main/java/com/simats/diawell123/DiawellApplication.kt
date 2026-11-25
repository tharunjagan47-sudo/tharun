package com.simats.diawell123

import android.app.Application
import android.util.Log
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.util.DiaWellNotificationHelper

class DiawellApplication : Application() {
    companion object {
        lateinit var instance: DiawellApplication
            private set
        var hasShownSplash: Boolean = false
        var currentScreen: String = "home"
        var backStack: List<String> = emptyList()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Global uncaught exception safeguard across all worker threads
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("DiawellApp", "Uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            SessionManager.init(this)
            com.simats.diawell123.ui.theme.ThemeManager.init(this)
            DiaWellNotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            Log.e("DiawellApp", "Error during DiawellApplication onCreate", e)
        }
    }
}

