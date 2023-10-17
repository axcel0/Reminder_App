package com.example.myapplication.services

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.UI.WakeupActivity
import com.example.myapplication.databinding.ActivityWakeupBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity
import com.example.myapplication.utils.Constants

class AlarmService : Service() {
    private var alarmBinder = AlarmBinder()


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        //main activity code
//        val serviceIntent = Intent(this, AlarmService::class.java)
//        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
//        serviceIntent.putExtra(NOTIFICATION_ID, notificationId)
//        startService(serviceIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        val bundle = intent?.extras
        val notificationId = bundle?.getInt(Constants.NOTIFICATION_ID)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val wakeupIntent = Intent(this, WakeupActivity::class.java)
        val pendingIntent =
            notificationId?.let {
                PendingIntent.getActivity(this,
                    it, wakeupIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }

        if (pendingIntent != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
        }
        //toast notification id
        if (notificationId != null) {
            Toast.makeText(this, "Alarm $notificationId", Toast.LENGTH_SHORT).show()
        }
        // Perform background tasks here

        // If the system kills the service, restart it
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        // Clean up resources or perform any necessary cleanup here
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Service bound")
        // Return null because this service is not designed to bind to an activity
        return AlarmBinder()
    }
    inner class AlarmBinder : Binder() {
        fun getService(): AlarmService
        {
            return this@AlarmService
        }
    }


    companion object {
        private const val TAG = "MyBackgroundService"
    }
}