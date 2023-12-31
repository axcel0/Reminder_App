package com.example.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.myapplication.utils.Constants

class ReminderApp: Application(){
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            Constants.DEFAULT_CHANNEL_ID,
            "Reminder",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)


    }
}