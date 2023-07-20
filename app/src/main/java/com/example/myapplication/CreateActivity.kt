package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
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
        datePicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            val date = Date(datePicker.year-1900, datePicker.month, datePicker.dayOfMonth)
            val time = Time(timePicker.hour, timePicker.minute, 0)
            val reminder = Reminder(title = "", date = date, time = time)
            reminder.schedule(textView2)
        }
        //make function to set time as repeat
        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            val date = Date(datePicker.year-1900, datePicker.month, datePicker.dayOfMonth)
            val time = Time(timePicker.hour, timePicker.minute, 0)
            val reminder = Reminder(title = "", date = date, time = time)
            reminder.schedule(textView2)
        }
        //make function to save data using apply button
        fun saveData(){
            val titleText = title.text.toString()
            val sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.apply{
                putString("STRING_KEY", titleText)
                putInt("INT_KEY", datePicker.year)
                putInt("INT_KEY", datePicker.month)
                putInt("INT_KEY", datePicker.dayOfMonth)
                putInt("INT_KEY", timePicker.hour)
                putInt("INT_KEY", timePicker.minute)
            }.apply()
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()

        }
        //make function to load data
        fun loadData(){
            val sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
            val savedString = sharedPreferences.getString("STRING_KEY", null)
            val savedInt = sharedPreferences.getInt("INT_KEY", 0)
            title.setText(savedString)
            datePicker.updateDate(savedInt, savedInt, savedInt)
            timePicker.hour = savedInt
            timePicker.minute = savedInt
        }


        //set applyButton to save data
        applyButton.setOnClickListener {
            val titleText = title.text.toString()
            val date = Date(datePicker.year-1900, datePicker.month, datePicker.dayOfMonth)
            val time = Time(timePicker.hour, timePicker.minute, 0)
            val reminder = Reminder(title = titleText, date = date, time = time)
            reminder.schedule(textView2)
            saveData()
            loadData()

        }


    }
}