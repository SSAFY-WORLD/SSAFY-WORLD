package com.ssafy.world.src.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.world.data.model.Calendar
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.ItemCalendarBinding
import com.ssafy.world.utils.getFormattedTime

class MainCalendarAdapter() : ListAdapter<Calendar, MainCalendarAdapter.MyViewHolder>(ItemComparator) {

    companion object ItemComparator : DiffUtil.ItemCallback<Calendar>() {
        override fun areItemsTheSame(oldItem: Calendar, newItem: Calendar): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Calendar, newItem: Calendar): Boolean {
            // Item의 id 값으로 비교한다. object에 id가 있으면 그 id를 사용하여 비교.
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainCalendarAdapter.MyViewHolder {
        val binding = ItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainCalendarAdapter.MyViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }

    override fun getItemCount(): Int {
        // 최신 아이템 위에 3개만 보여주기 위해 아이템 개수를 조정
        return minOf(super.getItemCount(), 3)
    }

    interface ItemClickListener {
        fun onClick(data: Calendar)
    }

    lateinit var itemClickListener: ItemClickListener



    inner class MyViewHolder(private val binding: ItemCalendarBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Calendar) = with(binding) {
            title.text = data.title
            continueDay.text = data.startTime
            preiod.text = "${data.startTime} ~ ${data.endTime}"

            itemView.setOnClickListener {
                itemClickListener.onClick(data)
            }
        }
    }
}