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
import com.example.myapplication.utils.Constants
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class ReminderAdapter(private val dataSet: List<ReminderEntity>) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val reminderName: TextView = view.findViewById(R.id.tv_title_name)
            val dateAdded: TextView = view.findViewById(R.id.tv_date_added)
            val timeAdded: TextView = view.findViewById(R.id.tv_time_added)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_title, parent, false)
            return ViewHolder(view)
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.reminderName.text = dataSet[position].reminderName
        val date = LocalDateTime.ofEpochSecond(dataSet[position].dateAdded, 0, ZoneId.systemDefault().rules.getOffset(Date().toInstant()))
        val dateStr = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d \nyyyy"))
        val timeStr = date.format(DateTimeFormatter.ofPattern("hh:mm a"))
        holder.dateAdded.text = dateStr
        holder.timeAdded.text = timeStr

        val clickedElement = deleteList.find() {
            if (it == dataSet[position].id.toString()) {
                return@find true
            }
            return@find false
        }

        if (clickedElement != null) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, com.google.android.material.R.color.material_blue_grey_800))
        }
        else {
            //set background color to blue
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, com.google.android.material.R.color.material_deep_teal_200))
        }

            holder.itemView.setOnLongClickListener {
                when {
                    clickedElement != null -> {
                        holder.itemView.setBackgroundColor(holder.itemView.context.getColor(com.google.android.material.R.color.material_dynamic_neutral70))
                        deleteList.remove(dataSet[position].id.toString())
                        notifyItemChanged(position)
                    }
                    else -> {
                        holder.itemView.setBackgroundColor(holder.itemView.context.getColor(com.google.android.material.R.color.material_dynamic_neutral10))
                        selectDeleteList(dataSet[position].id)
                        notifyItemChanged(position)
                    }
                }
                true
            }

            holder.itemView.setOnClickListener {
                when {
                    clickedElement != null -> {
                        holder.itemView.setBackgroundColor(holder.itemView.context.getColor(com.google.android.material.R.color.material_dynamic_neutral70))
                        deleteList.remove(dataSet[position].id.toString())
                        notifyItemChanged(position)
                    }
                    deleteList.isNotEmpty() -> {
                        holder.itemView.setBackgroundColor(holder.itemView.context.getColor(com.google.android.material.R.color.material_dynamic_neutral10))
                        selectDeleteList(dataSet[position].id)
                        notifyItemChanged(position)
                    }
                    else -> {
                        val context = holder.itemView.context
                        val intent = android.content.Intent(context, com.example.myapplication.UI.CreateActivity::class.java).apply {
                            putExtra(Constants.REMINDER_ID_EXTRA, dataSet[position].id)
                            putExtra(Constants.REMINDER_NAME_EXTRA, dataSet[position].reminderName)
                            putExtra(Constants.REMINDER_DATE_EXTRA, dataSet[position].dateAdded)
                            putExtra(Constants.REMINDER_RINGTONE_PATH_EXTRA, dataSet[position].ringtonePath)
                        }
                        context.startActivity(intent)
                    }
                }
            }


        }

    private fun selectDeleteList(id: Long) {
        deleteList.add(id.toString())
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}