package com.ssafy.world.src.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.repository.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.HashMap

class ChatViewModel: ViewModel() {
    private val chatRoomRepository = ChatRepository

    private val _conversionId = MutableLiveData<String?>(null)
    val conversionId: LiveData<String?>
        get() = _conversionId
    private val _chatRooms = MutableLiveData<List<ChatMessage>>()
    val chatRooms: LiveData<List<ChatMessage>>
        get() = _chatRooms

    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>>
        get() = _chatMessages

    init {
        observeChatRooms()
    }

    private fun observeChatRooms() {
        viewModelScope.launch {
            chatRoomRepository.listenConversations()
                .flowOn(Dispatchers.IO)
                .collect { chatRooms ->
                    _chatRooms.value = chatRooms
                }
        }
    }

    fun sendMessage(message: ChatMessage) {
        viewModelScope.launch {
            chatRoomRepository.sendMessage(message)
        }
    }
    fun observeChatMessages(receiverId: String) {
        viewModelScope.launch {
            chatRoomRepository.observeChatMessages(receiverId).collect { chatMessages ->
                _chatMessages.value = chatMessages
            }
        }
    }

    fun addConversion(conversion: HashMap<String, Any>) {
        viewModelScope.launch {
            val conversionId = chatRoomRepository.addConversion(conversion)
            if (conversionId != null) {
                _conversionId.value = conversionId
            }
        }
    }

    fun checkForConversion(senderId: String, receiverId: String) {
        viewModelScope.launch {
            val checkOne = chatRoomRepository.checkForConversationRemotely(senderId, receiverId)
            val checkTwo = chatRoomRepository.checkForConversationRemotely(receiverId, senderId)
            if (checkOne != null) {
                _conversionId.value = checkOne
            }
            if (checkTwo != null) {
                _conversionId.value = checkTwo
            }
        }
    }
    fun updateConversion(conversionId: String, message: String) {
        viewModelScope.launch {
            chatRoomRepository.updateConversion(conversionId, message)
        }
    }


}