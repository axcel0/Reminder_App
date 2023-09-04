package com.example.myapplication.services

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.example.myapplication.R

//import com.example.myapplication.services.NotificationService
//TODO change notificationID to reminder ID

const val notificationID = 0
//set notificationID as reminder ID

const val CHANNEL_ID = "Reminder"
const val TITLE_EXTRA = "title"
const val MESSAGE_EXTRA = "message"


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        //set notification ID same as reminder ID so that it can be cancelled when reminder is deleted
//
//        val bundle : Bundle? = intent?.extras
//        val notificationID = bundle?.getLong("id")?.toInt() ?: 0

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.reminder_icon)
            .setContentTitle(intent?.getStringExtra(TITLE_EXTRA))
            .setContentText(intent?.getStringExtra(MESSAGE_EXTRA))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationID, notification)


    }

}