package com.ssafy.world.src.main.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.FragmentInChatBinding
import com.ssafy.world.src.main.MainActivity
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.HashMap

private const val TAG = "InChatFragment_싸피"

class InChatFragment : Fragment() {
	private val chatViewModel: ChatViewModel by viewModels()
	private val args: InChatFragmentArgs by navArgs()
	private val chatMessages = ArrayList<ChatMessage>()
	private lateinit var chatAdapter: ChatAdapter
	private var conversionId: String? = null
	private lateinit var receiverId: String
	private lateinit var receiverName: String
	private lateinit var receiverImage: String
	// 현재 로그인된 id
	private lateinit var sendUser: User
	private lateinit var binding: FragmentInChatBinding
	private val db: FirebaseFirestore by lazy { Firebase.firestore }

	private var chatListenerRegistration1: ListenerRegistration? = null
	private var chatListenerRegistration2: ListenerRegistration? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentInChatBinding.inflate(inflater, container, false)
		return binding.root
	}
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		loadReceiverDetails()
		setListeners()
		initView()
		listenMessages()
	}

	private fun listenMessages() {
		val chatRef1 = Firebase.firestore.collection(Constants.KEY_COLLECTION_CHAT)
			.whereEqualTo(Constants.KEY_SENDER_ID, sendUser.email)
			.whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
			.orderBy(Constants.KEY_TIMESTAMP, Query.Direction.ASCENDING)

		val chatRef2 = Firebase.firestore.collection(Constants.KEY_COLLECTION_CHAT)
			.whereEqualTo(Constants.KEY_SENDER_ID, receiverId)
			.whereEqualTo(Constants.KEY_RECEIVER_ID, sendUser.email)
			.orderBy(Constants.KEY_TIMESTAMP, Query.Direction.ASCENDING)

		// 실시간 업데이트를 위해 단일 문서 참조에 대한 리스너를 등록.
		chatListenerRegistration1 = chatRef1.addSnapshotListener(eventListener)
		chatListenerRegistration2 = chatRef2.addSnapshotListener(eventListener)
	}


	private val eventListener = EventListener<QuerySnapshot> { value, error ->
		if (error != null) {
			return@EventListener
		}
		if (value != null) {
			val count = chatMessages.size
			for (documentChange in value.documentChanges) {
				if (documentChange.type == DocumentChange.Type.ADDED) {
					// 상대방인지 확인
					val isOtherRead = documentChange.document.getString(Constants.KEY_RECEIVER_ID) == sendUser.email
					// 로그인한 유저가 보내는 메시지
					val chatMessage = ChatMessage(
						senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!,
						senderImage = documentChange.document.getString(Constants.KEY_SENDER_IMAGE)!!,
						receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!,
						message = documentChange.document.getString(Constants.KEY_MESSAGE)!!,
						dateObject = documentChange.document.getLong(Constants.KEY_TIMESTAMP)!!,
						isRead = false
					)
					chatMessages.add(chatMessage)
					if (isOtherRead) {
						// 상대방이 메시지를 읽었을 때 isRead 값을 true로 업데이트합니다.
 						markMessageAsRead(documentChange.document.id)
					}
				}
			}
			chatMessages.sortBy { it.dateObject }
			if (count == 0) {
				chatAdapter.notifyDataSetChanged()
			} else {
				chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
				binding.chattingRecyclerview.smoothScrollToPosition(chatMessages.size)
			}
			if (conversionId == null) {
				checkForConversion()
			}
		}
	}

	private fun markMessageAsRead(chatId: String) {
		val chatRef = db.collection(Constants.KEY_COLLECTION_CHAT).document(chatId)
		chatRef.update(Constants.KEY_IS_READ, true)
	}

	private fun loadReceiverDetails() {
		args.user?.let {
			receiverId = it.id
			receiverName = it.name
			receiverImage = it.image
		}
		args.UserFromProfile?.let {
			receiverId = it.email
			receiverName = it.name
			receiverImage = it.profilePhoto
		}
		(requireActivity() as MainActivity).setTitle(receiverName)
		sendUser = ApplicationClass.sharedPreferences.getUser()!!
	}

	private fun initView() = with(binding) {
		chatAdapter = ChatAdapter(chatMessages, sendUser.email)
		chattingRecyclerview.adapter = chatAdapter
	}

	private fun setListeners() = with(binding) {
		addImage.setOnClickListener {
			// 이미지 보내기 TODO
		}

		sendMessage.setOnClickListener {
			if (messageInput.text.isNotBlank()) {
				sendMessage(messageInput.text.toString(), sendUser, receiverId)
			}
		}

	}

	private fun sendMessage(message: String, sendUser: User, receiverId: String) {
		val chat = ChatMessage(
			sendUser.email,
			sendUser.profilePhoto,
			receiverId,
			message,
			System.currentTimeMillis()
		)
		chatViewModel.sendMessage(chat, sendUser, receiverId)
		if (conversionId == null) {
			val conversion = HashMap<String, Any>()
			conversion[Constants.KEY_SENDER_ID] = sendUser.email
			conversion[Constants.KEY_SENDER_NAME] = sendUser.name
			conversion[Constants.KEY_SENDER_IMAGE] = sendUser.profilePhoto
			conversion[Constants.KEY_RECEIVER_ID] = receiverId
			conversion[Constants.KEY_RECEIVER_NAME] = receiverName
			conversion[Constants.KEY_RECEIVER_IMAGE] = receiverImage
			conversion[Constants.KEY_LAST_MESSAGE] = message
			conversion[Constants.KEY_TIMESTAMP] = chat.dateObject
			addConversion(conversion)
		} else {
			updateConversion(message, chat)
		}
		binding.messageInput.text = null
	}

	private fun updateConversion(message: String, chat: ChatMessage) {
		val documentReference =
			conversionId?.let { db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(it) }

		documentReference?.update(
			Constants.KEY_LAST_MESSAGE, message,
			Constants.KEY_TIMESTAMP, chat.dateObject,
		)
	}

	private fun addConversion(conversion: HashMap<String, Any>) {
		db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
			.add(conversion)
			.addOnSuccessListener { documentReference ->
				conversionId = documentReference.id
			}
	}

	private fun checkForConversion() {
		if (chatMessages.isNotEmpty()) {
			CoroutineScope(Dispatchers.IO).launch {
				checkForConversationRemotely(sendUser.email, receiverId)
			}
		}
	}
	private fun checkForConversationRemotely(senderId: String, receiverId: String) {
		db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
			.whereEqualTo(Constants.KEY_SENDER_ID, senderId)
			.whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
			.get()
			.addOnCompleteListener(conversationOnCompleteListener)
		db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
			.whereEqualTo(Constants.KEY_SENDER_ID, receiverId)
			.whereEqualTo(Constants.KEY_RECEIVER_ID, senderId)
			.get()
			.addOnCompleteListener(conversationOnCompleteListener)
	}

	private val conversationOnCompleteListener = OnCompleteListener<QuerySnapshot> { task ->
		if (task.isSuccessful && task.result != null && task.result.documents.isNotEmpty()) {
			val documentSnapshot = task.result.documents[0]
			conversionId = documentSnapshot.id
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		chatListenerRegistration1?.remove() // 기존 리스너 제거
		chatListenerRegistration2?.remove() // 기존 리스너 제거
	}
}