package com.example.myapplication.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle the alarm event here
        // You can show the AlertDialog or any other action you want
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Notification")
                .setMessage("Time to wake up!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
        }?.show()
        //start notification service when date is reached
        val service = Intent(context, AlarmReceiver::class.java)
        service.putExtra("reason", "notification")
        service.putExtra("timestamp", intent?.getLongExtra("timestamp", 0))
        service.putExtra("reminderBody", intent?.getStringExtra("reminderBody"))
        context?.startService(service)

    }


}