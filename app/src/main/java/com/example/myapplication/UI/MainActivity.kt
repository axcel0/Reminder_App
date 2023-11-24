package com.example.myapplication.UI

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.R
import com.example.myapplication.UI.adapters.ReminderAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.services.AlarmService
import com.example.myapplication.utils.Constants
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ServiceConnection {
    private var reminderList: List<ReminderEntity> = emptyList()
    private var isPostNotificationPermissionGranted = false
    private var isReadMediaAudioPermissionGranted = false
    private var isDisplayOverOtherAppsPermissionGranted = false
    private lateinit var toolbar: MaterialToolbar
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: ReminderAdapter

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
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
//        window.statusBarColor = ContextCompat.getColor(this, R.color.material_dynamic_primary)
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                when (it.key) {
                    android.Manifest.permission.READ_MEDIA_AUDIO -> isReadMediaAudioPermissionGranted = it.value
                    android.Manifest.permission.POST_NOTIFICATIONS -> isPostNotificationPermissionGranted = it.value
                    android.Manifest.permission.SYSTEM_ALERT_WINDOW -> isDisplayOverOtherAppsPermissionGranted = it.value
                }
            }
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        menuInflater.inflate(R.menu.menu_main, binding.toolbar.menu)

        recyclerView = binding.recyclerView
        swipeRefreshLayout = binding.swipeRefreshLayout

        adapter = ReminderAdapter(reminderList, object : ReminderAdapter.OnItemClickListener {
            override fun onItemClick(reminder: ReminderEntity) {
                // Handle the click event here to edit the reminder
                val intent = Intent(this@MainActivity, CreateActivity::class.java).apply {
                    putExtra(Constants.REMINDER_ID_EXTRA, reminder.id)
                    putExtra(Constants.REMINDER_NAME_EXTRA, reminder.reminderName)
                    putExtra(Constants.REMINDER_DATE_EXTRA, reminder.dateAdded)
                    putExtra(Constants.REMINDER_RINGTONE_PATH_EXTRA, reminder.ringtonePath)
                }
                startActivity(intent)
            }
        })

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = this@MainActivity.adapter
        }

        swipeRefreshLayout.setOnRefreshListener {
            loadData()
            swipeRefreshLayout.isRefreshing = false
        }

        supportActionBar!!.title = getString(R.string.action_bar_name)

        loadData()
        requestPermission()
        createNotificationChannel()

        if (intent.hasExtra(Constants.REMINDER_ID_EXTRA)) {
            val id = intent.getLongExtra(Constants.REMINDER_ID_EXTRA, 0)
            val title = intent.getStringExtra(Constants.REMINDER_NAME_EXTRA)
            val dateAdded = intent.getLongExtra(Constants.REMINDER_DATE_EXTRA, 0)
            val time = intent.getLongExtra(Constants.REMINDER_TIME_EXTRA, 0)
            val ringtonePath = intent.getStringExtra(Constants.REMINDER_RINGTONE_PATH_EXTRA)

            Log.d("MainActivity", "onCreate: $title $dateAdded $time")
            scheduleNotification(ReminderEntity(id = id, reminderName = title!!, dateAdded = dateAdded, ringtonePath = ringtonePath!!), time)
            //log id
            Log.d("MainActivity id", "onCreate: $id")
        }
    }


    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager.getNotificationChannel(Constants.DEFAULT_CHANNEL_ID) == null)
        {
            val name = Constants.NOTIFICATION_CHANNEL_NAME
            val descriptionText = Constants.NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.DEFAULT_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotification(reminder: ReminderEntity, time: Long) {
        val notificationIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(Constants.NOTIFICATION_ID, reminder.id.toInt())
            putExtra(Constants.TITLE_EXTRA, reminder.reminderName)
            putExtra(Constants.MESSAGE_EXTRA, "Don't Forget to do ${reminder.reminderName}")
            putExtra(Constants.RINGTONE_PATH_EXTRA, reminder.ringtonePath)
            putExtra(Constants.TIME_EXTRA, time)
            putExtra(Constants.SNOOZE_COUNTER, Constants.DEFAULT_SNOOZE_COUNTER)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            reminder.id.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
                        query?.let { searchReminder(it) }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        newText?.let { searchReminder(it) }
                        return true
                    }
                })
                true
            }
            R.id.action_delete_all -> {
                val alertDialog = MaterialAlertDialogBuilder(this)
                val adapter = recyclerView.adapter as ReminderAdapter
                val selectedItems = adapter.getSelectedItems()
                if (selectedItems.isNotEmpty()) {
                    alertDialog.setTitle("Delete Selected Reminders")
                    alertDialog.setMessage("Are you sure you want to delete ${selectedItems.size} selected reminders?")
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        selectedItems.apply {
                            forEachIndexed { index, reminder ->
                                runBlocking { deleteReminder(reminder.id) }
                                adapter.notifyItemRemoved(index)
                            }
                        }
                    }
                    alertDialog.setNegativeButton("No") { _, _ -> }
                } else {
                    alertDialog.setTitle("Delete All Reminders")
                    alertDialog.setMessage("Are you sure you want to delete all reminders?")
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        deleteAllReminders()
                    }
                    alertDialog.setNegativeButton("No") { _, _ -> }
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

    private fun loadData() {
        lifecycleScope.launch {
            getDatabase(this@MainActivity).reminderDao().getReminders().collect { reminders ->
                reminderList = reminders
                updateUIComponents()
            }
        }
    }

    //search reminder by title
    private fun searchReminder(reminderName: String?) {
        lifecycleScope.launch {
            val reminderDao = getDatabase(this@MainActivity).reminderDao()
            reminderDao.searchReminder(reminderName ?: "").collect { reminders ->
                reminderList = reminders
                updateUIComponents()
            }
        }
    }

    //delete reminder
    private fun deleteReminder(reminderId: Long) {
        lifecycleScope.launch {
            val reminderDao = getDatabase(this@MainActivity).reminderDao()
            val reminder = reminderDao.getReminder(reminderId.toInt()).apply {
                cancelAlarm(this.id)
            }
            reminderDao.deleteReminder(reminder)
            loadData()
        }
    }

    //delete all reminders
    private fun deleteAllReminders() {
        lifecycleScope.launch {
            val reminderDao = getDatabase(this@MainActivity).reminderDao()
            reminderDao.deleteAllReminders()
            loadData()
            //also cancel pending intent when all reminders are deleted
            cancelAllAlarm()
        }
    }

    private fun cancelAlarm(reminderId: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, reminderId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun cancelAllAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        for (reminder in reminderList) {
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, reminder.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun requestPermission() {
        val permissions = mapOf(
            android.Manifest.permission.READ_MEDIA_AUDIO to ::isReadMediaAudioPermissionGranted,
            android.Manifest.permission.POST_NOTIFICATIONS to ::isPostNotificationPermissionGranted,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW to ::isDisplayOverOtherAppsPermissionGranted
        )

        val permissionRequest = permissions.keys.filter { permission ->
            ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        permissionRequest.forEach { permission ->
            permissions[permission]?.set(false)
        }

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun updateUIComponents() {
        lifecycleScope.launch {
            try {
                // Update the adapter on the IO dispatcher.
                val newAdapter = withContext(Dispatchers.IO) {
                    ReminderAdapter(reminderList, object : ReminderAdapter.OnItemClickListener {
                        override fun onItemClick(reminder: ReminderEntity) {
                            // Handle the click event here to edit the reminder
                            val intent = Intent(this@MainActivity, CreateActivity::class.java).apply {
                                putExtra(Constants.REMINDER_ID_EXTRA, reminder.id)
                                putExtra(Constants.REMINDER_NAME_EXTRA, reminder.reminderName)
                                putExtra(Constants.REMINDER_DATE_EXTRA, reminder.dateAdded)
                                putExtra(Constants.REMINDER_RINGTONE_PATH_EXTRA, reminder.ringtonePath)
                            }
                            startActivity(intent)
                        }
                    })
                }
                // Check if the lifecycle is still at least STARTED before updating the RecyclerView adapter.
                if (isActive) {
                    recyclerView.adapter = newAdapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as AlarmService.AlarmBinder
        val alarmService = binder.getService()
        Log.d("MainActivity", "onServiceConnected: $alarmService")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d("MainActivity", "onServiceDisconnected: ")
    }

}