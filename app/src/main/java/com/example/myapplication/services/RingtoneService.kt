package com.example.myapplication.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager

class RingtoneService : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {

        val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val alarmSound = RingtoneManager.getRingtone(context, ringtone)
        alarmSound.play()

    }

}