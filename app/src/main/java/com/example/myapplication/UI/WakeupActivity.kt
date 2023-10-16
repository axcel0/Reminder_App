package com.example.myapplication.UI

import android.app.AlarmManager
import android.app.KeyguardManager
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
import com.example.myapplication.services.MESSAGE_EXTRA
import com.example.myapplication.services.NOTIFICATION_ID
import com.example.myapplication.services.TITLE_EXTRA
import java.util.Calendar
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

    private var ringtonePath: String? = null
    private var notificationId: Int? = null
    private var title: String? = null
    private var message: String? = null
    private var time: Long? = null
    private var snoozeCounter: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = MainActivity.getDatabase(this)
        //make array for spinner to be filled with data from local storage alarm sounds
        binding = ActivityWakeupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //bundle data ringtonePath from reminder
        val bundle : Bundle? = intent.extras
        ringtonePath = bundle?.getString("ringtonePath")
        notificationId = bundle?.getInt(NOTIFICATION_ID)
        title = bundle?.getString("title")
        message = bundle?.getString("message")
        time = bundle?.getLong("time")
        snoozeCounter = bundle?.getInt("snoozeCounter")


        playAudio(ringtonePath?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
        //set onclick listener for dismiss button
        binding.dismissButton.setOnClickListener {
            //finish the activity
            finish().also {

                val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
                //stop the alarm
                mediaPlayer?.stop()
                //cancel the notification
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.activeNotifications[0]?.let {
                    notificationManager.cancel(notificationManager.activeNotifications[0].id)
                    Log.e("Notification ID: ${it.id}", "Notification TAG: ${it.tag}")
                }
                cancelPendingIntent()

            }
        }
        //set onclick listener for snooze button
        if(snoozeCounter == 0){
            binding.snoozeButton.visibility = View.GONE
            //cancel pendingIntent if snooze counter is 0
            cancelPendingIntent()
        }
        else{
            binding.snoozeButton.visibility = View.VISIBLE
        }
        binding.snoozeButton.setOnClickListener {
            //finish the activity
            mediaPlayer?.stop()
            //cancel the notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationManager.activeNotifications[0].id)

            //snooze the alarm
            snoozeAlarm(300000).also { finish() }

        }
    }
    private fun playAudio(audioUri: Uri) {
        //make media player
        mediaPlayer = MediaPlayer().apply {
            //set audio attributes
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
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
    private fun snoozeAlarm(additionalTime: Long){
        val totalTime = time?.plus(additionalTime)
        Toast.makeText(this, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
        val notificationIntent = Intent(this, AlarmReceiver::class.java)
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.cancel()
        notificationIntent.putExtra(NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(TITLE_EXTRA, title)
        notificationIntent.putExtra(MESSAGE_EXTRA, message)
        notificationIntent.putExtra("ringtonePath", ringtonePath)
        notificationIntent.putExtra("time", totalTime)
        notificationIntent.putExtra("snoozeCounter", snoozeCounter?.minus(1))

        val pendingIntent = notificationId?.let {
            PendingIntent.getBroadcast(applicationContext,
                it, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (pendingIntent != null && totalTime != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, totalTime, pendingIntent)
        }
    }
    //make function to cancel pending intent when activity is destroyed or reminder has been deleted
    private fun cancelPendingIntent(){
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(pendingIntent).also {
                vibrator.cancel()
                finish()
            }

    }


}
