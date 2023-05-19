package com.ssafy.world.src.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {
    private val chatRoomRepository = ChatRepository
    private val _chatRooms = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatRooms: StateFlow<List<ChatMessage>>
        get() = _chatRooms

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>>
        get() = _chatMessages

    init {
        observeChatRooms()
    }

    private fun observeChatRooms() {
        viewModelScope.launch {
            chatRoomRepository.observeChatRooms().collect { chatRooms ->
                _chatRooms.value = chatRooms
            }
        }
    }
    fun sendMessage(message: ChatMessage) {
        viewModelScope.launch {
            chatRoomRepository.sendMessage(message)
        }
    }
    fun observeChatMessages() {
        viewModelScope.launch {
            chatRoomRepository.observeChatMessages().collect { chatMessages ->
                _chatMessages.value = chatMessages
            }
        }
    }
}