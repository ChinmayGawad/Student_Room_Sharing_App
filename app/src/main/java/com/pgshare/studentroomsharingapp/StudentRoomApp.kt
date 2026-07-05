package com.pgshare.studentroomsharingapp

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class StudentRoomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FCMService.createNotificationChannels(this)
    }
}
