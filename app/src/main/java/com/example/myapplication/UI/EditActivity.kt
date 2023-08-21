package com.example.myapplication.UI

import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.UI.MainActivity
import com.example.myapplication.databinding.ActivityCreateBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.models.AppDatabase
import com.google.android.material.textfield.TextInputEditText


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
        val added = bundle?.getLong("added")

        val title = findViewById<TextInputEditText>(R.id.title)
        val datePicker =  findViewById<DatePicker>(R.id.datePicker)
        val timePicker =  findViewById<TimePicker>(R.id.timePicker)

        title.setText(name)
        datePicker.minDate = System.currentTimeMillis().also { timePicker.setIs24HourView(true) }


        //save

        binding.saveButton.setOnClickListener {
            if (id!= null && name != null && added != null) {
                val newTitle = title.text.toString()

                MainActivity().updateReminder(id, newTitle, added)
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