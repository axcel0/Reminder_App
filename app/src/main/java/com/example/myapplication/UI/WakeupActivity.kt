package com.example.myapplication.UI

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.AlarmClock
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.UI.EditActivity.Companion.audioFiles
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.databinding.ActivityWakeupBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.AudioFiles
import com.example.myapplication.models.entities.ReminderEntity
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.services.NOTIFICATION_ID
import kotlin.math.log

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
        //make array for spinner to be filled with data from local storage alarm sounds
        binding = ActivityWakeupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //bundle data ringtonePath from reminder
        val bundle : Bundle? = intent.extras
        val ringtonePath = bundle?.getString("ringtonePath")

//        playAudio((RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)))
        ringtonePath ?: playAudio(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        ringtonePath?.let { playAudio(Uri.parse(it)) }

        //set onclick listener for dismiss button
        binding.dismissButton.setOnClickListener {
            //finish the activity
            finish().also {
                //stop the alarm
                mediaPlayer?.stop()
                //cancel the notification
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationManager.activeNotifications[0].id)
                //cancel the alarm
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                alarmManager.cancel(pendingIntent)
                //stop vibrator
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.cancel()
            }
        }
    }
    private fun playAudio(audioUri: Uri) {
        //make media player
        mediaPlayer = MediaPlayer().apply {
            //set audio attributes
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            //set audioPath from selected ringtone from spinner
            setDataSource(this@WakeupActivity, audioUri)
            //set audio duration
            setOnPreparedListener {
                it.start()
                isLooping = true
                setVolume(1.0f, 1.0f)
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
                //set screen to be on
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                //add vibration
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(
                    //loop vibration pattern
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 500, 1000, 500, 1000, 500, 1000, 500), 0
                    )
                )
            }.run {
                prepare()
            }

        }
    }

}
