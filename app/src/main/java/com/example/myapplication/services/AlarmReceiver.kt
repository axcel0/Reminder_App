package com.example.myapplication.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.UI.WakeupActivity


const val NOTIFICATION_ID = "notificationID"
const val CHANNEL_ID = "Reminder"
const val TITLE_EXTRA = "title"
const val MESSAGE_EXTRA = "message"


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.reminder_icon)
            .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
            .setContentText(intent.getStringExtra(MESSAGE_EXTRA))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        Intent(context, WakeupActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(NOTIFICATION_ID, notificationId)
            context.startActivity(this)
        }
    }

    fun cancel(notificationId: String) {
            val intent = Intent("cancelAlarm")
            intent.putExtra(NOTIFICATION_ID, notificationId)

        }


}