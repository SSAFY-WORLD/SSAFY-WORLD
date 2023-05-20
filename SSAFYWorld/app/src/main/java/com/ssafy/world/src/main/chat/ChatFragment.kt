package com.ssafy.world.src.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.ConversationUser
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.FragmentChatBinding
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatFragment : Fragment(), ConversionListener  {

    private lateinit var binding: FragmentChatBinding
    private lateinit var conversationAdapter: RecentConversationAdapter
    private lateinit var sendUser: User
    private var chatRoomList: ArrayList<ChatMessage> = arrayListOf()
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendUser = ApplicationClass.sharedPreferences.getUser()!!
        initCallback()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListener()
    }


    private fun init() = with(binding) {
        conversationAdapter = RecentConversationAdapter(sendUser.email ,chatRoomList, this@ChatFragment)
        chattingRecyclerview.adapter = conversationAdapter
    }

    private fun setListener() = with(binding) {
        fabNewChat.setOnClickListener {
            val action = ChatFragmentDirections.actionChatFragmentToUserFragment()
            findNavController().navigate(action)
        }
    }

    override fun onConversionClicked(user: ConversationUser) {
        val action = ChatFragmentDirections.actionChatFragmentToInChatFragment(user, null)
        findNavController().navigate(action)
    }

    private fun initCallback() {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, sendUser.email)
                .addSnapshotListener(eventListener)
            db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, sendUser.email)
                .addSnapshotListener(eventListener)
        }
    }

    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            for (documentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                    val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                    CoroutineScope(Dispatchers.IO).launch {
                        if (sendUser.email == senderId) {
                            documentChange.document.apply {
                                val message = ChatMessage(
                                    senderId,
                                    receiverId!!,
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                                        .toString(),
                                    dateObject = getLong(Constants.KEY_TIMESTAMP)!!,
                                    conversionId = getString(Constants.KEY_RECEIVER_ID).toString(),
                                    conversionName = getString(Constants.KEY_RECEIVER_NAME).toString(),
                                    conversionImage = getString(Constants.KEY_RECEIVER_IMAGE).toString(),
                                )
                                chatRoomList.add(message)
                            }
                        } else {
                            documentChange.document.apply {
                                val message = ChatMessage(
                                    senderId!!,
                                    receiverId!!,
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                                        .toString(),
                                    dateObject = getLong(Constants.KEY_TIMESTAMP)!!,
                                    conversionId = getString(Constants.KEY_SENDER_ID).toString(),
                                    conversionName = getString(Constants.KEY_SENDER_NAME).toString(),
                                    conversionImage = getString(Constants.KEY_SENDER_IMAGE).toString(),
                                )
                                chatRoomList.add(message)
                            }
                        }
                    }
                } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                    for (i in 0 until chatRoomList.size) {
                        val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                        val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                        if (chatRoomList[i].senderId == senderId && chatRoomList[i].receiverId == receiverId) {
                            chatRoomList[i].message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString()
                            chatRoomList[i].dateObject = documentChange.document.getLong(Constants.KEY_TIMESTAMP)!!
                            break
                        }
                    }
                }
            }
        }
        chatRoomList.sortBy { it.dateObject }
        conversationAdapter.notifyDataSetChanged()
        binding.chattingRecyclerview.smoothScrollToPosition(0)
    }

}