package com.example.myapplication.UI.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.entities.ReminderEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReminderAdapter(private val dataSet: List<ReminderEntity>, private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(reminder: ReminderEntity)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reminderName: TextView = view.findViewById(R.id.tv_title_name)
        val dateAdded: TextView = view.findViewById(R.id.tv_date_added)
        val timeAdded: TextView = view.findViewById(R.id.tv_time_added)
        val cardView: CardView = view.findViewById(R.id.card_view_title)

        init {
            itemView.setOnLongClickListener {
                toggleSelection(bindingAdapterPosition)
                true
            }
            itemView.setOnClickListener {
                if (isItemSelected(bindingAdapterPosition)) {
                    deselectItem(bindingAdapterPosition)
                } else if (isAnyItemSelected()) {
                    selectItem(bindingAdapterPosition)
                } else {
                    itemClickListener.onItemClick(dataSet[bindingAdapterPosition])
                }
            }
        }
    }

    private val selectedItems = mutableSetOf<Int>()

    private fun isItemSelected(position: Int) = selectedItems.contains(position)

    private fun isAnyItemSelected() = selectedItems.isNotEmpty()

    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
    }

    private fun selectItem(position: Int) {
        selectedItems.add(position)
        notifyItemChanged(position)
    }

    private fun deselectItem(position: Int) {
        selectedItems.remove(position)
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_title, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.reminderName.text = dataSet[position].reminderName
        val date = dataSet[position].dateAdded.let { LocalDateTime.ofEpochSecond(it, 0, ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())) }
        val dateStr = date?.format(DateTimeFormatter.ofPattern("EEEE, MMMM d \nyyyy"))
        val timeStr = date?.format(DateTimeFormatter.ofPattern("hh:mm a"))
        holder.dateAdded.text = dateStr
        holder.timeAdded.text = timeStr

        if (isItemSelected(position)) {
            holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(com.google.android.material.R.color.material_dynamic_primary60))
        } else {
            holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(com.google.android.material.R.color.material_dynamic_primary30))
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
    fun getSelectedItems(): List<ReminderEntity> {
        return selectedItems.map { position -> dataSet[position] }
    }
}