package com.example.myapplication.UI

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
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.AudioFiles
import com.example.myapplication.models.entities.ReminderEntity
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import android.media.RingtoneManager


class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding
    private lateinit var db: AppDatabase
    private var mediaplayer: MediaPlayer? = null
    private var isFirstInit = true


    companion object {
        var audioFiles = ArrayList<AudioFiles>()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setAllAudioFiles()
        //make array for spinner to be filled with data from local storage alarm sounds
        binding = ActivityCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //set spinner to array from audioFiles
        val spinnerList = ArrayList<String>()

        getAudioFiles().run {
            audioFiles = this
        }
        //get audiofiles uri
        spinnerList += audioFiles.map { it.audioName }

        //toast audioFiles size
        Toast.makeText(this, "audioFiles size: ${audioFiles.size}", Toast.LENGTH_SHORT).show()


        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerList)
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long){

                val audioFiles = audioFiles[position]
                val path = audioFiles.path
                Log.e("Path: $path", "Name: ${audioFiles.audioName}")
                if (mediaplayer!= null && mediaplayer!!.isPlaying) {
                    mediaplayer!!.stop()
                }
                mediaplayer = MediaPlayer.create(this@CreateActivity, Uri.parse(path))
                if (!isFirstInit) {
                    mediaplayer!!.start()
                } else {
                    isFirstInit = false
                }


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //do nothing
            }
        }

        db = MainActivity.getDatabase(this)

        val title = findViewById<TextInputEditText>(R.id.title)
        val datePicker =  findViewById<DatePicker>(R.id.datePicker)
        val timePicker =  findViewById<TimePicker>(R.id.timePicker)

        datePicker.minDate = System.currentTimeMillis().also { timePicker.setIs24HourView(true) }
        binding.saveButton.isEnabled = false
        title.addTextChangedListener {
            if (checkSameTitle(title.text.toString())) {
                Toast.makeText(this, "Reminder with same title already exist", Toast.LENGTH_SHORT).show()
                binding.saveButton.isEnabled = false
            } else if (title.text.toString() == "") {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                binding.saveButton.isEnabled = false
            } else if (title.text.toString().length > 20) {
                Toast.makeText(this, "Title cannot be more than 20 characters", Toast.LENGTH_SHORT).show()
                binding.saveButton.isEnabled = false
            } else {
                binding.saveButton.isEnabled = true
            }
        }

        //set saveButton to save data
        binding.saveButton.setOnClickListener {
            val titleText = title.text.toString()
            val date = LocalDateTime.of(datePicker.year, datePicker.month+1, datePicker.dayOfMonth, timePicker.hour, timePicker.minute, 0)
            val zoneId = ZoneId.systemDefault()
            val selectedRingtonePath = audioFiles[binding.spinner.selectedItemPosition].path

            val epoch = date.atZone(zoneId).toEpochSecond()
            val reminderEntity = ReminderEntity(reminderName = titleText, dateAdded = epoch, ringtonePath = selectedRingtonePath)

            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.monthValue-1, date.dayOfMonth, date.hour, date.minute, 0)
            val time = calendar.timeInMillis

            db.reminderDao().insertReminder(reminderEntity).also {
                Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show()
            }.run {
                val intent = Intent(this@CreateActivity, MainActivity::class.java)
                intent.putExtra("id", this)
                intent.putExtra("reminderName", titleText)
                intent.putExtra("dateAdded", epoch)
                intent.putExtra("time", time)
                intent.putExtra("ringtonePath", selectedRingtonePath)
                startActivity(intent).also { finish() }

            }
        }
        //set cancelButton to go back to MainActivity
        binding.cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent).also { finish() }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaplayer?.stop()
    }

    //make function to check if there any same title in database
    private fun checkSameTitle(title: String): Boolean {
        val reminderList = db.reminderDao().getReminders()
        for (reminder in reminderList) {
            if (reminder.reminderName == title) {
                return true
            }
        }
        return false
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

}