package com.ssafy.world.ui.main.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.world.R
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.ConversationUser
import com.ssafy.world.databinding.ChatroomItemBinding
import com.ssafy.world.utils.Constants
import com.ssafy.world.utils.getReadableDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class RecentConversationAdapter(
    private val senderId: String,
    private val chatRoomList: ArrayList<ChatMessage>,
    private val conversionListener: ConversionListener
) : RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val binding =
            ChatroomItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversionViewHolder(binding)
    }

    override fun getItemCount() = chatRoomList.size

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatRoomList[position])
        // 클릭시
        holder.itemView.setOnClickListener {
            val chat = chatRoomList[position]
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
                val receiverId = if (senderId == chatMessage.senderId) chatMessage.receiverId else chatMessage.senderId
                // 새로운 메시지의 경우 badge를 보여줌
                CoroutineScope(Dispatchers.IO).launch {
                    val unread = Firebase.firestore.collection(Constants.KEY_COLLECTION_CHAT)
                        .whereEqualTo(Constants.KEY_SENDER_ID, receiverId)
                        .whereEqualTo(Constants.KEY_RECEIVER_ID, senderId)
                        .whereEqualTo(Constants.KEY_IS_READ, false)
                        .get().await().count()
                    if (unread == 0) {
                        newBadge.visibility = View.INVISIBLE
                    } else {
                        withContext(Dispatchers.Main) {
                            newBadge.visibility = View.VISIBLE
                            unReadCount.text = unread.toString()
                        }
                    }
                }

                if (chatMessage.conversionImage.isNotBlank()) {
                    Glide.with(binding.root)
                        .load(chatMessage.conversionImage)
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

interface ConversionListener {
    fun onConversionClicked(user: ConversationUser)
}