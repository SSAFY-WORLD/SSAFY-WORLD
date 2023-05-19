package com.ssafy.world.utils

object Constants {
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
            remoteMsgHeaders!![REMOTE_MSG_AUTHORIZATION] = "key=AAAAFRz4Kco:APA91bF5XogB05ovv5nylNctYlv7hp6cGo9yMWDcV4zTNYTBw-t3-MS9f9B2o3pvgFmqNRyjV47fi7V-6-7J-q50cWo6rsjQ4eZoqq71PbKYcIDiIdibr30QsiJCAysmtfkqUzIrE7YB"
            remoteMsgHeaders!![REMOTE_MSG_CONTENT_TYPE] = "application/json"
        }
        return remoteMsgHeaders!!
    }
}