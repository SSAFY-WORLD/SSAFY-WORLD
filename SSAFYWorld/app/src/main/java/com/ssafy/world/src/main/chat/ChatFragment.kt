package com.ssafy.world.src.main.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.ConversationUser
import com.ssafy.world.databinding.FragmentChatBinding
import com.ssafy.world.utils.Constants
import java.util.*


class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::bind, R.layout.fragment_chat), ConversionListener  {

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatRoomList: RecentConversationAdapter
    private var conversionId: String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatRoomList = RecentConversationAdapter(this)
        binding.chattingRecyclerview.adapter = chatRoomList
        chatViewModel.conversionId.observe(viewLifecycleOwner) {
            conversionId = it
        }
        chatRoomList.submitList(
            mutableListOf(
                ChatMessage("1", "2", "테스트 메시지1", System.currentTimeMillis(), true, "200", "홍길동", "image")
            )
        )
//        chatViewModel.chatRooms.observe(viewLifecycleOwner) {
//            binding.chattingRecyclerview.smoothScrollToPosition(0)
//        }

        binding.fabNewChat.setOnClickListener {
        }
    }

    override fun onConversionClicked(user: ConversationUser) {
        val action = ChatFragmentDirections.actionChatFragmentToInChatFragment(user)
        findNavController().navigate(action)
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