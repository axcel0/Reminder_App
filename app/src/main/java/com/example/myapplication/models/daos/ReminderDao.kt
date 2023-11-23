package com.example.myapplication.models.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.models.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminder(id: Int): ReminderEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    //search reminder by name
    @Query("SELECT * FROM reminders WHERE reminderName LIKE '%' || :reminderName || '%'")
    fun searchReminder(reminderName: String): Flow<List<ReminderEntity>>
}