package com.ssafy.world.data.repository.chat

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.world.data.model.ChatMessage
import com.ssafy.world.data.model.User
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ChatRepository {

    private val db by lazy { Firebase.firestore }

    suspend fun sendMessage(message: ChatMessage, sendUser: User, receiverId: String) {
        val chat = hashMapOf<String, Any>().apply {
            this[Constants.KEY_SENDER_ID] = sendUser.email
            this[Constants.KEY_RECEIVER_ID] = receiverId
            this[Constants.KEY_MESSAGE] = message.message
            this[Constants.KEY_TIMESTAMP] = message.dateObject
            this[Constants.KEY_IS_READ] = message.isRead
        }
        db.collection(Constants.KEY_COLLECTION_CHAT).add(chat).await()
    }
}