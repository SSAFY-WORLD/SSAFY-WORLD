package com.ssafy.world.src.main.chat

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.databinding.ItemReceivedMessageBinding
import com.ssafy.world.databinding.ItemSentMessageBinding
import com.ssafy.world.utils.getReadableDateTime

class ChatAdapter(
	private val chatList: MutableList<ChatMessage>,
	private val senderId: String
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffUtil) {

	companion object {
		private const val VIEW_TYPE_SENT = 1
		private const val VIEW_TYPE_RECEIVED = 2
	}

	object ChatDiffUtil: DiffUtil.ItemCallback<ChatMessage>() {
		override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
			Log.d("싸피", "areItemsTheSame: ${oldItem.dateObject == newItem.dateObject}")
			return oldItem.dateObject == newItem.dateObject
		}

		override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
			return oldItem == newItem
		}

	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return if (viewType == VIEW_TYPE_SENT) {
			SentMessageViewHolder(
				ItemSentMessageBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
			)
		} else {
			ReceivedMessageViewHolder(
				ItemReceivedMessageBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
			)
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (getItemViewType(position) == VIEW_TYPE_SENT) {
			(holder as SentMessageViewHolder).setData(chatList[position])
		} else {
			(holder as ReceivedMessageViewHolder).setData(chatList[position])
		}
	}

	override fun getItemCount() = chatList.size

	// 보내는 사람을 확인하여 View Type을 확인
	override fun getItemViewType(position: Int): Int {
		return if (chatList[position].senderId == senderId) {
			VIEW_TYPE_SENT
		} else {
			VIEW_TYPE_RECEIVED
		}
	}

	inner class SentMessageViewHolder(private val binding: ItemSentMessageBinding) :
		RecyclerView.ViewHolder(binding.root) {

		fun setData(chatMessage: ChatMessage) {
			with(binding) {
				textMessage.text = chatMessage.message
				textDateTime.text = getReadableDateTime(chatMessage.dateObject)
			}
		}
	}

	inner class ReceivedMessageViewHolder(private val binding: ItemReceivedMessageBinding) :
		RecyclerView.ViewHolder(binding.root) {

		fun setData(chatMessage: ChatMessage) {
			with(binding) {
				textMessage.text = chatMessage.message
				textDateTime.text = getReadableDateTime(chatMessage.dateObject)
//				binding.profileImage.setImageBitmap(profile)
			}
		}
	}

}