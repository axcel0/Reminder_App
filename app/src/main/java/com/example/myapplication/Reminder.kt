package com.example.myapplication

import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import com.google.android.material.timepicker.TimeFormat
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Date


class Reminder(title: String, date: Date, time: Time) {
    var title: String
    var date: Date
    var time: Time

    init {
        this.title = title
        this.date = date
        this.time = time

    }
//make fun schedule
    fun schedule(textView2: TextView) {
        //get data from text input from title
        val titleText = title
        //bind date and time into textView
        textView2.setOnClickListener {
            val date = SimpleDateFormat("dd/MM/y").format(date)
            val time = SimpleDateFormat("HH:mm").format(time)
            val dateTime = "$date $time"
            //set textView2 to show date and time and title
            textView2.text = "$titleText $dateTime"
        }
    }
}