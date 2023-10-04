package com.example.myapplication.UI

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.AlarmClock
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityWakeupBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.AudioFiles
import com.example.myapplication.models.entities.ReminderEntity
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.services.NOTIFICATION_ID

class WakeupActivity : AppCompatActivity(){
    //binding for the activity layout
    private lateinit var binding: ActivityWakeupBinding
    //database instance
    private lateinit var db: AppDatabase
    //reminder entity
    private lateinit var reminder: ReminderEntity
    //media player
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        var audioFiles = ArrayList<AudioFiles>()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

}
