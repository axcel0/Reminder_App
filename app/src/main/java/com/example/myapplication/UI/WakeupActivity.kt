package com.example.myapplication.UI

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import androidx.core.content.ContextCompat
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

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.alarmTitle.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.alarmMessage.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.alarmTitle.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.alarmMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
        binding.alarmTitle.text = title
        binding.alarmMessage.text = message


        playAudio(ringtonePath?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        startVibration()
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
                    setVolume(1.0f, 1.0f)
                    startVibration()
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e("WakeupActivity", "Error playing audio", e)
            }
        }
    }

    private fun snoozeAlarm() {
        val totalTime = time?.plus(Constants.DEFAULT_SNOOZE_TIME)
        val notificationIntent = createNotificationIntent(totalTime)

        cancelVibration()

        if (notificationId != null && totalTime != null) {
            scheduleAlarm(notificationId!!, totalTime, notificationIntent)
        }
        Toast.makeText(this, "Snoozed for ${Constants.DEFAULT_SNOOZE_TIME / 60_000} minutes", Toast.LENGTH_SHORT).show()
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
    private var vibrator: Vibrator? = null
    private fun cancelVibration() {
        vibrator?.cancel()
    }
    private fun startVibration() {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibrator = vibratorManager.defaultVibrator
        vibrator?.vibrate(
            //loop vibration pattern
            VibrationEffect.createWaveform(
                longArrayOf(0, 1000, 500, 1000, 500, 1000, 500, 1000, 500),
                //repeat at index 0
                0
            )
        )
    }

    private fun scheduleAlarm(notificationId: Int, totalTime: Long, notificationIntent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, notificationId, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, totalTime, pendingIntent)
        startVibration()
    }

    private fun cancelPendingIntent() {
        val notificationId = intent.getIntExtra(Constants.NOTIFICATION_ID, 0)
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
        vibrator?.cancel()
        finish()
    }

    private fun setSnoozeCounterVisibility() {
        binding.snoozeButton.visibility = if (snoozeCounter == 0) View.GONE else View.VISIBLE
    }

    private fun onDismissButtonClicked() {
        mediaPlayer?.stop()
        cancelPendingIntent()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationId?.let {
            notificationManager.cancel(it)
        }
        finish()
    }

    private fun onSnoozeButtonClicked() {
        mediaPlayer?.stop()
        snoozeAlarm()

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

        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}