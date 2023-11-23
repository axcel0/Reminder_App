package com.example.myapplication.models.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [Index(
        value = ["reminderName"],
        unique = true
    )]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "reminderName") val reminderName: String,
    @ColumnInfo(name = "dateAdded") val dateAdded: Long,
    @ColumnInfo(name = "ringtonePath") val ringtonePath: String,
) {
    override fun toString(): String {
        return "ReminderEntity(id=$id, reminderName='$reminderName', dateAdded=$dateAdded, ringtonePath='$ringtonePath')"
    }
}