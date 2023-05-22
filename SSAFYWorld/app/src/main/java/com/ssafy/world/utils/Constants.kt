package com.ssafy.world.utils

object Constants {
    // FCM Token
    const val KEY_TOKEN = "token"
    // Notification Channel ID
    const val CHANNEL_ID = "ssafy_world_channel"
    const val CHANNEL_NAME = "ssafy"
    // 채팅을 위한 KEY
    const val KEY_COLLECTION_CHAT = "chat"
    const val KEY_SENDER_ID = "senderId"
    const val KEY_RECEIVER_ID = "receiverId"
    const val KEY_MESSAGE = "message"
    const val KEY_TIMESTAMP = "timestamp"
    const val KEY_IS_READ = "isRead"
    // Conversations 위한 Key
    const val KEY_COLLECTION_CONVERSATIONS = "conversations"
    const val KEY_SENDER_NAME = "senderName"
    const val KEY_SENDER_IMAGE = "senderImage"
    const val KEY_RECEIVER_NAME = "receiverName"
    const val KEY_RECEIVER_IMAGE = "receiverImage"
    const val KEY_LAST_MESSAGE = "lastMessage"
    const val KEY_SENDER_UNREAD = "senderUnRead"
    const val KEY_RECEIVER_UNREAD = "receiverUnRead"

    // BASE URL
    const val BASE_URL = "https://fcm.googleapis.com/fcm/"

    // Remote Message
    private const val REMOTE_MSG_AUTHORIZATION = "Authorization"
    private const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
    const val REMOTE_MSG_DATA = "data"
    const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"
    // Header
    var remoteMsgHeaders: HashMap<String, String>? = null
    @JvmName("getRemoteMsgHeaders1")
    fun getRemoteMsgHeaders(): HashMap<String, String> {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = HashMap()
            remoteMsgHeaders!![REMOTE_MSG_AUTHORIZATION] = "key=98bf28c1ca96b7215eed4ffd37e657167fc2e3d8"
            remoteMsgHeaders!![REMOTE_MSG_CONTENT_TYPE] = "application/json"
        }
        return remoteMsgHeaders!!
    }
}