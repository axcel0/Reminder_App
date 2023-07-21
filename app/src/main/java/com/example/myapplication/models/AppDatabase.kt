package com.example.myapplication.models

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.models.daos.ReminderDao
import com.example.myapplication.models.entities.ReminderEntity
import com.example.myapplication.utils.Converters

@Database(entities = [ReminderEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}