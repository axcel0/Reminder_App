package com.example.myapplication.UI

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.example.myapplication.services.TITLE_EXTRA
import com.example.myapplication.services.notificationID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var reminderList: List<ReminderEntity> = emptyList()

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

        loadData().also { requestNotificationPermissions() }

//        val fcm = FirebaseMessaging.getInstance()
//        fcm.token.addOnCompleteListener() { task ->
//            if (!task.isSuccessful) {
//                println("Fetching FCM registration token failed").also{
//                    return@addOnCompleteListener
//                }
//            }
//            val token = task.result
//
//        }
//        fcm.subscribeToTopic("reminder").addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                Toast.makeText(this, "Subscribed to reminder", Toast.LENGTH_SHORT).show()
//            }
//
//        }
        createNotificationChannel()
        if(intent.hasExtra("reminderName")) {
            val title = intent.getStringExtra("reminderName")
            val dateAdded = intent.getLongExtra("dateAdded", 0)
            val time = intent.getLongExtra("time", 0)
            Log.d("MainActivity", "onCreate: $title $dateAdded $time")
            scheduleNotification(ReminderEntity(reminderName = title!!, dateAdded = dateAdded), time)
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
        val title = reminder.reminderName
        val message = "Don't Forget to do ${reminder.reminderName}"

        notificationIntent.putExtra(TITLE_EXTRA, title)
        notificationIntent.putExtra(MESSAGE_EXTRA, message)



        val pendingIntent = PendingIntent.getBroadcast(applicationContext, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager


        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
//        showAlert(time, title, message)

//        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent)
    }
    //show alert dialog to notify when the reminder time has come
    private fun showAlert(time: Long, title: String?, message: String?) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
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
            R.id.action_delete_all -> {
                val alertDialog = AlertDialog.Builder(this)
                if (deleteList.size > 0) {
                    alertDialog.setTitle("Delete Selected Reminders")
                    alertDialog.setMessage("Are you sure you want to delete selected reminders?")
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        deleteList.forEach {
                            deleteReminder(it.toLong())
                        }.run { deleteList.clear() }
                    }
                    alertDialog.setNegativeButton("No") { _, _ ->
                        deleteList.clear()
                    }
                } else {
                    //alert dialog to delete all reminders
                    alertDialog.setTitle("Delete All Reminders")
                    alertDialog.setMessage("Are you sure you want to delete all reminders?")
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        deleteAllReminders()
                    }
                    alertDialog.setNegativeButton("No") { _, _ ->
                    }
                }
                alertDialog.show()
                true
            }
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
    fun updateReminder(reminderId: Long, reminderName: String, dateAdded: Long) {
        val reminderDao = getDatabase(this).reminderDao()
        reminderDao.updateReminder(reminderId, reminderName, dateAdded).also { loadData() }
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