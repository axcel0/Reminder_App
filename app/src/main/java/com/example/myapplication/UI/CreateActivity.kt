package com.example.myapplication.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity
import com.google.android.material.textfield.TextInputEditText
import java.sql.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
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
            db.reminderDao().insertReminder(reminderEntity).also {
                Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show()
            }.run {
                //refresh the list
                startActivity(Intent(this@CreateActivity, MainActivity::class.java)).also { finish() }
            }
        }

        //set cancelButton to go back to MainActivity
        binding.cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent).also { finish() }
        }


    }
}