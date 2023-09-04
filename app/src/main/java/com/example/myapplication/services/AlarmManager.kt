package com.example.myapplication.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class AlarmManager{

//    private fun setAlarm(context: Context, calendar: Calendar, id: Long, reminderBody: String) {
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(context, AlarmReceiver::class.java)
//        intent.putExtra("id", id)
//        intent.putExtra("reminderBody", reminderBody)
//        intent.putExtra("timestamp", calendar.timeInMillis)
//        intent.putExtra("reason", "notification")
//        val pendingIntent = PendingIntent.getBroadcast(context, id.toInt(), intent, 0)
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//    }
//
//    private fun cancelAlarm(context: Context, id: Long) {
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(context, AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(context, id.toInt(), intent, 0)
//        alarmManager.cancel(pendingIntent)
//    }

}