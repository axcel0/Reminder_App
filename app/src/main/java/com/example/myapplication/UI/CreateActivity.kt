package com.example.myapplication.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding
    private lateinit var db: AppDatabase
    //make arraylist of playerList
    val music = arrayOf<Spinner>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        val spinner = findViewById<Spinner>(R.id.spinner)
//        val arrayAdapter = ArrayAdapter.createFromResource(this, R.array.music_array, android.R.layout.simple_spinner_item)
//        spinner.adapter = arrayAdapter
//        spinner.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>,
//                                        view: View, position: Int, id: Long) {
//
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // write code to perform some action
//
//            }
//        }
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
}