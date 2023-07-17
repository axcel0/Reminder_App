package com.example.myapplication

import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker



class Reminder(title: String, date: DatePicker, time: TimePicker, textView2: TextView) {
    var title: String
    var textView2: TextView
    var date: DatePicker
    var time: TimePicker

    init {
        this.title = title
        this.textView2 = textView2
        this.date = date
        this.time = time

    }
//make fun schedule
    fun schedule() {
        //get data from text input from title
        val titleText = title
        //bind date and time into textView
        textView2.setOnClickListener {
            val date = "${date.dayOfMonth}/${date.month + 1}/${date.year}"
            val time = "${time.hour}:${time.minute}"
            val dateTime = "$date $time"
            //set textView2 to show date and time and title
            textView2.text = "$titleText $dateTime"
        }
    }
}