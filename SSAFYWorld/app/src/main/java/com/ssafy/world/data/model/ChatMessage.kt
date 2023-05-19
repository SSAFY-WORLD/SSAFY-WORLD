package com.ssafy.world.data.model

import java.io.Serializable
import java.util.*

data class ChatMessage(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    var dateObject: Long = System.currentTimeMillis(),
    var isRead: Boolean = false,
    // conversion Chat
    var conversionId: String = "",
    var conversionName: String = "",
    var conversionImage: String = "",
) : Serializable {
    override fun toString(): String {
        return "ChatMessage(senderId='$senderId', receiverId='$receiverId', message='$message', dateObject=$dateObject, isRead=$isRead)"
    }
}