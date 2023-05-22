package com.ssafy.world.data.model

import java.io.Serializable
import java.util.*

data class ChatMessage(
    var senderId: String = "",
    var senderImage: String = "",
    var receiverId: String = "",
    var message: String = "",
    var dateObject: Long = System.currentTimeMillis(),
    var isRead: Boolean = false,
    // conversion Chat
    var conversionId: String = "",
    var conversionName: String = "",
    var conversionImage: String = "",
    var senderUnRead: Long = 0,
    var receiverUnRead: Long = 0
) : Serializable {
    override fun toString(): String {
        return "ChatMessage(senderId='$senderId', receiverId='$receiverId', message='$message', dateObject=$dateObject, isRead=$isRead)"
    }
}