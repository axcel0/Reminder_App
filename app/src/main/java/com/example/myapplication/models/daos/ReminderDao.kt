package com.example.myapplication.models.daos

import com.example.myapplication.Reminder

interface ReminderDao {
    fun getReminders(): List<Reminder>
    fun getReminder(id: Int): Reminder
    fun insertReminder(reminder: Reminder)
    fun deleteReminder(reminder: Reminder)
    fun updateReminder(reminder: Reminder)

}