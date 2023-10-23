package com.example.myapplication.UI

import android.app.AlarmManager
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
import android.os.VibratorManager
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.core.app.NotificationCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.R
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

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

    private enum class Mode {
        CREATE, EDIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Get the database
        db = MainActivity.getDatabase(this)

        // Get the audio files
        audioFiles = getAudioFiles()
        val spinnerList = ArrayList<String>()
        spinnerList += audioFiles.map { it.audioName }

        // Set the spinner adapter
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerList)
        binding.spinner.adapter = spinnerAdapter

        // Set the date picker and time picker
        binding.datePicker.minDate = System.currentTimeMillis().also { binding.timePicker.setIs24HourView(true) }

        // Set Event Listeners
        binding.title.addTextChangedListener {onTitleChanged() }
        binding.cancelButton.setOnClickListener{ onCancelButtonClicked() }
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isFirstInit) {
                    isFirstInit = false
                } else {
                    onSpinnerItemSelected()
                }
            }
        }

        // Determine the current mode of the activity
        currentMode = if (intent.hasExtra(Constants.REMINDER_ID_EXTRA)) Mode.EDIT else Mode.CREATE
        when (currentMode) {
            Mode.CREATE -> {
                binding.saveButton.isEnabled = false
                binding.saveButton.setOnClickListener { onCreateSaveButtonClicked() }
            }

            Mode.EDIT -> {
                val bundle: Bundle = intent.extras!!
                val id = bundle.getLong(Constants.REMINDER_ID_EXTRA)
                val name = bundle.getString(Constants.REMINDER_NAME_EXTRA)
                val dateTime = bundle.getLong(Constants.REMINDER_DATE_EXTRA)
                val ringtonePath = bundle.getString(Constants.REMINDER_RINGTONE_PATH_EXTRA)
                oldTitleName = name
                currentMode = Mode.EDIT

                binding.title.setText(name)

                binding.datePicker.minDate = System.currentTimeMillis().also { binding.timePicker.setIs24HourView(true) }
                binding.timePicker.hour = LocalDateTime.ofEpochSecond(
                    if (dateTime < System.currentTimeMillis()) System.currentTimeMillis() / 1000 else dateTime, 0,
                    ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
                ).hour
                binding.timePicker.minute = LocalDateTime.ofEpochSecond(
                    if (dateTime < System.currentTimeMillis()) System.currentTimeMillis() / 1000 else dateTime, 0,
                    ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
                ).minute

                val ringtoneName = audioFiles.find { it.path == ringtonePath }?.audioName
                val spinnerPosition = spinnerAdapter.getPosition(ringtoneName)
                binding.spinner.setSelection(spinnerPosition)

                binding.saveButton.setOnClickListener { onEditSaveButtonClicked(id) }
            }
        }
    }

    private fun epochToMillis(epochTime: Long): Long {
        return epochTime * 1000
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
    }

    //make function to check if there any same title in database
    private fun checkSameTitle(title: String): Boolean {
        return db.reminderDao().getReminders().any { reminder -> reminder.reminderName == title }
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

    //load audio files from local storage
    private fun getAudioFiles(): ArrayList<AudioFiles> {
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
                val path = cursor2.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor2.getString(RingtoneManager.ID_COLUMN_INDEX)
                val audioFiles = AudioFiles(path, name)
                Log.e("Path: $path", "Name: $name")
                audioList.add(audioFiles)
            }
            cursor2.close()
        }
        return audioList
    }

    //region Event Listeners

    private fun onSpinnerItemSelected() {
        val audioFiles = audioFiles[binding.spinner.selectedItemPosition]
        val path = audioFiles.path
        Log.e("Path: $path", "Name: ${audioFiles.audioName}")

        // Stop the previous audio
        if (mediaPlayer!= null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }

        // Play the audio for 5 seconds
        mediaPlayer = MediaPlayer.create(this@CreateActivity, Uri.parse(path))
        mediaPlayer!!.start()
        Thread {
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            mediaPlayer!!.stop()
        }.start()
    }

    private fun onTitleChanged() {
        val title = binding.title.text.toString()

        if (title.isBlank()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            binding.saveButton.isEnabled = false
        } else if (title.length > 20) {
            Toast.makeText(this, "Title cannot be more than 20 characters", Toast.LENGTH_SHORT).show()
            binding.saveButton.isEnabled = false
        } else if (currentMode == Mode.CREATE && checkSameTitle(title)) {
            Toast.makeText(this, "Reminder with same title already exist", Toast.LENGTH_SHORT).show()
            binding.saveButton.isEnabled = false
        } else if (currentMode == Mode.EDIT && title != oldTitleName) {
            if (checkSameTitle(title)) {
                Toast.makeText(this, "Reminder with same title already exist", Toast.LENGTH_SHORT).show()
                binding.saveButton.isEnabled = false
            }
            else {
                binding.saveButton.isEnabled = true
            }
        }
        else {
            binding.saveButton.isEnabled = true
        }
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
            0)

        val zoneId = ZoneId.systemDefault()
        val selectedRingtonePath = audioFiles[binding.spinner.selectedItemPosition].path

        val reminderEntity = ReminderEntity(
            reminderName = title,
            dateAdded = date.atZone(zoneId).toEpochSecond(),
            ringtonePath = selectedRingtonePath
        )

        // Insert the reminder into the database
        db.reminderDao().insertReminder(reminderEntity)

        val intent = Intent(this@CreateActivity, MainActivity::class.java)
        intent.putExtra(Constants.REMINDER_ID_EXTRA, reminderEntity.id)
        intent.putExtra(Constants.REMINDER_NAME_EXTRA, reminderEntity.reminderName)
        intent.putExtra(Constants.REMINDER_DATE_EXTRA, reminderEntity.dateAdded)
        intent.putExtra(Constants.REMINDER_TIME_EXTRA, epochToMillis(reminderEntity.dateAdded))
        intent.putExtra(Constants.REMINDER_RINGTONE_PATH_EXTRA, reminderEntity.ringtonePath)

        // Show a toast message
        Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show()
        //create new notification to notify user that reminder has been added
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, Constants.DEFAULT_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.reminder_icon)
            setContentTitle("Reminder")
            setContentText("Reminder $title will be triggered at ${date.hour}:${date.minute}")
            setContentIntent(PendingIntent.getActivity(this@CreateActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setAutoCancel(true)
        }
        //set notification manager id same as notificationId from bundle
        notificationManager.notify(0, builder.build())
        // Start the main activity
        startActivity(intent).also { finish() }
    }

    private fun onEditSaveButtonClicked(id: Long) {
        val bundle : Bundle? = intent.extras
        //bundle data
        val id = bundle?.getLong("id")
        val name = bundle?.getString("name")
       //binding title, date, time
        val title = binding.title.text.toString()
        val date = LocalDateTime.of(
            binding.datePicker.year,
            binding.datePicker.month+1,
            binding.datePicker.dayOfMonth,
            binding.timePicker.hour,
            binding.timePicker.minute,
            0)
        if (id!= null && name != null) {
            val newTitle = title
            val newDateTime = LocalDateTime.of(date.year, date.month+1, date.dayOfMonth, date.hour, date.minute, 0)
            val newDateTimeLong = newDateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
            val newRingtonePath = EditActivity.audioFiles[binding.spinner.selectedItemPosition].path

            val calendar = Calendar.getInstance()
            calendar.set(newDateTime.year, newDateTime.monthValue-1, newDateTime.dayOfMonth, newDateTime.hour, newDateTime.minute, 0)
            val time = calendar.timeInMillis

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("reminderName", newTitle)
            intent.putExtra("dateAdded", newDateTimeLong)
            intent.putExtra("time", time)
            intent.putExtra("ringtonePath", newRingtonePath)

            MainActivity().updateReminder(id, newTitle, newDateTimeLong, newRingtonePath)
            MainActivity().loadData().run {
                //refresh the list
                startActivity(intent).also { finish() }
            }
        }
    }

    private fun onCancelButtonClicked() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent).also { finish() }
    }

    //endregion
}