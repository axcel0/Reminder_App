package com.example.myapplication.services

import android.app.NotificationManager
import android.app.PendingIntent
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val wakeupIntent = Intent(context, WakeupActivity::class.java)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)


        showNotification(context, notificationId, intent)


        wakeupIntent.putExtra(NOTIFICATION_ID, notificationId)
        wakeupIntent.putExtra(TITLE_EXTRA, intent.getStringExtra(TITLE_EXTRA))
        wakeupIntent.putExtra(MESSAGE_EXTRA, intent.getStringExtra(MESSAGE_EXTRA))
        wakeupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(wakeupIntent)

    }
    private fun showNotification(context: Context, notificationId: Int, intent: Intent?) {
        val pendingIntent: PendingIntent = Intent(context, WakeupActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.reminder_icon)
            .setContentTitle(intent?.getStringExtra(TITLE_EXTRA))
            .setContentText(intent?.getStringExtra(MESSAGE_EXTRA))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

}