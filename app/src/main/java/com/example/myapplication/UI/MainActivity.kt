package com.example.myapplication.UI

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.myapplication.R
import com.example.myapplication.UI.adapters.ReminderAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
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
//    private String CHANNEL_ID = "CHANNEL 1"

    companion object {
        private var db: AppDatabase? = null
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
        menuInflater.inflate(R.menu.menu_main, binding.toolbar.menu)
        supportActionBar!!.title = "Reminders"
        recyclerView = binding.recyclerView
        adapter = ReminderAdapter(reminderList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true) //recyclerview size is fixed
        //select reminder from recyclerview
//        addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
//            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                val child = rv.findChildViewUnder(e.x, e.y)
//                if (child != null && gestureDetector.onTouchEvent(e)) {
//                    val position = rv.getChildAdapterPosition(child)
//                    val reminder = adapter.holdReminder(position)
//                    val intent = Intent(this@MainActivity, CreateActivity::class.java)
//                    intent.putExtra("reminder", reminder)
//                    startActivity(intent)
//                    return true
//                }
//                return false
//            }
//
//            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
//                Log.d("MainActivity", "onTouchEvent: $e")
//            }
//
//            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
//                Log.d("MainActivity", "onRequestDisallowInterceptTouchEvent: $disallowIntercept")
//            }
//        })
        loadData().also { requestNotificationPermissions() }

        val fmc = FirebaseMessaging.getInstance()
        fmc.token.addOnCompleteListener() { task ->
            if (!task.isSuccessful) {
                println("Fetching FCM registration token failed").also{
                    return@addOnCompleteListener
                }
            }
            val token = task.result
            println("FCM token: $token")
            //toast message
            Toast.makeText(baseContext, "FCM token: $token", Toast.LENGTH_SHORT).show()

        }


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
            R.id.action_search -> {
                val searchView = item.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        searchReminder(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        searchReminder(newText)
                        return true
                    }
                })
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
        reminderList = getDatabase(this).reminderDao().getReminders().also{
            updateUIComponents()
        }
    }
    //search reminder by title
    private fun searchReminder(reminderName: String?) {
        val reminderDao = getDatabase(this).reminderDao()
        reminderList = reminderDao.searchReminder(reminderName!!).also {
            updateUIComponents()
        }
    }
    //delete reminder
    private fun deleteReminder(reminderId: Long) {
        val reminderDao = getDatabase(this).reminderDao()
        reminderDao.deleteReminder(reminderId).also { loadData() }
    }
    //delete all reminders
    private fun deleteAllReminders() {
        val reminderDao = getDatabase(this).reminderDao()
        reminderDao.deleteAllReminders().also { loadData() }
    }

    private fun requestNotificationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    private fun updateUIComponents() {
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