package com.ssafy.world.src.main.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.databinding.FragmentChatBinding
import java.util.*


class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::bind, R.layout.fragment_chat) {

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatViewModel.observeChatMessages()
        chatAdapter = ChatAdapter(mutableListOf(), "200")
        binding.chattingRecyclerview.adapter = chatAdapter
//        observeChatRooms()
        binding.fabNewChat.setOnClickListener {
            // 테스트
            val message = ChatMessage("1", "2", "테스트 메시지1", System.currentTimeMillis(), true)
            chatViewModel.sendMessage(message)
        }
        lifecycleScope.launchWhenStarted {
            chatViewModel.chatMessages.collect { chatMessages ->
                chatAdapter.submitList(chatMessages.toMutableList())
            }
        }
    }
//
//    private fun observeChatRooms() {
//        lifecycleScope.launchWhenStarted {
//            chatViewModel.chatRooms.collect { chatRooms ->
//                binding.chattingRecyclerview.adapter = RecentConversationAdapter(chatRooms.toList())
//            }
//        }
//    }

}