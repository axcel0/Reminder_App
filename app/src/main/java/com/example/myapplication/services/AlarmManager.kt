package com.example.myapplication.services

import java.util.Calendar

class AlarmManager {
    private fun startAlarm() {

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
        }
//        setAlarm(calendar.timeInMillis)
    }

//    private fun setAlarm(timeInMillis: Long) {
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(this, MyAlarm::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
//        alarmManager.setRepeating(
//            AlarmManager.RTC,
//            timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            pendingIntent
//        )
//        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show()
//    }
//    private class MyAlarm : BroadcastReceiver() {
//        override fun onReceive(
//            context: Context,
//            intent: Intent
//        ) {
//            Log.d("Alarm Bell", "Alarm just fired")
//        }
//    }
}