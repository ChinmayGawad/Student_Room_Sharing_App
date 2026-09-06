package com.pgshare.studentroomsharingapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.FirebaseDatabase

class StudentRoomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Restore user dark mode preference
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        if (prefs.contains("key_dark_mode")) {
            val isDark = prefs.getBoolean("key_dark_mode", false)
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FCMService.createNotificationChannels(this)
    }
}
