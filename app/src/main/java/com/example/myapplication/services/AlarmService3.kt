package com.example.myapplication.services

//class AlarmService: BroadcastReceiver(){
//
//    fun createChannel(context: Context) {
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val channel = NotificationChannel(
//            CHANNEL_ID,
//            "Reminder",
//            NotificationManager.IMPORTANCE_DEFAULT
//
//        )
//        notificationManager.createNotificationChannel(channel)
//    }
//    private fun alarmIntent(context: Context): PendingIntent {
//        val intent = Intent(context, AlarmService::class.java)
//        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//    }
//}