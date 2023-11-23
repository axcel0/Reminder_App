package com.example.myapplication.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.UI.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FirebaseService", "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    class MyReminderReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, "myChannelId")
                .setContentTitle("My Notification")
                .setContentText("This is my notification")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
            notificationManager.notify(0, notification)
        }
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
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        Log.d("FirebaseService", "showNotification.defaultSoundUri: $defaultSoundUri")


        val notification = Notification.Builder(this, "channelId")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        Log.d("FirebaseService", "showNotification.notificationBuilder: $notification")

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
        Log.d("FirebaseService", "showNotification.notificationManager: $notificationManager")
    }

    private fun sendRegistrationToServer(token: String) {
        val preferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("deviceToken", token)
        editor.apply()
        Log.d("FirebaseService", "sendRegistrationToServer: $token")
    }
}