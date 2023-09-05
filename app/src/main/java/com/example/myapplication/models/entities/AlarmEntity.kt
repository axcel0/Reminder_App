package com.example.myapplication.models.entities

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "ringtone",
    indices = [Index(
        value = ["ringtoneName"],
        unique = true
    )]
)
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "reminderName") val alarmName: String,
    @ColumnInfo(name = "dateAdded") val dateAdded: Long,
)

