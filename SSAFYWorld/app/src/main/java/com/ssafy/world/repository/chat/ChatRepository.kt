package com.ssafy.world.repository.chat

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.ktx.Firebase
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.utils.Constants
import com.ssafy.world.utils.getReadableDateTime
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

    suspend fun observeChatRooms(): Flow<List<ChatMessage>> = withContext(Dispatchers.IO) {
        return@withContext callbackFlow {
            val chatRoomsRef = db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, "100")
                .whereEqualTo(Constants.KEY_RECEIVER_ID, "200")
            val listener = chatRoomsRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    cancel(exception.message!!, exception)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val chatRooms = mutableListOf<ChatMessage>()
                    for (document in it.documents) {
                        val chatRoom = document.toObject(ChatMessage::class.java)
                        chatRoom?.let { room -> chatRooms.add(room) }
                    }
                    trySend(chatRooms).isSuccess
                }
            }
            awaitClose {
                listener.remove()
            }
        }
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
    suspend fun addConversion(conversion: HashMap<String, Any>) {
        suspendCoroutine { continuation ->
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

    suspend fun observeChatMessages(): Flow<List<ChatMessage>> = withContext(Dispatchers.IO) {
        return@withContext callbackFlow {
            val chatRoomsRef = db.collection(Constants.KEY_COLLECTION_CHAT)
            val listener = chatRoomsRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    cancel(exception.message!!, exception)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val chatRooms = mutableListOf<ChatMessage>()
                    for (document in it.documents) {
                        Log.d("싸피", "observeChatMessages: ${document.data}")
                        val chatRoom = document.toObject(ChatMessage::class.java)
                        chatRoom?.let { room ->
                            chatRooms.add(room)
                        }
                    }
                    trySend(chatRooms).isSuccess
                }
            }
            awaitClose {
                listener.remove()
            }
        }
    }
}