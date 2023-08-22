package com.example.myapplication.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.UI.MainActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

class FirebaseService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("bapakmu", "Refreshed token: $token")
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


//    val tokenStored = preferences.getString("deviceToken", "")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("FirebaseService.onMessageReceived: $remoteMessage")

        if (remoteMessage.notification == null) return

        val body = remoteMessage.notification?.body
        val title = remoteMessage.notification?.title

        showNotification(title, body)
    }
//    private fun scheduleAlarm(
//        scheduledTimeString: String?,
//        title: String?,
//        message: String?
//    ) {
//        val alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val alarmIntent =
//            Intent(applicationContext, NotificationBroadcastReceiver::class.java).let { intent ->
//                intent.putExtra(NOTIFICATION_TITLE, title)
//                intent.putExtra(NOTIFICATION_MESSAGE, message)
//                PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
//            }
//
//        // Parse Schedule time
//        val scheduledTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            .parse(scheduledTimeString!!)
//
//        scheduledTime?.let {
//            // With set(), it'll set non repeating one time alarm.
//            alarmMgr.set(
//                AlarmManager.RTC_WAKEUP,
//                it.time,
//                alarmIntent
//            )
//        }
//    }
//
//    private fun showNotification(title: String, message: String) {
//        NotificationUtil(applicationContext).showNotification(title, message)
//    }

    private fun showNotification(title: String?, body: String?) {
        Log.d("FirebaseService", "showNotification: $title, $body")
        val channelId = "com.example.myapplication"
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


        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        Log.d("FirebaseService", "showNotification.notificationBuilder: $notificationBuilder")


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("FirebaseService", "showNotification.notificationManager: $notificationManager")
        notificationManager.notify(0, notificationBuilder.build())


    }
   //make function to send token to server
    private fun sendRegistrationToServer(token: String) {
        val preferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("deviceToken", token)
        editor.apply()
        Log.d("FirebaseService", "sendRegistrationToServer: $token")
   }


}