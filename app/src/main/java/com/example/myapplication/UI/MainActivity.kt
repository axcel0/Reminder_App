package com.example.myapplication.UI

import android.app.AlarmManager
import android.app.Instrumentation.ActivityResult
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.services.MESSAGE_EXTRA
import com.example.myapplication.services.NOTIFICATION_ID
import com.example.myapplication.services.TITLE_EXTRA
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var reminderList: List<ReminderEntity> = emptyList()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isPostNotificationPermissionGranted = false
    private var isReadMediaAudioPermissionGranted = false
    private var uiScope: CoroutineScope? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter
//    private String CHANNEL_ID = "CHANNEL 1"

    companion object {
        var deleteList : ArrayList<String> = ArrayList()
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

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("MainActivity", "${it.key} = ${it.value}")
            }
        }
        loadData().also {
            updateUIComponents()
            requestPermission()
        }
        //get data from intent
        val intent = intent
        createNotificationChannel()
        if(intent.hasExtra("reminderName")) {
            val id = intent.getLongExtra("id", 0)
            val title = intent.getStringExtra("reminderName")
            val dateAdded = intent.getLongExtra("dateAdded", 0)
            val time = intent.getLongExtra("time", 0)
            val ringtonePath = intent.getStringExtra("ringtonePath")
            Log.d("MainActivity", "onCreate: $title $dateAdded $time")
            scheduleNotification(ReminderEntity(id = id, reminderName = title!!, dateAdded = dateAdded, ringtonePath = ringtonePath!!), time)
        }
    }
    private fun createNotificationChannel() {
        val name = "Reminder"
        val descriptionText = "Reminder"
        val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
        val channel = android.app.NotificationChannel("Reminder", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: android.app.NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    private fun scheduleNotification(reminder: ReminderEntity, time: Long) {
        val notificationIntent = Intent(this, AlarmReceiver::class.java)
        val notificationID = reminder.id.toInt()
        val title = reminder.reminderName
        val message = "Don't Forget to do ${reminder.reminderName}"
        //toast notificationID
        Toast.makeText(this, "notificationID: $notificationID", Toast.LENGTH_SHORT).show()
        notificationIntent.putExtra(NOTIFICATION_ID, notificationID)
        notificationIntent.putExtra(TITLE_EXTRA, title)
        notificationIntent.putExtra(MESSAGE_EXTRA, message)

        val pendingIntent = PendingIntent.getBroadcast(applicationContext, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager


        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)

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
                searchView.queryHint = "Search Reminder"
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
            R.id.action_delete_all -> {
                val alertDialog = MaterialAlertDialogBuilder(this)
                    .setTitle("Delete All Reminders")
                    .setMessage("Are you sure you want to delete all reminders?")
                if (deleteList.size > 0) {
                    alertDialog.setTitle("Delete Selected Reminders")
                    alertDialog.setMessage("Are you sure you want to delete selected reminders?")
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        deleteList.forEach {
                            deleteReminder(it.toLong())
                            deleteList = ArrayList()
                        }
                    }
                    alertDialog.setNegativeButton("No") { _, _ ->
                        //set deleteList to empty
                        deleteList = ArrayList()

                    }
                }
                else {
                   if(reminderList.isEmpty()) {
                       alertDialog.setMessage("There is no reminder to delete")
                       alertDialog.setPositiveButton("Ok") { _, _ ->

                       }
                   }else {
                       //alert dialog to delete all reminders
                       alertDialog.setTitle("Delete All Reminders")
                       alertDialog.setMessage("Are you sure you want to delete all reminders?")
                       alertDialog.setPositiveButton("Yes") { _, _ ->
                           deleteAllReminders()
                       }
                       alertDialog.setNegativeButton("No") { _, _ ->
                           deleteList = ArrayList()
                       }
                   }
                }
                alertDialog.show()
                //notify adapter
                true
            }
            //use material design

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openCreateActivity() {
        val intent = Intent(this, CreateActivity::class.java)
        startActivity(intent)
    }

    fun loadData() {
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
    //update data reminder by id
    fun updateReminder(reminderId: Long, reminderName: String, dateAdded: Long, ringtoneName: String) {
        val reminderDao = getDatabase(this).reminderDao()
        reminderDao.updateReminder(reminderId, reminderName, dateAdded, ringtoneName).also { loadData() }
    }
    //permission to read media audio
    private fun requestReadStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), 2)
        }
    }
    //permission to post notification
    private fun requestNotificationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }
    private fun requestPermission() {
        isReadMediaAudioPermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        isPostNotificationPermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        val permissionRequest: MutableList<String> = ArrayList()

        if (!isReadMediaAudioPermissionGranted) {
            permissionRequest.add(android.Manifest.permission.READ_MEDIA_AUDIO)
        }

        if (!isPostNotificationPermissionGranted) {
            permissionRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
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