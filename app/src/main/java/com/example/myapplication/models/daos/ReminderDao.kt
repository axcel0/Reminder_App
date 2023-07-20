package com.example.myapplication.models.daos

import androidx.room.Query
import com.example.myapplication.Reminder

interface ReminderDao {

    fun getReminders(): List<Reminder>
    @Query("SELECT * FROM reminders WHERE id = :id")

    fun getReminder(id: Int): Reminder
    fun insertReminder(reminder: Reminder)
    fun deleteReminder(reminder: Reminder)
    fun updateReminder(reminder: Reminder)

}