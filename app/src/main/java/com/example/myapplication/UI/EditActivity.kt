package com.example.myapplication.UI

import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCreateBinding
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime


class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle : Bundle? = intent.extras

        //bundle data
        val id = bundle?.getLong("id")
        val name = bundle?.getString("name")
        val dateTime = bundle?.getLong("added")

        val title = findViewById<TextInputEditText>(R.id.title)
        val date =  findViewById<DatePicker>(R.id.datePicker)
        val time =  findViewById<TimePicker>(R.id.timePicker)

        title.setText(name)
        //initiate date and time
        date.minDate = System.currentTimeMillis().also { time.setIs24HourView(true) }
        time.hour = LocalDateTime.ofEpochSecond(dateTime!!, 0, java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())).hour
        time.minute = LocalDateTime.ofEpochSecond(dateTime!!, 0, java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())).minute

        //save

        binding.saveButton.setOnClickListener {
            if (id!= null && name != null) {
                val newTitle = title.text.toString()
                val newDateTime = LocalDateTime.of(date.year, date.month+1, date.dayOfMonth, time.hour, time.minute, 0)
                val newDateTimeLong = newDateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
                MainActivity().updateReminder(id, newTitle, newDateTimeLong)
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





}