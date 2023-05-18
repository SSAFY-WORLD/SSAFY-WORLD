package com.ssafy.world.src.main.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.databinding.ChatroomItemBinding


class RecentConversationAdapter(
    private val chatList: ArrayList<ChatMessage>,
    private val conversionListener: ConversionListener
) : RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val binding =
            ChatroomItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatList[position])
        // 클릭시
        holder.itemView.setOnClickListener {
            val chat = chatList[position]
            conversionListener.onConversionClicked(userId = chat.conversionId)
        }
    }

    override fun getItemCount(): Int  = chatList.size

    inner class ConversionViewHolder(private val binding: ChatroomItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            with(binding) {
                profileImage.setImageBitmap(getConversionImage(chatMessage.conversionImage))
                userName.text = chatMessage.conversionName
                userCurrentMessage.text = chatMessage.message
                messageTime.text = chatMessage.dateTime
            }
        }
    }

    private fun getConversionImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

}

interface ConversionListener {
    fun onConversionClicked(userId: String)
}