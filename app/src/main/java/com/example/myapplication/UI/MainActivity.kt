package com.example.myapplication.UI

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.myapplication.R
import com.example.myapplication.UI.adapters.ReminderAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Time
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {
    private var reminderList: List<ReminderEntity> = emptyList()
    private var uiScope: CoroutineScope? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter


    companion object {
        var db: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            if (db == null) {
                db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "database-name"
                ).allowMainThreadQueries().build()
            }
            return db!!
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar!!.title = "Reminders"

        recyclerView = binding!!.recyclerView
        adapter = ReminderAdapter(reminderList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadData()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_create -> {
                openCreateActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openCreateActivity() {
        val intent = Intent(this, CreateActivity::class.java)
        startActivity(intent)
    }

    private fun loadData() {
        reminderList = getDatabase(this).reminderDao().getReminders()
        updateUIComponents()
    }

    fun updateUIComponents() {
        uiScope?.cancel()

        uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        uiScope?.launch {
            try {
                // Update the adapter on the IO dispatcher
                val newAdapter = withContext(Dispatchers.IO) {
                    ReminderAdapter(reminderList)
                }

                // Update the RecyclerView adapter on the main thread
                recyclerView.adapter = newAdapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}