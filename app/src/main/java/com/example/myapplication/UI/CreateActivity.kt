package com.example.myapplication.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.AudioFiles
import com.example.myapplication.models.entities.ReminderEntity
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar


class CreateActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityCreateBinding
    private lateinit var db: AppDatabase

    companion object {
        var audioFiles = ArrayList<AudioFiles>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAllAudioFiles()
        //make array for spinner to be filled with data from local storage alarm sounds
        setContentView(R.layout.activity_create)
        val spinnerArray = arrayOf("Alarm 1", "Alarm 2", "Alarm 3", "Alarm 4", "Alarm 5")
        val spinner = findViewById<Spinner>(R.id.spinner)
        //set spinner to array

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerArray)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = spinnerAdapter

        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = MainActivity.getDatabase(this)

        val title = findViewById<TextInputEditText>(R.id.title)
        val datePicker =  findViewById<DatePicker>(R.id.datePicker)
        val timePicker =  findViewById<TimePicker>(R.id.timePicker)

        datePicker.minDate = System.currentTimeMillis().also { timePicker.setIs24HourView(true) }

        //set applyButton to save data
        binding.saveButton.setOnClickListener {
            val titleText = title.text.toString()
            val date = LocalDateTime.of(datePicker.year, datePicker.month+1, datePicker.dayOfMonth, timePicker.hour, timePicker.minute, 0)
            val zoneId = ZoneId.systemDefault()
            val epoch = date.atZone(zoneId).toEpochSecond()
            val reminderEntity = ReminderEntity(reminderName = titleText, dateAdded = epoch)

            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.monthValue-1, date.dayOfMonth, date.hour, date.minute, 0)
            val time = calendar.timeInMillis

            db.reminderDao().insertReminder(reminderEntity).also {
                Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show()
            }.run {
                val intent = Intent(this@CreateActivity, MainActivity::class.java)
                intent.putExtra("reminderName", titleText)
                intent.putExtra("dateAdded", epoch)
                intent.putExtra("time", time)
                startActivity(intent).also { finish() }

            }
        }
        //set cancelButton to go back to MainActivity
        binding.cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent).also { finish() }
        }


    }

    fun setAllAudioFiles(){
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA
        )
        val sortingList = MediaStore.Audio.Media.DISPLAY_NAME + " ASC"
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortingList
        )
        while (cursor!!.moveToNext())
        {
            val name = cursor.getString(1)
            val path = cursor.getString(2)
            val audioFilesTemp = AudioFiles(path, name)
            audioFiles.add(audioFilesTemp)
        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Toast.makeText(this,"Selected item: ${p0?.getItemAtPosition(p2).toString()}", Toast.LENGTH_SHORT).show()

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}