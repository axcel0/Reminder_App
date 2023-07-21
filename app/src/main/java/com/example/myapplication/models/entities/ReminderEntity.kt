package com.example.myapplication.models.entities

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.Index
import java.sql.Time
import java.time.LocalDateTime
import java.util.Date

@Entity(
    tableName = "reminders",
    indices = [Index(
        value = ["reminderName"],
        unique = true
    )]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "reminderName") val reminderName: String,
    @ColumnInfo(name = "dateAdded") val dateAdded: Long,
)
