package com.example.myapplication.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.UI.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("bapakmu", "Refreshed token: $token")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("FirebaseService.onMessageReceived: $remoteMessage")

        if (remoteMessage.notification == null) return

        val body = remoteMessage.notification?.body
        val title = remoteMessage.notification?.title

        showNotification(title, body)
    }

    private fun showNotification(title: String?, body: String?) {
        Log.d("FirebaseService", "showNotification: $title, $body")

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Log.d("FirebaseService", "showNotification.intent: $intent")

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("FirebaseService", "showNotification.pendingIntent: $pendingIntent")

        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.reminder_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        Log.d("FirebaseService", "showNotification.notificationBuilder: $notificationBuilder")


        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        Log.d("FirebaseService", "showNotification.notificationManager: $notificationManager")
        notificationManager.notify(0, notificationBuilder.build())

    }


}