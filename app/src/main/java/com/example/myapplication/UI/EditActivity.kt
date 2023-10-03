package com.example.myapplication.UI

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.AudioFiles
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime


class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBinding
    private lateinit var db: AppDatabase
    companion object {
        var audioFiles = ArrayList<AudioFiles>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getAudioFiles().run {
            audioFiles = this
        }

        //set spinner to array from audioFiles
        val spinnerList = ArrayList<String>()
        spinnerList += audioFiles.map { it.audioName }


        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerList)
        binding.spinner.adapter = adapter

        val bundle : Bundle? = intent.extras

        //bundle data
        val id = bundle?.getLong("id")
        val name = bundle?.getString("name")
        val dateTime = bundle?.getLong("added")
        val ringtoneName = bundle?.getString("ringtoneName")


        db = MainActivity.getDatabase(this)
        val title = findViewById<TextInputEditText>(R.id.title)
        val date =  findViewById<DatePicker>(R.id.datePicker)
        val time =  findViewById<TimePicker>(R.id.timePicker)

        title.setText(name)
        //initiate date and time
        date.minDate = System.currentTimeMillis().also { time.setIs24HourView(true) }
        time.hour = LocalDateTime.ofEpochSecond(dateTime!!, 0, java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())).hour
        time.minute = LocalDateTime.ofEpochSecond(dateTime, 0, java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())).minute

        //set spinner to ringtoneName
        val spinnerPosition = adapter.getPosition(ringtoneName)
        binding.spinner.setSelection(spinnerPosition)
//        binding.saveButton.isEnabled = true
        //if user close keyboard, check if there any same title
        //use addtextchangedlistener to check if there any same title as soon as user type
        title.addTextChangedListener {
            if (checkSameTitle(title.text.toString())) {
                Toast.makeText(this, "Reminder with same title already exist", Toast.LENGTH_SHORT).show()
                //disable saveButton if there any same title
                binding.saveButton.isEnabled = false
            }else if (title.text.toString() == "") {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                //disable saveButton if title is empty
                binding.saveButton.isEnabled = false
            }else if (title.text.toString().length > 20) {
                Toast.makeText(this, "Title cannot be more than 20 characters", Toast.LENGTH_SHORT).show()
                //disable saveButton if title is more than 20 characters
                binding.saveButton.isEnabled = false
            }
            //check if there is no same title, enable saveButton
            else {
                binding.saveButton.isEnabled = true
            }
        }
        //save data to database
        binding.saveButton.setOnClickListener {
            if (id!= null && name != null) {
                val newTitle = title.text.toString()
                val newDateTime = LocalDateTime.of(date.year, date.month+1, date.dayOfMonth, time.hour, time.minute, 0)
                val newDateTimeLong = newDateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
                val newRingtoneName = binding.spinner.selectedItem.toString()
                MainActivity().updateReminder(id, newTitle, newDateTimeLong, newRingtoneName)
                MainActivity().loadData().run {
                    //refresh the list
                    startActivity(Intent(this@EditActivity, MainActivity::class.java)).also { finish() }
                }
            }

        }
        //cancel
        binding.cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent).also { finish() }
        }


    }

    private fun checkSameTitle(title: String): Boolean {
        val reminderList = db.reminderDao().getReminders()
        for (reminder in reminderList) {
            if (reminder.reminderName == title) {
                return true
            }
        }
        return false
    }

    private fun getAudioFiles(): ArrayList<AudioFiles> {
        val tempAudioList = ArrayList<AudioFiles>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE
        )
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                val name = cursor.getString(1)
                val audioFiles = AudioFiles(path, name)
                Log.e("Path: $path", "Name: $name")
                tempAudioList.add(audioFiles)
            }
            cursor.close()
        }
        return tempAudioList
    }





}