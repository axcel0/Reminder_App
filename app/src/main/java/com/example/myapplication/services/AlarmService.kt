package com.example.myapplication.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.util.Calendar

class AlarmService: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent? ) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        alarm.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis, pendingIntent)
        createChannel(context)
    }

    private fun createChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reminder",
            NotificationManager.IMPORTANCE_DEFAULT

        )
        notificationManager.createNotificationChannel(channel)
    }
    fun alarmIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmService::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}