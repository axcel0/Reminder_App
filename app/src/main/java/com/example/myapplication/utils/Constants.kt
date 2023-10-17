package com.example.myapplication.utils

class Constants {
    companion object {
        //region Notification Channel
        const val NOTIFICATION_ID = "notificationID"
        const val DEFAULT_CHANNEL_ID = "Reminder"
        const val NOTIFICATION_CHANNEL_NAME = "Reminder"
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "Reminder"
        //endregion

        //region Alarm
        const val SNOOZE_COUNTER = "snoozeCounter"
        const val DEFAULT_SNOOZE_COUNTER = 5
        const val DEFAULT_SNOOZE_TIME : Long = 300_000 // In milliseconds
        //endregion

        //region Notification Extras
        const val TITLE_EXTRA = "title"
        const val MESSAGE_EXTRA = "message"
        const val RINGTONE_PATH_EXTRA = "ringtonePath"
        const val TIME_EXTRA = "time"
        //endregion

        //region Reminder Extras
        const val REMINDER_ID_EXTRA = "reminderId"
        const val REMINDER_NAME_EXTRA = "reminderName"
        const val REMINDER_DATE_EXTRA = "reminderDate"
        const val REMINDER_TIME_EXTRA = "reminderTime"
        const val REMINDER_RINGTONE_PATH_EXTRA = "reminderRingtonePath"
        //endregion
    }
}