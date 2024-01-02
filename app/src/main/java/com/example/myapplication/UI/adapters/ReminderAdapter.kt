package com.example.myapplication.UI.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.myapplication.databinding.CardViewTitleBinding
import com.example.myapplication.models.entities.ReminderEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(private val dataSet: List<ReminderEntity>, private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(reminder: ReminderEntity)
    }

    inner class ViewHolder(private val binding: CardViewTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener {
                toggleSelection(bindingAdapterPosition)
                true
            }
            binding.root.setOnClickListener {
                if (isItemSelected(bindingAdapterPosition)) {
                    deselectItem(bindingAdapterPosition)
                } else if (isAnyItemSelected()) {
                    selectItem(bindingAdapterPosition)
                } else {
                    itemClickListener.onItemClick(dataSet[bindingAdapterPosition])
                }
            }
        }

        fun bind(reminder: ReminderEntity) {
            binding.tvTitleName.text = reminder.reminderName
            val date = reminder.dateAdded.let { LocalDateTime.ofEpochSecond(it, 0, ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())) }
            val dateStr = date?.format(DateTimeFormatter.ofPattern("EEEE, MMMM d \nyyyy"))
            val timeStr = date?.format(DateTimeFormatter.ofPattern("hh:mm a"))
            binding.tvDateAdded.text = dateStr
            binding.tvTimeAdded.text = timeStr

            if (isItemSelected(bindingAdapterPosition)) {
                binding.cardViewTitle.setCardBackgroundColor(binding.root.context.getColor(com.google.android.material.R.color.material_blue_grey_800))
            } else {
                binding.cardViewTitle.setCardBackgroundColor(binding.root.context.getColor(com.google.android.material.R.color.material_deep_teal_200))
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
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardViewTitleBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun getSelectedItems(): List<ReminderEntity> {
        return selectedItems.map { position -> dataSet[position] }
    }

}