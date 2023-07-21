package com.example.myapplication.utils
import androidx.room.TypeConverter

object Converters {
    @TypeConverter
    fun fromTimestamp( value: Long?) :
            java.sql.Date {
        return java.sql.Date(value ?: 0)
    }
    @TypeConverter
    fun dateToTimestamp(date :java.util.Date?)
            :Long {
        return date?.time ?: 0
    }
}