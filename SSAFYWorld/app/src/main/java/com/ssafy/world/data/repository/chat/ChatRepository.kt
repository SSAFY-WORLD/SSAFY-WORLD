package com.ssafy.world.data.repository.chat

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ChatRepository {

    private val db by lazy { Firebase.firestore }

    fun listenConversations(): Flow<List<ChatMessage>> = callbackFlow {
        val conversations = mutableListOf<ChatMessage>()
        val listener = db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, "100")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                value?.let { snapshot ->
                    val conversations = handleSnapshotChanges(conversations, snapshot.documents)
                    trySend(conversations).isSuccess
                }
            }

        val secondListener = db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, "100")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                value?.let { snapshot ->
                    val conversations = handleSnapshotChanges(conversations, snapshot.documents)
                    trySend(conversations).isSuccess
                }
            }

        awaitClose {
            listener.remove()
            secondListener.remove()
        }
    }

    suspend fun checkForConversationRemotely(senderId: String, receiverId: String): String? {
        val snapshot = db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .await()

        if (!snapshot.isEmpty && snapshot.documents.isNotEmpty()) {
            val documentSnapshot = snapshot.documents[0]
            return documentSnapshot.id
        }

        return null
    }

    private fun handleSnapshotChanges(conversations: MutableList<ChatMessage>, documents: List<DocumentSnapshot>): List<ChatMessage> {
        for (document in documents) {
            if (document.exists()) {
                val senderId = document.getString(Constants.KEY_SENDER_ID)
                val receiverId = document.getString(Constants.KEY_RECEIVER_ID)
                val lastMessage = document.getString(Constants.KEY_LAST_MESSAGE)
                val timestamp = document.getLong(Constants.KEY_TIMESTAMP)
                val conversionId = if ("100" == senderId) {
                    document.getString(Constants.KEY_RECEIVER_ID)
                } else {
                    document.getString(Constants.KEY_SENDER_ID)
                }
                val conversionName = if ("100" == senderId) {
                    document.getString(Constants.KEY_RECEIVER_NAME)
                } else {
                    document.getString(Constants.KEY_SENDER_NAME)
                }
                val conversionImage = if ("100" == senderId) {
                    document.getString(Constants.KEY_RECEIVER_IMAGE)
                } else {
                    document.getString(Constants.KEY_SENDER_IMAGE)
                }
                val conversation = ChatMessage(
                    senderId = senderId!!,
                    receiverId = receiverId!!,
                    message = lastMessage!!,
                    dateObject = System.currentTimeMillis(),
                    conversionId = conversionId ?: "",
                    conversionName = conversionName ?: "",
                    conversionImage = conversionImage ?: ""
                )
                conversations.add(conversation)
            }
        }
        conversations.sortByDescending { it.dateObject }
        return conversations
    }


    suspend fun updateConversion(conversionId: String, message: String) = withContext(Dispatchers.IO) {
        val documentReference =
            conversionId.run { db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(this) }
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message,
            Constants.KEY_TIMESTAMP, System.currentTimeMillis()
        ).await()
    }

    // 최근에 채팅을 한 유저가 아니면 conversion을 생성 후 id값을 반환
    suspend fun addConversion(conversion: HashMap<String, Any>): String? {
        return suspendCoroutine { continuation ->
            db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener { documentReference ->
                    continuation.resume(documentReference.id)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    suspend fun sendMessage(message: ChatMessage) {
        val chat = hashMapOf<String, Any>().apply {
            this[Constants.KEY_SENDER_ID] = "100"
            this[Constants.KEY_RECEIVER_ID] = "200"
            this[Constants.KEY_MESSAGE] = message.message
            this[Constants.KEY_TIMESTAMP] = message.dateObject
            this[Constants.KEY_IS_READ] = message.isRead
        }
        db.collection(Constants.KEY_COLLECTION_CHAT).add(chat).await()
    }

    suspend fun observeChatMessages(receiverId: String): Flow<List<ChatMessage>> = withContext(Dispatchers.IO) {
        return@withContext callbackFlow {
            // 유저 아이디로 나중에 바꾸기
            val chatRoomsRef1 = db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,"100")
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            // 유저 아이디로 나중에 바꾸기
//            val chatRoomsRef2 = db.collection(Constants.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Constants.KEY_SENDER_ID,receiverId)
//                .whereEqualTo(Constants.KEY_RECEIVER_ID, "100")
            val listener1 = chatRoomsRef1.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    cancel(exception.message!!, exception)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val chatMessages = mutableListOf<ChatMessage>()
                    for (document in it.documents) {
                        Log.d("싸피", "observeChatMessages: ${document.data}")
                        val chatRoom = document.toObject(ChatMessage::class.java)
                        chatRoom?.let { room ->
                            chatMessages.add(room)
                        }
                    }
                    chatMessages.sortBy { it.dateObject }
                    trySend(chatMessages).isSuccess
                }
            }

//            val listener2 = chatRoomsRef2.addSnapshotListener { snapshot, exception ->
//                if (exception != null) {
//                    cancel(exception.message!!, exception)
//                    return@addSnapshotListener
//                }
//
//                snapshot?.let {
//                    val chatRooms = mutableListOf<ChatMessage>()
//                    for (document in it.documents) {
//                        Log.d("싸피", "observeChatMessages: ${document.data}")
//                        val chatRoom = document.toObject(ChatMessage::class.java)
//                        chatRoom?.let { room ->
//                            chatMessages.add(room)
//                        }
//                    }
//                    chatMessages.sortBy { it.dateObject }
//                    trySend(chatRooms).isSuccess
//                }
//            }
            awaitClose {
                listener1.remove()
//                listener2.remove()
            }
        }
    }
}