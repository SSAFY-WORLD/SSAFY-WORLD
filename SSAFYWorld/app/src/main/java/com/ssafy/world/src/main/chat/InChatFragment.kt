package com.ssafy.world.src.main.chat

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.databinding.FragmentInChatBinding
import com.ssafy.world.src.main.MainActivity
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.HashMap


class InChatFragment :
	BaseFragment<FragmentInChatBinding>(FragmentInChatBinding::bind, R.layout.fragment_in_chat) {
	private val chatViewModel: ChatViewModel by viewModels()
	private val conversion: InChatFragmentArgs by navArgs()
	private val chatMessages = ArrayList<ChatMessage>()
	private lateinit var chatAdapter: ChatAdapter
	private var conversionId: String? = null
	private lateinit var activity: MainActivity

	override fun onAttach(context: Context) {
		super.onAttach(context)
		activity = (context as MainActivity)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setListeners()
		initView()
		loadReceiverDetails()
		observeConversionId()
		listenMessages()
	}

	private fun listenMessages() {
		CoroutineScope(Dispatchers.Main).launch {
			Firebase.firestore.collection(Constants.KEY_COLLECTION_CHAT)
				.whereEqualTo(Constants.KEY_SENDER_ID, "100")
				.whereEqualTo(Constants.KEY_RECEIVER_ID, "200")
				.addSnapshotListener(eventListener)

			Firebase.firestore.collection(Constants.KEY_COLLECTION_CHAT)
				.whereEqualTo(Constants.KEY_SENDER_ID, "200")
				.whereEqualTo(Constants.KEY_RECEIVER_ID, "100")
				.addSnapshotListener(eventListener)
		}
	}

	private val eventListener = EventListener<QuerySnapshot> { value, error ->
		if (error != null) {
			return@EventListener
		}
		if (value != null) {
			val count = chatMessages.size
			for (documentChange in value.documentChanges) {
				if (documentChange.type == DocumentChange.Type.ADDED) {
					val chatMessage = ChatMessage(
						senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!,
						receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!,
						message = documentChange.document.getString(Constants.KEY_MESSAGE)!!,
						dateObject = documentChange.document.getLong(Constants.KEY_TIMESTAMP)!!,
					)
					chatMessages.add(chatMessage)
				}
			}
			chatMessages.sortBy { it.dateObject }
			if (count == 0) {
				chatAdapter.notifyDataSetChanged()
			} else {
				chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
				binding.chattingRecyclerview.smoothScrollToPosition(chatMessages.size)
			}
		}
	}

	private fun observeConversionId() {
		chatViewModel.conversionId.observe(viewLifecycleOwner) {
			conversionId = it
		}
	}

	private fun loadReceiverDetails() {
		activity.setTitle(conversion.user.name)
	}

	private fun initView() = with(binding) {
		// 나중엔 로그인후 sharedPreference에 있는 id로 넣어준다
		chatAdapter = ChatAdapter(chatMessages, "100")
		chattingRecyclerview.adapter = chatAdapter

		if (conversionId == null) {
			chatViewModel.checkForConversion("100", "200")
		}
	}

	private fun setListeners() = with(binding) {
		addImage.setOnClickListener {
		}

		sendMessage.setOnClickListener {
			if (messageInput.text.isNotBlank()) {
				sendMessage(messageInput.text.toString())
			}
		}

	}

	private fun sendMessage(message: String) {
		// 테스트
		val chat = ChatMessage(
			"1",
			"2",
			message,
			System.currentTimeMillis(),
			true
		)
		chatViewModel.sendMessage(chat)
		if (conversionId == null) {
			val conversion = HashMap<String, Any>()
			conversion[Constants.KEY_SENDER_ID] = "100"
			conversion[Constants.KEY_SENDER_NAME] = "홍길동"
			conversion[Constants.KEY_SENDER_IMAGE] = "image"
			conversion[Constants.KEY_RECEIVER_ID] = "200"
			conversion[Constants.KEY_RECEIVER_NAME] = "길동홍"
			conversion[Constants.KEY_RECEIVER_IMAGE] = "image"
			conversion[Constants.KEY_LAST_MESSAGE] = message
			conversion[Constants.KEY_TIMESTAMP] = System.currentTimeMillis()
			chatViewModel.addConversion(conversion)
		} else {
			chatViewModel.updateConversion(conversionId!!, message)
		}
		binding.messageInput.text = null
	}
}