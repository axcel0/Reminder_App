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
import com.example.myapplication.R
import com.example.myapplication.UI.WakeupActivity
import com.example.myapplication.databinding.ActivityWakeupBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity

class AlarmService : Service() {
    private var alarmBinder = AlarmBinder()


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(this, WakeupActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
//
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

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