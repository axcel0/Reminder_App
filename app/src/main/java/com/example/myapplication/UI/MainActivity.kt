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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.myapplication.R
import com.example.myapplication.UI.adapters.ReminderAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.models.AppDatabase
import com.example.myapplication.models.entities.ReminderEntity
import com.example.myapplication.services.AlarmReceiver
import com.example.myapplication.services.AlarmService
import com.example.myapplication.utils.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ServiceConnection {
    private var reminderList: List<ReminderEntity> = emptyList()
    private var isPostNotificationPermissionGranted = false
    private var isReadMediaAudioPermissionGranted = false
    private var isDisplayOverOtherAppsPermissionGranted = false
    private var uiScope: CoroutineScope? = null

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView

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
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        menuInflater.inflate(R.menu.menu_main, binding.toolbar.menu)

        recyclerView = binding.recyclerView
        recyclerView.adapter = ReminderAdapter(reminderList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        supportActionBar!!.title = getString(R.string.action_bar_name)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("MainActivity", "${it.key} = ${it.value}")
            }
        }

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
                if (deleteList.size > 0) {
                    alertDialog.setTitle("Delete Selected Reminders ")
                    alertDialog.setMessage("Are you sure you want to delete ${deleteList.size} selected reminders?")
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        deleteList.forEach {
                            deleteReminder(it.toLong())
                            deleteList = ArrayList()
                            //cancel alarm
                            cancelAlarm(it.toLong())
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
                            cancelAllAlarm()
                        }
                        alertDialog.setNegativeButton("No") { _, _ ->
                            deleteList = ArrayList()
                        }
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
        reminderList = reminderDao.searchReminder(reminderName ?: "").also { updateUIComponents() }
    }

    //delete reminder
    private fun deleteReminder(reminderId: Long) {
        val reminderDao = getDatabase(this).reminderDao()
        reminderDao.deleteReminder(reminderDao.getReminder(reminderId.toInt())).also {
            loadData()
        }
    }

    //delete all reminders
    private fun deleteAllReminders() {
        val reminderDao = getDatabase(this).reminderDao()
        reminderDao.deleteAllReminders().also {
            loadData()
            //aldo cancel pending intent when all reminders are deleted
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

    //update data reminder by id
    fun updateReminder(reminderId: Long, reminderName: String, dateAdded: Long, ringtoneName: String) {
        val reminderDao = getDatabase(this).reminderDao()
        reminderDao.updateReminder(ReminderEntity(id = reminderId, reminderName = reminderName, dateAdded = dateAdded, ringtonePath = ringtoneName)).also { loadData() }
    }

    private fun requestPermission() {
        val permissionRequest: MutableList<String> = ArrayList()

        isReadMediaAudioPermissionGranted =
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        isPostNotificationPermissionGranted =
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        isDisplayOverOtherAppsPermissionGranted =
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED

        if (!isReadMediaAudioPermissionGranted)
            permissionRequest.add(android.Manifest.permission.READ_MEDIA_AUDIO)

        if (!isPostNotificationPermissionGranted)
            permissionRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)

        if (!isDisplayOverOtherAppsPermissionGranted)
            permissionRequest.add(android.Manifest.permission.SYSTEM_ALERT_WINDOW)

        if (permissionRequest.isNotEmpty())
            permissionLauncher.launch(permissionRequest.toTypedArray())
    }

    private fun updateUIComponents() {
        // Cancel the previous UI scope if it is running.
        uiScope?.cancel()

        // Create a new UI scope with a SupervisorJob.
        // This will ensure that the UI scope is not cancelled if one of the coroutines it launches fails.
        uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        // Launch a coroutine to update the adapter.
        uiScope?.launch {
            try {
                // Update the adapter on the IO dispatcher.
                val newAdapter = withContext(Dispatchers.IO) {
                    ReminderAdapter(reminderList)
                }

                // Update the RecyclerView adapter on the main thread.
                recyclerView.adapter = newAdapter
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