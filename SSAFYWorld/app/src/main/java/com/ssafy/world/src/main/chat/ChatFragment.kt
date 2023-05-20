package com.ssafy.world.src.main.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.ConversationUser
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.FragmentChatBinding


class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::bind, R.layout.fragment_chat), ConversionListener  {

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var conversationAdapter: RecentConversationAdapter
    private var conversionId: String? = null
    private var chatRoomList: ArrayList<ChatMessage> = arrayListOf()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conversationAdapter = RecentConversationAdapter(this)
        binding.chattingRecyclerview.adapter = conversationAdapter
        chatViewModel.conversionId.observe(viewLifecycleOwner) {
            conversionId = it
        }
        conversationAdapter.submitList(chatRoomList.toMutableList())
        chatViewModel.chatRooms.observe(viewLifecycleOwner) {
            binding.chattingRecyclerview.smoothScrollToPosition(0)
        }

        binding.fabNewChat.setOnClickListener {
            val action = ChatFragmentDirections.actionChatFragmentToUserFragment()
            findNavController().navigate(action)
        }
    }

    override fun onConversionClicked(user: ConversationUser) {
        val action = ChatFragmentDirections.actionChatFragmentToInChatFragment(user, null)
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