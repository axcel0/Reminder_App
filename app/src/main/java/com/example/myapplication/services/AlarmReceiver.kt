package com.example.myapplication.services

import android.app.AlarmManager
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

        showNotification(context, intent.getIntExtra(NOTIFICATION_ID, 0), intent)

        val wakeupIntent = Intent(context, WakeupActivity::class.java).apply {
            putExtra(NOTIFICATION_ID, intent.getIntExtra(NOTIFICATION_ID, 0))
            putExtra(TITLE_EXTRA, intent.getStringExtra(TITLE_EXTRA))
            putExtra(MESSAGE_EXTRA, intent.getStringExtra(MESSAGE_EXTRA))
            putExtra("ringtonePath", intent.getStringExtra("ringtonePath"))
            putExtra("time", intent.getLongExtra("time", 0))
            putExtra("snoozeCounter", intent.getIntExtra("snoozeCounter", 0))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(wakeupIntent)
    }

    private fun showNotification(context: Context, notificationId: Int, intent: Intent?) {
        val pendingIntent = Intent(context, WakeupActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(context, notificationId, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.reminder_icon)
            setContentTitle(intent?.getStringExtra(TITLE_EXTRA))
            setContentText(intent?.getStringExtra(MESSAGE_EXTRA))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, builder.build())
    }


}