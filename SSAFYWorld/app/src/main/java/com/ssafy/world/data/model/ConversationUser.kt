package com.ssafy.world.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConversationUser(
	var name: String,
	var image: String,
	var token: String? = null,
	var id: String
): Parcelable
