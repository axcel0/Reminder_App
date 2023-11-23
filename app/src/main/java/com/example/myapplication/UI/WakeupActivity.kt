package com.example.myapplication.UI

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityWakeupBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WakeupActivity : AppCompatActivity(){
    private var mediaPlayer: MediaPlayer? = null
    private var ringtonePath: String? = null
    private var notificationId: Int? = null
    private var title: String? = null
    private var message: String? = null
    private var time: Long? = null
    private var snoozeCounter: Int? = null

    private lateinit var binding: ActivityWakeupBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set onbackpressed dispatcher
        onBackPressedDispatcher.addCallback(this) {
            onSnoozeButtonClicked()
        }
        binding = ActivityWakeupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = MainActivity.getDatabase(this)
        getBundleExtras()

        requestDismissKeyguard()
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setSnoozeCounterVisibility()
        binding.snoozeButton.setOnClickListener{ onSnoozeButtonClicked() }
        binding.dismissButton.setOnClickListener{ onDismissButtonClicked() }

        playAudio(ringtonePath?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
    }

    private fun playAudio(audioUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    setDataSource(applicationContext, audioUri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e("WakeupActivity", "Error playing audio", e)
            }
        }
    }

    private fun snoozeAlarm(additionalTime: Long) {
        val totalTime = time?.plus(additionalTime)
        val notificationIntent = createNotificationIntent(totalTime)

        cancelVibration()

        if (notificationId != null && totalTime != null) {
            scheduleAlarm(notificationId!!, totalTime, notificationIntent)
        }

        Toast.makeText(this, "Snoozed for ${additionalTime / 60_000} minutes", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationIntent(totalTime: Long?): Intent {
        return Intent(this, AlarmReceiver::class.java).apply {
            putExtra(Constants.NOTIFICATION_ID, notificationId)
            putExtra(Constants.TITLE_EXTRA, title)
            putExtra(Constants.MESSAGE_EXTRA, message)
            putExtra(Constants.RINGTONE_PATH_EXTRA, ringtonePath)
            putExtra(Constants.TIME_EXTRA, totalTime)
            snoozeCounter?.let { putExtra(Constants.SNOOZE_COUNTER, it - 1) }
        }
    }

    private fun cancelVibration() {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator.cancel()
    }

    private fun scheduleAlarm(notificationId: Int, totalTime: Long, notificationIntent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, notificationId, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, totalTime, pendingIntent)
    }

    //make function to cancel pending intent when activity is destroyed or reminder has been deleted
    private fun cancelPendingIntent() {
        val notificationId = intent.getIntExtra(Constants.NOTIFICATION_ID, 0)
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
        vibratorManager.defaultVibrator.cancel()
        finish()
    }

    private fun setSnoozeCounterVisibility() {
        binding.snoozeButton.visibility = if (snoozeCounter == 0) View.GONE else View.VISIBLE
    }

    private fun onDismissButtonClicked() {
        mediaPlayer?.stop()
        cancelPendingIntent()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use the correct notification ID to cancel the notification
        notificationId?.let {
            notificationManager.cancel(it)
        }
        finish()
    }

    private fun onSnoozeButtonClicked() {
        mediaPlayer?.stop()
        snoozeAlarm(Constants.DEFAULT_SNOOZE_TIME)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        for (notification in notificationManager.activeNotifications) {
            Log.e("Notification ID: ${notification.id}", "Notification TAG: ${notification.tag}")
            if (notification.id == notificationId) {
                val builder = NotificationCompat.Builder(this, Constants.DEFAULT_CHANNEL_ID).apply {
                    setSmallIcon(R.mipmap.reminder_icon)
                    setContentTitle(title)
                    setContentText("Snooze: ${snoozeCounter?.minus(1)}")
                    priority = NotificationCompat.PRIORITY_HIGH
                    setCategory(NotificationCompat.CATEGORY_ALARM)
                    setAutoCancel(true)
                }
                notificationManager.notify(notification.id, builder.build())
            }
        }

        finish()
    }

    private fun getBundleExtras() {
        val bundle : Bundle? = intent.extras
        ringtonePath = bundle?.getString(Constants.RINGTONE_PATH_EXTRA)
        notificationId = bundle?.getInt(Constants.NOTIFICATION_ID)
        title = bundle?.getString(Constants.TITLE_EXTRA)
        message = bundle?.getString(Constants.MESSAGE_EXTRA)
        time = bundle?.getLong(Constants.TIME_EXTRA)
        snoozeCounter = bundle?.getInt(Constants.SNOOZE_COUNTER)
    }
    private fun requestDismissKeyguard() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop and release the media player if it's not null
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}