package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val title = findViewById<TextInputEditText>(R.id.title)
        val datePicker =  findViewById<DatePicker>(R.id.datePicker)
        val timePicker =  findViewById<TimePicker>(R.id.timePicker)
        val textView2 = findViewById<TextView>(R.id.textView2)
        val applyButton = findViewById<Button>(R.id.applyButton)

//    //bind date and time into textView
//        textView2.setOnClickListener {
//            val date = "${datePicker.dayOfMonth}/${datePicker.month+1}/${datePicker.year}"
//            val time = "${timePicker.hour}:${timePicker.minute}"
//            val dateTime = "$date $time"
//            textView2.text = dateTime
//        }
        //get data from text input from title
        val titleText = title.editableText.toString()

        //set applyButton to save data
        applyButton.setOnClickListener {
            val reminder = Reminder(title = titleText, date = datePicker, time = timePicker, textView2 = textView2)
            reminder.schedule()
        }
    }








}