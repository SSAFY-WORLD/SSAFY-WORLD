package com.ssafy.world.src.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.world.R
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.ConversationUser
import com.ssafy.world.databinding.ChatroomItemBinding
import com.ssafy.world.utils.getReadableDateTime


class RecentConversationAdapter(
    private val conversionListener: ConversionListener
) : ListAdapter<ChatMessage, RecentConversationAdapter.ConversionViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val binding =
            ChatroomItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(currentList[position])
        // 클릭시
        holder.itemView.setOnClickListener {
            val chat = currentList[position]
            val user = ConversationUser(
                id = chat.conversionId,
                name = chat.conversionName,
                image = chat.conversionImage
            )
            conversionListener.onConversionClicked(user)
        }
    }

    inner class ConversionViewHolder(private val binding: ChatroomItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            with(binding) {
                userName.text = chatMessage.conversionName
                userCurrentMessage.text = chatMessage.message
                messageTime.text = getReadableDateTime(chatMessage.dateObject)

                Glide.with(binding.root)
                    .load(chatMessage.conversionImage)
                    .error(R.drawable.default_profile_image)
                    .into(profileImage)
            }
        }
    }
}

interface ConversionListener {
    fun onConversionClicked(user: ConversationUser)
}