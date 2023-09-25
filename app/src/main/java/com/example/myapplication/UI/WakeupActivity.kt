package com.example.myapplication.UI

import android.annotation.SuppressLint
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
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityWakeupBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.services.NOTIFICATION_ID

class WakeupActivity : AppCompatActivity(){
    companion object {
        private const val MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS = 1
        private const val INCREASE_VOLUME_DELAY = 300L
    }

    private val increaseVolumeHandler = Handler(Looper.getMainLooper())
    private val maxReminderDurationHandler = Handler(Looper.getMainLooper())
    private val swipeGuideFadeHandler = Handler()
    private val vibrationHandler = Handler(Looper.getMainLooper())
    private var isAlarmReminder = false
    private var didVibrate = false
    private var wasAlarmSnoozed = false
    private var reminder: ReminderEntity? = null
//    private var alarm: Alarm? = null
    private var audioManager: AudioManager? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var initialAlarmVolume: Int? = null
    private var dragDownX = 0f
    private lateinit var binding: ActivityWakeupBinding
//    private val binding: WakeupActivityBinding by viewBinding(WakeupActivityBinding::inflate)
    private var finished = false
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
//        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wakeup)
        binding = ActivityWakeupBinding.inflate(layoutInflater)
        showOverLockscreen()
        db = MainActivity.getDatabase(this)

//        updateTextColors(binding.root)
//        updateStatusbarColor(getProperBackgroundColor())

        val id = intent.getIntExtra(NOTIFICATION_ID, -1)
        isAlarmReminder = id != -1
        if (id != -1) {
            reminder = db.reminderDao().getReminder(id)
        }

        val label = if (isAlarmReminder) {
            reminder!!.reminderName.ifEmpty {
                "Saya Alarm"
            }
        } else {
            "Saya Timer"
        }

        binding.reminderTitle.text = "Sek yang lalu"
//        binding.reminderText.text = if (isAlarmReminder) getFormattedTime(getPassedSeconds(), false, false) else getString(R.string.time_expired)

//        val maxDuration = if (isAlarmReminder) config.alarmMaxReminderSecs else config.timerMaxReminderSecs
        maxReminderDurationHandler.postDelayed({
            finishActivity()
            // Sementara sudah benar yang diatas ini yang salah karena tidak bisa dijalankan di background dan tidak bisa dijalankan di foreground
//        }, maxDuration * 1000L)
        }, 1000 * 1000L)

        setupButtons()
        setupEffects()
    }

    private fun setupButtons() {
        if (isAlarmReminder) {
            setupAlarmButtons()
        } else {
            setupTimerButtons()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAlarmButtons() {
        //make stop button
        binding.reminderStop.setOnClickListener {
            finishActivity()
        }
    }

    private fun setupTimerButtons() {
//        binding.reminderStop.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, getProperPrimaryColor())
        arrayOf(binding.reminderSnooze, binding.reminderDraggableBackground, binding.reminderDraggable, binding.reminderDismiss).forEach {
//            it.beGone()
        }

        binding.reminderStop.setOnClickListener {
            finishActivity()
        }
    }

    private fun setupEffects() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initialAlarmVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0

            val pattern = LongArray(2) { 500 }
            vibrationHandler.postDelayed({
                val vibratorManager = this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator;
                vibrator.vibrate(pattern, 0)

            }, 500)
        //get sound uri from selected song
        val soundUri = Uri.parse("android.resource://com.example.myapplication/raw/reminder_sound")


//        if (soundUri != SILENT) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    //set content uri from audio selected
                    setDataSource(this@WakeupActivity, Uri.parse("android.resource://com.example.myapplication/raw/reminder_sound"))

                    isLooping = true
                    prepare()
                    start()
                }

//                if (config.increaseVolumeGradually) {
                    scheduleVolumeIncrease(MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS.toFloat(), initialAlarmVolume!!.toFloat(), 0)
//                }
            } catch (e: Exception) {
            }
//        }
    }

    private fun scheduleVolumeIncrease(lastVolume: Float, maxVolume: Float, delay: Long) {
        increaseVolumeHandler.postDelayed({
            val newLastVolume = (lastVolume + 0.1f).coerceAtMost(maxVolume)
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, newLastVolume.toInt(), 0)
            scheduleVolumeIncrease(newLastVolume, maxVolume, INCREASE_VOLUME_DELAY)
        }, delay)
    }

    private fun resetVolumeToInitialValue() {
        initialAlarmVolume?.apply {
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, this, 0)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == AlarmClock.ACTION_SNOOZE_ALARM) {
            val durationMinutes = intent.getIntExtra(AlarmClock.EXTRA_ALARM_SNOOZE_DURATION, -1)
            if (durationMinutes == -1) {
                snoozeAlarm()
            } else {
                snoozeAlarm(durationMinutes)
            }
        } else {
            finishActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        increaseVolumeHandler.removeCallbacksAndMessages(null)
        maxReminderDurationHandler.removeCallbacksAndMessages(null)
        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
        vibrationHandler.removeCallbacksAndMessages(null)
        if (!finished) {
            finishActivity()
//            notificationManager.cancel(ALARM_NOTIF_ID)
        } else {
            destroyEffects()
        }
    }

    private fun destroyEffects() {
        if (isAlarmReminder) {
            resetVolumeToInitialValue()
        }

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun snoozeAlarm(overrideSnoozeDuration: Int? = null) {
        destroyEffects()
        if (overrideSnoozeDuration != null) {
//            setupAlarmClock(alarm!!, overrideSnoozeDuration * MINUTE_SECONDS)
            wasAlarmSnoozed = true
            finishActivity()
//        } else if (config.useSameSnooze) {
//            setupAlarmClock(alarm!!, config.snoozeTime * MINUTE_SECONDS)
//            wasAlarmSnoozed = true
//            finishActivity()
        } else {
//            showPickSecondsDialog(config.snoozeTime * MINUTE_SECONDS, true, cancelCallback = { finishActivity() }) {
//                config.snoozeTime = it / MINUTE_SECONDS
//                setupAlarmClock(alarm!!, it)
//                wasAlarmSnoozed = true
//                finishActivity()
//            }
        }
    }

    private fun finishActivity() {
        if (!wasAlarmSnoozed && reminder != null) {
            cancelAlarmClock(reminder!!)
        }
//        if (!wasAlarmSnoozed && alarm != null) {
//            cancelAlarmClock(alarm!!)
//            if (alarm!!.days > 0) {
//                scheduleNextAlarm(alarm!!, false)
//            }
//            if (alarm!!.days < 0) {
//                if (alarm!!.oneShot) {
//                    alarm!!.isEnabled = false
//                    dbHelper.deleteAlarms(arrayListOf(alarm!!))
//                } else {
//                    dbHelper.updateAlarmEnabledState(alarm!!.id, false)
//                }
//                updateWidgets()
//            }
//        }

        finished = true
        destroyEffects()
        finish()
        overridePendingTransition(0, 0)
    }
    private fun cancelAlarmClock(reminderEntity: ReminderEntity) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getAlarmIntent(reminderEntity))
//        alarmManager.cancel(getEarlyAlarmDismissalIntent(reminderEntity))
    }

    private fun getAlarmIntent(reminderEntity: ReminderEntity): PendingIntent {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra(NOTIFICATION_ID, reminderEntity.id)
        return PendingIntent.getBroadcast(this, reminderEntity.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun showOverLockscreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

        )

            setShowWhenLocked(true)
            setTurnScreenOn(true)

    }
}
