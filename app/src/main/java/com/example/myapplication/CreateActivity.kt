package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import com.example.myapplication.databinding.ActivityCreateBinding
import com.google.android.material.textfield.TextInputEditText
import java.sql.Time
import java.util.Date

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val title = findViewById<TextInputEditText>(R.id.title)
        val datePicker =  findViewById<DatePicker>(R.id.datePicker)
        val timePicker =  findViewById<TimePicker>(R.id.timePicker)
        val textView2 = findViewById<TextView>(R.id.textView2)
        val applyButton = findViewById<Button>(R.id.applyButton)

        datePicker.minDate = System.currentTimeMillis()
        timePicker.setIs24HourView(true)

        //make function to set day as repeat

        //set applyButton to save data
        applyButton.setOnClickListener {
            val titleText = title.text.toString()
            val date = Date(datePicker.year-1900, datePicker.month, datePicker.dayOfMonth)
            val time = Time(timePicker.hour, timePicker.minute, 0)
            val reminder = Reminder(title = titleText, date = date, time = time)
            reminder.schedule(textView2)
        }

    }
}