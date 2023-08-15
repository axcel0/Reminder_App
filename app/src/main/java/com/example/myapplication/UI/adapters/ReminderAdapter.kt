package com.example.myapplication.UI.adapters

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.entities.ReminderEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class ReminderAdapter(private val dataSet: List<ReminderEntity>) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val reminderName: TextView = view.findViewById(R.id.tv_title_name)
            val dateAdded: TextView = view.findViewById(R.id.tv_date_added)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_title, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.reminderName.text = dataSet[position].reminderName
            val date = LocalDateTime.ofEpochSecond(dataSet[position].dateAdded, 0, ZoneId.systemDefault().rules.getOffset(Date().toInstant()))
            val dateStr = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(date)
            holder.dateAdded.text = dateStr
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }
    //add hold function to select reminder
    fun holdReminder(position: Int): ReminderEntity {
        return dataSet[position]
    }
    //add delete function to delete selected items
    fun deleteReminder(position: Int) {
        dataSet.drop(position).also { notifyItemRemoved(position) }
    }
    //add search function to search for reminders
    fun searchReminder(reminderName: String) {
        dataSet.filter { it.reminderName.contains(reminderName) }.also { notifyDataSetChanged() }
    }

}