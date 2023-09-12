package com.example.myapplication.services

import android.app.AlarmManager
import android.app.AlarmManager.OnAlarmListener
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import java.util.Calendar

const val NOTIFICATION_ID = "notificationID"
const val CHANNEL_ID = "Reminder"
const val TITLE_EXTRA = "title"
const val MESSAGE_EXTRA = "message"


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent? ) {

        Intent (context, AlarmService::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(context, 0, it,
                PendingIntent.FLAG_IMMUTABLE)
        }
        context.startActivity(Intent(context, AlarmService::class.java))
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.reminder_icon)
            .setContentTitle(intent?.getStringExtra(TITLE_EXTRA))
            .setContentText(intent?.getStringExtra(MESSAGE_EXTRA))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        createChannel(context)



        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(intent!!.getIntExtra(NOTIFICATION_ID,0), notification)

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

}