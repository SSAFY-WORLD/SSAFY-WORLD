package com.ssafy.world.src.main.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.ConversationUser
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.FragmentChatBinding
import com.ssafy.world.utils.Constants
import com.ssafy.world.utils.LineDividerItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatFragment : Fragment(), ConversionListener  {

    private lateinit var binding: FragmentChatBinding
    private lateinit var conversationAdapter: RecentConversationAdapter
    private lateinit var sendUser: User
    private lateinit var chatRoomList: ArrayList<ChatMessage>
    private val db: FirebaseFirestore by lazy { Firebase.firestore }
    private lateinit var mContext: Context

    private var chatRoomListener1: ListenerRegistration? = null
    private var chatRoomListener2: ListenerRegistration? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        initCallback()
        chatRoomList = ArrayList()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendUser = ApplicationClass.sharedPreferences.getUser()!!
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListener()
    }


    private fun init() = with(binding) {
        conversationAdapter = RecentConversationAdapter(sendUser.email ,chatRoomList, this@ChatFragment)
        chattingRecyclerview.adapter = conversationAdapter
        val swipeDelete = object: SwipeToDeleteCallback(mContext) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                deleteChatRoom(position)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeDelete)
        itemTouchHelper.attachToRecyclerView(chattingRecyclerview)
        chattingRecyclerview.addItemDecoration(LineDividerItemDecoration(mContext, R.drawable.recyclerview_divider, 10, 10))
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
            val chatRoomRef1 = db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, sendUser.email)
            val chatRoomRef2 = db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, sendUser.email)

            chatRoomListener1 = chatRoomRef1
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(eventListener)
            chatRoomListener2 = chatRoomRef2
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(eventListener)
        }
    }

    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        val addedChatRooms = ArrayList<ChatMessage>()
        for (documentChange in value?.documentChanges.orEmpty()) {
            if (documentChange.type == DocumentChange.Type.ADDED) {
                val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                val message = documentChange.document.toObject(ChatMessage::class.java)
                val lastMessage = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                if (sendUser.email == senderId) {
                    message.conversionId = receiverId!!
                    message.conversionName = documentChange.document.getString(Constants.KEY_RECEIVER_NAME).toString()
                    message.message = lastMessage!!
                    message.conversionImage = documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE).toString()
                } else {
                    message.senderId = senderId!!
                    message.receiverId = receiverId!!
                    message.conversionId = senderId
                    message.conversionName = documentChange.document.getString(Constants.KEY_SENDER_NAME).toString()
                    message.conversionImage = documentChange.document.getString(Constants.KEY_SENDER_IMAGE).toString()
                    message.message = lastMessage!!
                }
                addedChatRooms.add(message)
            } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                for (chatRoom in chatRoomList) {
                    if (chatRoom.senderId == documentChange.document.getString(Constants.KEY_SENDER_ID)
                        && chatRoom.receiverId == documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                    ) {
                        chatRoom.message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString()
                        chatRoom.dateObject = documentChange.document.getLong(Constants.KEY_TIMESTAMP)!!
                        break
                    }
                }
            }
        }
        chatRoomList.addAll(addedChatRooms)
        CoroutineScope(Dispatchers.Main).launch {
            conversationAdapter.notifyDataSetChanged()
            binding.chattingRecyclerview.smoothScrollToPosition(0)
        }
    }

    private fun deleteChatRoom(position: Int) {
        val chatRoom = chatRoomList[position]
        val conversationRef = db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
        val query = conversationRef
            .whereEqualTo(Constants.KEY_SENDER_ID, chatRoom.senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, chatRoom.receiverId)

        query.get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    conversationRef.document(document.id).delete()
                        .addOnSuccessListener {
                            // 채팅방 삭제 성공 처리 후 해당 채팅들도 삭제
                            deleteChattings(chatRoom.senderId, chatRoom.receiverId)
                            deleteChattings(chatRoom.receiverId, chatRoom.senderId)
                            chatRoomList.removeAt(position)
                            conversationAdapter.notifyItemRemoved(position)
                        }
                }
            }
    }

    private fun deleteChattings(senderId: String, receiverId: String) {
        // 채팅방 삭제 성공 처리 후 해당 채팅들도 삭제
        val messagesRef = db.collection(Constants.KEY_COLLECTION_CHAT)
        val chatQuery = messagesRef
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
        chatQuery.get()
            .addOnSuccessListener { chatSnapshot ->
                for (chatDocument in chatSnapshot.documents) {
                    messagesRef.document(chatDocument.id).delete()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatRoomListener1?.remove()
        chatRoomListener2?.remove()
    }
}