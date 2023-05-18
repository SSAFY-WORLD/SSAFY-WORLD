package com.ssafy.world.src.main.chat

import android.os.Bundle
import android.view.View
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.databinding.FragmentChatBinding
import java.util.*


class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::bind, R.layout.fragment_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = arrayListOf<ChatMessage>(
            ChatMessage("1", "2", "테스트 메시지1", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지2", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("2", "1", "테스트 메시지 테스트 메시지 테스트 메시지 테스트 메시지 테스트 메시지", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지4", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("2", "1", "테스트 메시지5", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("2", "1", "테스트 메시지6", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지 테스트 메시지 테스트 메시지 테스트 메시지 테스트 메시지", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지1", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지2", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("2", "1", "테스트 메시지 테스트 메시지 테스트 메시지 테스트 메시지 테스트 메시지", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지4", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("2", "1", "테스트 메시지5", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("2", "1", "테스트 메시지6", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지 테스트 메시지 테스트 메시지 테스트 메시지 테스트 메시지", "18.12", Date(), "1", "홍길동", "image"),
            ChatMessage("1", "2", "테스트 메시지", "18.12", Date(), "1", "홍길동", "image"),
        )
        binding.chattingRecyclerview.adapter = ChatAdapter(list, "1")
    }

}