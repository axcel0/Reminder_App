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
            putExtra(Constants.NOTIFICATION_ID, intent.getIntExtra(Constants.NOTIFICATION_ID, 0))
            putExtra(Constants.TITLE_EXTRA, intent.getStringExtra(Constants.TITLE_EXTRA))
            putExtra(Constants.MESSAGE_EXTRA, intent.getStringExtra(Constants.MESSAGE_EXTRA))
            putExtra(Constants.RINGTONE_PATH_EXTRA, intent.getStringExtra(Constants.RINGTONE_PATH_EXTRA))
            putExtra(Constants.TIME_EXTRA, intent.getLongExtra(Constants.TIME_EXTRA, 0))
            putExtra(Constants.SNOOZE_COUNTER, intent.getIntExtra(Constants.SNOOZE_COUNTER, 0))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(wakeupIntent)
    }

    private fun showNotification(context: Context, notificationId: Int, intent: Intent?) {
        val pendingIntent = Intent(context, WakeupActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(context, notificationId, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(context, Constants.DEFAULT_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.reminder_icon)
            setContentTitle(intent?.getStringExtra(Constants.TITLE_EXTRA))
            setContentText(intent?.getStringExtra(Constants.MESSAGE_EXTRA))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, builder.build())
    }
}