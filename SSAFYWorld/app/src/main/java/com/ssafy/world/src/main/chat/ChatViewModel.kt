package com.ssafy.world.src.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.User
import com.ssafy.world.data.repository.chat.ChatRepository
import kotlinx.coroutines.launch
import java.util.HashMap

class ChatViewModel: ViewModel() {
    private val chatRoomRepository = ChatRepository

    fun sendMessage(message: ChatMessage, sendUser: User, receiverId: String) {
        viewModelScope.launch {
            chatRoomRepository.sendMessage(message, sendUser, receiverId)
        }
    }
}