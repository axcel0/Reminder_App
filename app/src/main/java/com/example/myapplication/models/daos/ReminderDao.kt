package com.example.myapplication.models.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.models.entities.ReminderEntity
import java.time.LocalDateTime
import java.util.Date

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminder(id: Int): ReminderEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertReminder(reminder: ReminderEntity): Long

    @Query("DELETE FROM reminders WHERE id = :id")
    fun deleteReminder(id: Long)

    @Query("UPDATE reminders SET reminderName = :reminderName, dateAdded = :dateAdded WHERE id = :id")
    fun updateReminder(id: Long, reminderName: String, dateAdded: Long)

}