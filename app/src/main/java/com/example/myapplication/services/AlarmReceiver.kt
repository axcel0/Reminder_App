package com.example.myapplication.services

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
//import com.example.myapplication.services.NotificationService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle the alarm event here
        // You can show the AlertDialog or any other action you want
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Alarm")
            .setMessage("Time to wake up!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
        //start notification service when date is reached
        val service = Intent(context, NotificationService::class.java)
        service.putExtra("reason", "notification")
        service.putExtra("timestamp", intent?.getLongExtra("timestamp", 0))
        service.putExtra("reminderBody", intent?.getStringExtra("reminderBody"))
        context?.startService(service)

    }

}