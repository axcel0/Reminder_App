package com.example.myapplication.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.util.Calendar

class AlarmService{

    //set alarm
    fun setAlarm(context: Context, calendar: Calendar, id: Long, reminderBody: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("id", id)
        intent.putExtra("reminderBody", reminderBody)
        val pendingIntent = PendingIntent.getBroadcast(context, id.toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
    //cancel alarm
    fun cancelAlarm(context: Context, id: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, id.toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

    }

}