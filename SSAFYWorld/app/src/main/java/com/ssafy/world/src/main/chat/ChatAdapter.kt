package com.ssafy.world.src.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.world.R
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.databinding.ItemReceivedMessageBinding
import com.ssafy.world.databinding.ItemSentMessageBinding
import com.ssafy.world.utils.getReadableDateTime

class ChatAdapter(
	private val chatList: List<ChatMessage>,
	private val senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	companion object {
		private const val VIEW_TYPE_SENT = 1
		private const val VIEW_TYPE_RECEIVED = 2
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
				if (chatMessage.senderImage != "") {
					Glide.with(binding.root)
						.load(chatMessage.senderImage)
						.thumbnail(
							Glide.with(binding.root).load(chatMessage.conversionImage)
								.override(100, 100)
						)
						.skipMemoryCache(true)
						.dontAnimate()
						.into(profileImage)
				} else {
					profileImage.setImageResource(R.drawable.default_profile_image)
				}
			}
		}
	}

}