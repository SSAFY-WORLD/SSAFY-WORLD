package com.ssafy.world.src.main.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.world.data.local.entity.NotificationEntity
import com.ssafy.world.databinding.ItemNotificationBinding
import com.ssafy.world.utils.getFormattedTime

class NotificationListAdapter:
    ListAdapter<NotificationEntity, NotificationListAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }

    interface ItemClickListener {
        fun onClick(view: View, data: NotificationEntity)
    }

    lateinit var itemClickListener: ItemClickListener

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: NotificationEntity) = with(binding) {
            title.text = data.title
            message.text = data.message
            notificationTime.text = getFormattedTime(data.receiveTime)
            //notificationDestination.text = data.destination.split("-")[0]
            itemView.setOnClickListener {
                itemClickListener.onClick(it, data)
            }
        }
    }

}
