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
        //get entitiy from database
        db = MainActivity.getDatabase(this)
        val reminderId = intent.getLongExtra("reminderId", 0)
//        reminder = db.reminderDao().getReminderById(reminderId)

        //set layout
        binding = ActivityWakeupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //set all audio files
        setAllAudioFiles()
        //set spinner to array from audioFiles
        val spinnerList = ArrayList<String>()
        //get audiofiles uri
        spinnerList += audioFiles.map { it.audioName }


    }

    private fun setAllAudioFiles() {
        val audioList = ArrayList<AudioFiles>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE,
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                val name = cursor.getString(1)
                val audioFiles = AudioFiles(path, name)
                Log.e("Path: $path", "Name: $name")
                audioList.add(audioFiles)
            }
            cursor.close()
        }

        val ringtoneManager = RingtoneManager(this)
        ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)

        val cursor2 = ringtoneManager.cursor
        if (cursor2 != null) {
            while (cursor2.moveToNext()) {
                val name = cursor2.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val path = cursor2.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor2.getString(
                    RingtoneManager.ID_COLUMN_INDEX)
                val audioFiles = AudioFiles(path, name)
                Log.e("Path: $path", "Name: $name")
                audioList.add(audioFiles)
            }
            cursor2.close()
        }
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setDataSource(this@WakeupActivity, Uri.parse(reminder.ringtonePath))
            isLooping = true
            prepare()
            start()
        }
        //set volume to max
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)
        //set screen to wake up
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //set button to stop alarm
//        binding.stopAlarm.setOnClickListener {
//            mediaPlayer.stop()
//            finish()
//        }
        audioFiles = audioList

    }

}
