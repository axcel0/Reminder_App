package com.example.myapplication.UI.adapters

import android.content.Context
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.UI.MainActivity
import com.example.myapplication.UI.MainActivity.Companion.deleteList
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

            val clickedElement = deleteList.find() {
                it == dataSet[position].id.toString()
            }

            if (clickedElement != null) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.material_dynamic_primary_light))
            }
            else {
                //set background color to blue
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.material_dynamic_primary_dark))
            }

            holder.itemView.setOnLongClickListener{
                selectDeleteList(dataSet[position].id)
                notifyDataSetChanged()
                return@setOnLongClickListener true
            }

            holder.itemView.setOnClickListener{

                if (clickedElement != null) {
                    holder.itemView.setBackgroundColor(holder.itemView.context.resources.getColor(R.color.purple))
                    deleteList.remove(dataSet[position].id.toString())
                    notifyDataSetChanged()
                }

            }

        }

    private fun selectDeleteList(id: Long) {
        deleteList.add(id.toString())
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