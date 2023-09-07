package com.example.myapplication.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.util.Calendar

class AlarmService{
    //make alarm service
    fun setAlarm(context: Context, calendar: Calendar, id: Long, reminderBody: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("id", id)
        intent.putExtra("reminderBody", reminderBody)
        val pendingIntent = PendingIntent.getBroadcast(context, id.toInt(), intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
    //cancel alarm service
    fun cancelAlarm(context: Context, id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, id.toInt(), intent, 0)
        alarmManager.cancel(pendingIntent)
    }

}