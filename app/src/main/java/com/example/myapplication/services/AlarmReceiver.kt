package com.example.myapplication.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.UI.WakeupActivity
import com.example.myapplication.utils.Constants

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        showNotification(context, intent.getIntExtra(Constants.NOTIFICATION_ID, 0), intent)

        val wakeupIntent = Intent(context, WakeupActivity::class.java).apply {
            putExtras(intent) // Pass all extras from the received intent
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(wakeupIntent)
    }

    private fun showNotification(context: Context, notificationId: Int, intent: Intent) {
        val pendingIntent = Intent(context, WakeupActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(context, notificationId, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = createNotificationBuilder(context, intent, pendingIntent)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationBuilder(context: Context, intent: Intent, pendingIntent: PendingIntent): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Constants.DEFAULT_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.reminder_icon)
            setContentTitle(intent.getStringExtra(Constants.TITLE_EXTRA))
            setContentText(intent.getStringExtra(Constants.MESSAGE_EXTRA))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }
    }
}