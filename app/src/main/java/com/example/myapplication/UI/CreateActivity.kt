package com.example.myapplication.UI

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.AudioFiles
import com.example.myapplication.models.entities.ReminderEntity
import java.time.LocalDateTime
import java.time.ZoneId
import android.media.RingtoneManager
import android.view.GestureDetector
import androidx.activity.addCallback
import androidx.core.app.NotificationCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.R
import com.example.myapplication.utils.Constants
import java.time.format.DateTimeFormatter
import android.view.MotionEvent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


class CreateActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var isFirstInit = true
    private var audioFiles = ArrayList<AudioFiles>()
    private var currentMode : Mode = Mode.CREATE
    private var oldTitleName : String? = null
    private var ringtonePath: String? = null
    private var notificationId: Int? = null
    private var title: String? = null
    private var message: String? = null
    private var time: Long? = null
    private var snoozeCounter: Int? = null

    private lateinit var binding: ActivityCreateBinding
    private lateinit var db: AppDatabase
    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
    private enum class Mode {
        CREATE, EDIT
    }
    private val gestureDetector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val deltaX = e1?.x?.let { e2.x - it } ?: 0f
                val deltaY = e1?.y?.let { e2.y - it } ?: 0f
                if (abs(deltaX) > abs(deltaY) && abs(deltaX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    onBackPressedDispatcher.onBackPressed()
                    return true
                }
                return false
            }
        })
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
    private fun backPressed() {
        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@CreateActivity, MainActivity::class.java)
            startActivity(intent).also { finish() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backPressed()
        binding = ActivityCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0f, 0f, 0))

        // Get the database
        db = MainActivity.getDatabase(this@CreateActivity)

        // Get the audio files
        audioFiles = getAudioFiles()
        val spinnerList = ArrayList<String>()
        spinnerList += audioFiles.map { it.audioName }
        setDateTimePicker()
        // Set the spinner adapter
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerList)
        binding.spinner.adapter = spinnerAdapter

        // Set the date picker and time picker
        binding.datePicker.minDate = System.currentTimeMillis().also { binding.timePicker.setIs24HourView(true) }

        // Set Event Listeners
        binding.title.addTextChangedListener {onTitleChanged() }
        binding.cancelButton.setOnClickListener{ onCancelButtonClicked() }
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isFirstInit) {
                    onSpinnerItemSelected()
                }
                isFirstInit = false
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if necessary
            }
        }

        // Determine the current mode of the activity
        currentMode = if (intent.hasExtra(Constants.REMINDER_ID_EXTRA)) Mode.EDIT else Mode.CREATE
        when (currentMode) {
            Mode.CREATE -> {
                binding.saveButton.isEnabled = false
                binding.saveButton.setOnClickListener {
                    onCreateSaveButtonClicked()
                }
            }

            Mode.EDIT -> {
                intent.extras?.let { bundle ->
                    val id = bundle.getLong(Constants.REMINDER_ID_EXTRA)
                    val name = bundle.getString(Constants.REMINDER_NAME_EXTRA)
                    val dateTime = bundle.getLong(Constants.REMINDER_DATE_EXTRA)
                    val ringtonePath = bundle.getString(Constants.REMINDER_RINGTONE_PATH_EXTRA)

                    oldTitleName = name

                    binding.title.setText(name)

                    binding.datePicker.minDate = System.currentTimeMillis().also {
                        binding.timePicker.setIs24HourView(true)
                    }

                    val currentTimeMillis = System.currentTimeMillis() / 1000
                    binding.timePicker.hour = LocalDateTime.ofEpochSecond(
                        if (dateTime < currentTimeMillis) currentTimeMillis else dateTime, 0,
                        ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
                    ).hour

                    binding.timePicker.minute = LocalDateTime.ofEpochSecond(
                        if (dateTime < currentTimeMillis) currentTimeMillis else dateTime, 0,
                        ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
                    ).minute

                    val ringtoneName = audioFiles.find { it.path == ringtonePath }?.audioName
                    val spinnerPosition = spinnerAdapter.getPosition(ringtoneName)
                    binding.spinner.setSelection(spinnerPosition)

                    binding.saveButton.setOnClickListener { onEditSaveButtonClicked(id) }
                }
            }
        }
    }
    private fun setDateTimePicker() {
        // Get the current date and time
        val now = LocalDateTime.now()

        // Set the minimum date of the DatePicker to the current date
        binding.datePicker.minDate = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Set the current date and time to the DatePicker and TimePicker
        binding.datePicker.updateDate(now.year, now.monthValue - 1, now.dayOfMonth)
        binding.timePicker.hour = now.hour
        binding.timePicker.minute = now.minute

        // Disable past times in the TimePicker
        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            val selectedTime = LocalDateTime.of(
                binding.datePicker.year,
                binding.datePicker.month + 1,
                binding.datePicker.dayOfMonth,
                hourOfDay,
                minute
            )
            if (selectedTime.isBefore(now)) {
                binding.timePicker.hour = now.hour
                binding.timePicker.minute = now.minute
            }
        }
    }

    private fun epochToMillis(epochTime: Long): Long {
        return epochTime * 1000
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //check if reminder with same title already exist
    private fun checkSameTitle(title: String): Boolean {
        return runBlocking {
            db.reminderDao().getReminders().first().any { reminder -> reminder.reminderName == title }
        }
    }

    //load audio files from local storage
    private fun getAudioFiles(): ArrayList<AudioFiles> {
        val audioList = ArrayList<AudioFiles>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE,
        )

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                val name = cursor.getString(1)
                val audioFiles = AudioFiles(path, name)
                Log.e("Path: $path", "Name: $name")
                audioList.add(audioFiles)
            }
        }

        val ringtoneManager = RingtoneManager(this)
        ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)

        ringtoneManager.cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val path = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
                val audioFiles = AudioFiles(path, name)
                Log.e("Path: $path", "Name: $name")
                audioList.add(audioFiles)
            }
        }
        return audioList
    }

    //region Event Listeners

    private fun onSpinnerItemSelected() {
    val audioFile = audioFiles[binding.spinner.selectedItemPosition]
    val path = audioFile.path
    Log.e("Path: $path", "Name: ${audioFile.audioName}")

    // Stop the previous audio
    mediaPlayer?.stop()

    // Play the audio for 5 seconds
    mediaPlayer = MediaPlayer.create(this@CreateActivity, Uri.parse(path)).apply {
        start()

        lifecycleScope.launch(Dispatchers.IO) {
            delay(5000)
            stop()
        }
    }
}

    private fun onTitleChanged() {
        val title = binding.title.text.toString()

        val errorMessage = when {
            title.isBlank() -> "Title cannot be empty"
            title.length > 20 -> "Title cannot be more than 20 characters"
            currentMode == Mode.CREATE && checkSameTitle(title) -> "Reminder with same title already exist"
            currentMode == Mode.EDIT && title != oldTitleName && checkSameTitle(title) -> "Reminder with same title already exist"
            else -> null
        }

        if (errorMessage != null) {
            showToastAndDisableButton(errorMessage)
        } else {
            binding.saveButton.isEnabled = true
        }
    }

    private fun showToastAndDisableButton(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        binding.saveButton.isEnabled = false
    }

    private fun onCreateSaveButtonClicked() {
        val bundle : Bundle? = intent.extras
        ringtonePath = bundle?.getString(Constants.RINGTONE_PATH_EXTRA)
        notificationId = bundle?.getInt(Constants.NOTIFICATION_ID)
        title = bundle?.getString(Constants.TITLE_EXTRA)
        message = bundle?.getString(Constants.MESSAGE_EXTRA)
        time = bundle?.getLong(Constants.TIME_EXTRA)
        snoozeCounter = bundle?.getInt(Constants.SNOOZE_COUNTER)
        val title = binding.title.text.toString()
        val date = LocalDateTime.of(
            binding.datePicker.year,
            binding.datePicker.month+1,
            binding.datePicker.dayOfMonth,
            binding.timePicker.hour,
            binding.timePicker.minute,
            0
        )
        val time = date.format(DateTimeFormatter.ofPattern("hh:mm a"))
        val selectedRingtonePath = audioFiles[binding.spinner.selectedItemPosition].path

        val zoneId = ZoneId.systemDefault()
        val reminderEntity = ReminderEntity(
            reminderName = title,
            dateAdded = date.atZone(zoneId).toEpochSecond(),
            ringtonePath = selectedRingtonePath
        )

        // Insert the reminder into the database
        val reminderId = runBlocking { db.reminderDao().insertReminder(reminderEntity) }

        val intent = Intent(this@CreateActivity, MainActivity::class.java).apply {
            putExtra(Constants.REMINDER_ID_EXTRA, reminderId)
            putExtra(Constants.REMINDER_NAME_EXTRA, reminderEntity.reminderName)
            putExtra(Constants.REMINDER_DATE_EXTRA, reminderEntity.dateAdded)
            putExtra(Constants.REMINDER_TIME_EXTRA, epochToMillis(reminderEntity.dateAdded))
            putExtra(Constants.REMINDER_RINGTONE_PATH_EXTRA, reminderEntity.ringtonePath)
        }

        //log id
        Log.e("OnCreate save button intent ID", reminderId.toString())

        // Show a toast message
        Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show()

        // Create a new notification to notify the user that the reminder has been added
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, Constants.DEFAULT_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.reminder_icon)
            setContentTitle("Reminder")
            setContentText("Reminder $title will be triggered at $time")
            setContentIntent(PendingIntent.getActivity(this@CreateActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setAutoCancel(true)
        }

        // Set the notification manager ID to the same value as the notification ID from the bundle
        notificationManager.notify(notificationId ?: 0, builder.build())

        // Start the main activity
        startActivity(intent).also { finish() }

    }

    private fun onEditSaveButtonClicked(id: Long) {
        val title = binding.title.text.toString()
        val date = LocalDateTime.of(
            binding.datePicker.year,
            binding.datePicker.month+1,
            binding.datePicker.dayOfMonth,
            binding.timePicker.hour,
            binding.timePicker.minute,
            0)
        val time = date.format(DateTimeFormatter.ofPattern("hh:mm a"))
        val zoneId = ZoneId.systemDefault()
        val audioFiles = getAudioFiles()
        val selectedRingtonePath = audioFiles[binding.spinner.selectedItemPosition].path

        val reminderEntity = ReminderEntity(
            id = id,
            reminderName = title,
            dateAdded = date.atZone(zoneId).toEpochSecond(),
            ringtonePath = selectedRingtonePath
        )

        // Update the reminder into the database
        runBlocking {
            db.reminderDao().updateReminder(reminderEntity)
        }

        val intent = Intent(this@CreateActivity, MainActivity::class.java)
        intent.putExtra(Constants.REMINDER_ID_EXTRA, reminderEntity.id)
        intent.putExtra(Constants.REMINDER_NAME_EXTRA, reminderEntity.reminderName)
        intent.putExtra(Constants.REMINDER_DATE_EXTRA, reminderEntity.dateAdded)
        intent.putExtra(Constants.REMINDER_TIME_EXTRA, epochToMillis(reminderEntity.dateAdded))
        intent.putExtra(Constants.REMINDER_RINGTONE_PATH_EXTRA, reminderEntity.ringtonePath)

        //create new notification to notify user that reminder has been added
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, Constants.DEFAULT_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.reminder_icon)
            setContentTitle("Reminder")
            setContentText("Reminder $title will be triggered at $time")
            setContentIntent(PendingIntent.getActivity(this@CreateActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setAutoCancel(true)
        }

        //set notification manager id same as notificationId from bundle
        notificationManager.notify(0, builder.build())
        // Show a toast message
        Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show()

        // Start the main activity
        startActivity(intent).also { finish() }
    }

    private fun onCancelButtonClicked() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
        }
        finish()
    }

    //endregion
}