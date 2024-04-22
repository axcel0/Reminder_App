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
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityWakeupBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WakeupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WakeupFragment : Fragment() {
    private var mediaPlayer: MediaPlayer? = null
    private var ringtonePath: String? = null
    private var notificationId: Int? = null
    private var title: String? = null
    private var message: String? = null
    private var time: Long? = null
    private var snoozeCounter: Int? = null

    private lateinit var binding: ActivityWakeupBinding
    private lateinit var db: AppDatabase
    private lateinit var vibrator: Vibrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityWakeupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = MainActivity.getDatabase(requireContext())
        getBundleExtras()

        requestDismissKeyguard()
        requireActivity().setShowWhenLocked(true)
        requireActivity().setTurnScreenOn(true)

        setSnoozeCounterVisibility()
        binding.snoozeButton.setOnClickListener{ onSnoozeButtonClicked() }
        binding.dismissButton.setOnClickListener{ onDismissButtonClicked() }

        //bind alarm title to my alarm name
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.alarmTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.alarmMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.alarmTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.alarmMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        binding.alarmTitle.text = title
        binding.alarmMessage.text = message

        playAudio(ringtonePath?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        startVibration()
    }

    private fun createNotificationIntent(totalTime: Long?): Intent {
        return Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra(Constants.NOTIFICATION_ID, notificationId)
            putExtra(Constants.TITLE_EXTRA, title)
            putExtra(Constants.MESSAGE_EXTRA, message)
            putExtra(Constants.RINGTONE_PATH_EXTRA, ringtonePath)
            putExtra(Constants.TIME_EXTRA, totalTime)
            snoozeCounter?.let { putExtra(Constants.SNOOZE_COUNTER, it - 1) }
        }
    }

    private fun cancelVibration() {
        vibrator.cancel()
    }

    private fun scheduleAlarm(notificationId: Int, totalTime: Long, notificationIntent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(requireContext().applicationContext, notificationId, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, totalTime, pendingIntent)
        startVibration()
    }

    private fun cancelPendingIntent() {
        val notificationId = arguments?.getInt(Constants.NOTIFICATION_ID, 0)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), notificationId!!, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
        cancelVibration()
    }

    private fun setSnoozeCounterVisibility() {
        binding.snoozeButton.visibility = if (snoozeCounter == 0) View.GONE else View.VISIBLE
    }

    private fun onDismissButtonClicked() {
        mediaPlayer?.stop()
        cancelPendingIntent()

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use the correct notification ID to cancel the notification
        notificationId?.let {
            notificationManager.cancel(it)
        }
    }

    private fun getBundleExtras() {
        val bundle : Bundle? = arguments
        title = bundle?.getString(Constants.TITLE_EXTRA)
        message = bundle?.getString(Constants.MESSAGE_EXTRA)
        ringtonePath = bundle?.getString(Constants.RINGTONE_PATH_EXTRA)
        time = bundle?.getLong(Constants.TIME_EXTRA)
        snoozeCounter = bundle?.getInt(Constants.SNOOZE_COUNTER)
        notificationId = bundle?.getInt(Constants.NOTIFICATION_ID)
    }

    private fun requestDismissKeyguard() {
        val keyguardManager = requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(requireActivity(), null)
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

    private fun startVibration() {
        val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(
            //loop vibration pattern
            VibrationEffect.createWaveform(
                longArrayOf(0, 1000, 500, 1000, 500, 1000, 500, 1000, 500),
                //repeat at index 0
                0
            )
        )
    }
    private fun snoozeAlarm() {
        val totalTime = time?.plus(Constants.DEFAULT_SNOOZE_TIME)
        val notificationIntent = createNotificationIntent(totalTime)

        cancelVibration()

        if (notificationId != null && totalTime != null) {
            scheduleAlarm(notificationId!!, totalTime, notificationIntent)
        }
        Toast.makeText(requireContext(), "Alarm snoozed", Toast.LENGTH_SHORT).show()
    }

    private fun onSnoozeButtonClicked() {
        mediaPlayer?.stop()
        snoozeAlarm()
        vibrator.cancel() // Cancel the vibration when snoozing the alarm

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        for (notification in notificationManager.activeNotifications) {
            Log.e("Notification ID: ${notification.id}", "Notification TAG: ${notification.tag}")
            if (notification.id == notificationId) {
                val builder = NotificationCompat.Builder(requireContext().applicationContext, Constants.DEFAULT_CHANNEL_ID).apply {
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

        requireActivity().finish()
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
                    setDataSource(requireContext().applicationContext, audioUri)
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
}
