package com.ssafy.world.utils

object Constants {
    // FCM Token
    const val KEY_TOKEN = "token"
    // Notification Channel ID
    const val CHANNEL_ID = "ssafy_world_channel"
    const val CHANNEL_NAME = "싸피월드"
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
    // Notification KEY
    var SUMMARY_ID = 100
    const val DESTINATION = "destination"
    // name으로 Notification구분 TODO
    const val CHAT = "채팅"
    const val COMMUNITY = "커뮤니티"
    const val TITLE = "title"
    const val MESSAGE = "message"
    const val IMAGE = "image"
    // Remote Message
    private const val REMOTE_MSG_AUTHORIZATION = "Authorization"
    private const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
    // Header
    var remoteMsgHeaders: HashMap<String, String>? = null
    @JvmName("getRemoteMsgHeaders1")
    fun getRemoteMsgHeaders(): HashMap<String, String> {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = HashMap()
            remoteMsgHeaders!![REMOTE_MSG_AUTHORIZATION] = "key=AAAA_DJ7fP0:APA91bGOei_kKfan2pmm5NEpox3MHM7mm7LU6UCOXdcV6YnD4igOzV9Z1wLifQrlsdJ7GyPKb66bceWtu3papcxwGEm--vfLRzKwWoKkUIpb8YPtuPNDJ80589CwVXKm2zP-AKJ16Fwj"
            remoteMsgHeaders!![REMOTE_MSG_CONTENT_TYPE] = "application/json"
        }
        return remoteMsgHeaders!!
    }
}