//package com.example.myapplication.services
//
//import android.app.*
//import android.content.Context
//import android.content.Intent
//import android.graphics.BitmapFactory
//import android.graphics.Color
//import android.media.RingtoneManager
//
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.example.myapplication.R
//import com.example.myapplication.UI.MainActivity
//import java.util.*
//
//class NotificationService : IntentService("NotificationService") {
//    private lateinit var mNotification: Notification
//    private val mNotificationId: Int = 1000
//
//    private fun createChannel() {
//        val context = this.applicationContext
//
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        val importance = NotificationManager.IMPORTANCE_HIGH
//        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
//        notificationChannel.enableVibration(true)
//        notificationChannel.setShowBadge(true)
//        notificationChannel.enableLights(true)
//        notificationChannel.lightColor = Color.parseColor("#e8334a")
//        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//        notificationManager.createNotificationChannel(notificationChannel)
//    }
//
//    companion object {
//        const val CHANNEL_ID = "com.example.myapplication.CHANNEL_ID"
//        const val CHANNEL_NAME = "Sample Notification"
//    }
//
//    @Deprecated("Deprecated in Java")
//    override fun onHandleIntent(intent: Intent?) {
//        // Create Channel
//        createChannel()
//
//        var timestamp: Long = 0
//        val title: String
//        var reminderBody: String? = null
//
//        intent?.extras?.let {
//            timestamp = it.getLong("timestamp")
//            reminderBody = it.getString("reminderBody")
//        }
//
//        if (timestamp > 0 && reminderBody != null) {
//            val context = this.applicationContext
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val notifyIntent = Intent(this, MainActivity::class.java)
//
//            title = if (Locale.getDefault().language == "en") {
//                "Reminder"
//            } else {
//                "Reminder"
//            }
//
//            notifyIntent.putExtra("title", title)
//            notifyIntent.putExtra("message", reminderBody)
//            notifyIntent.putExtra("notification", true)
//
//            notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//
//            val pendingIntent = PendingIntent.getActivity(
//                context,
//                0,
//                notifyIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )
//            val res = this.resources
//
//            val mNotification = NotificationCompat.Builder(this, CHANNEL_ID)
//                // Set the intent that will fire when the user taps the notification
//                .setContentIntent(pendingIntent)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
//                .setAutoCancel(true)
//                .setContentTitle(title)
//                .setStyle(
//                    NotificationCompat.BigTextStyle()
//                        .bigText(reminderBody)
//                )
//                .setContentText(reminderBody)
//                .build()
//
//            notificationManager.notify(mNotificationId, mNotification)
//
//        }
//    }
//}
