package com.ssafy.world.data.model

import java.io.Serializable
import java.util.*

data class ChatMessage(
	var senderId: String,
	var receiverId: String,
	var message: String,
	var dateTime: String = "",
	var dateObject: Date,
	// conversion Chat
	var conversionId: String = "",
	var conversionName: String = "",
	var conversionImage: String = "",
): Serializable