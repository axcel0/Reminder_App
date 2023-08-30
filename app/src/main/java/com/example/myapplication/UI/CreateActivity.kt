package com.example.myapplication.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
//        setAllAudioFiles()
        //make array for spinner to be filled with data from local storage alarm sounds
        binding = ActivityCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //set spinner to array from audioFiles
        val spinnerList = ArrayList<String>()

        getAudioFiles().run {
            audioFiles = this
        }
        spinnerList += audioFiles.map { it.audioName }

        //toast audioFiles size
        Toast.makeText(this, "audioFiles size: ${audioFiles.size}", Toast.LENGTH_SHORT).show()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerList)
        binding.spinner.adapter = adapter
        //toast selected item
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long){
                Toast.makeText(this@CreateActivity, "Selected item: ${parent?.getItemAtPosition(position).toString()}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //do nothing
            }
        }
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

    private fun editAction(bundle: Bundle?){
        val bundle : Bundle? = intent.extras

        //bundle data
        val id = bundle?.getLong("id")
        val name = bundle?.getString("name")
        val dateTime = bundle?.getLong("added")
//        val audio = bundle?.getString("audio")

        val title = findViewById<TextInputEditText>(R.id.title)
        val date =  findViewById<DatePicker>(R.id.datePicker)
        val time =  findViewById<TimePicker>(R.id.timePicker)
//        val audioSpinner = findViewById<Spinner>(R.id.spinner)

        title.setText(name)
        //initiate date and time
        date.minDate = System.currentTimeMillis().also { time.setIs24HourView(true) }
        time.hour = LocalDateTime.ofEpochSecond(dateTime!!, 0, ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())).hour
        time.minute = LocalDateTime.ofEpochSecond(dateTime!!, 0, ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())).minute
        //get selected audio file from spinner list
//        audioSpinner.onItemSelectedListener = this

        binding.saveButton.setOnClickListener {
            if (id!= null && name != null) {
                val newTitle = title.text.toString()
                val newDateTime = LocalDateTime.of(date.year, date.month+1, date.dayOfMonth, time.hour, time.minute, 0)
                val newDateTimeLong = newDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
                //save selected audio file from spinner list
//                val newAudio = audioSpinner.selectedItem.toString()


                MainActivity().updateReminder(id, newTitle, newDateTimeLong)
                MainActivity().loadData().run {
                    //refresh the list
                    startActivity(Intent(this@CreateActivity, MainActivity::class.java)).also { finish() }
                }
            }
        }
        //cancel
        binding.cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent).also { finish() }
        }

    }
    //load audio files from local storage
    private fun getAudioFiles(): ArrayList<AudioFiles> {
        val tempAudioList = ArrayList<AudioFiles>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE
        )
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                val name = cursor.getString(1)
                val audioFiles = AudioFiles(path, name)
                Log.e("Path: $path", "Name: $name")
                tempAudioList.add(audioFiles)
            }
            cursor.close()
        }
        return tempAudioList
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Toast.makeText(this,"Selected item: ${p0?.getItemAtPosition(p2).toString()}", Toast.LENGTH_SHORT).show()

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}