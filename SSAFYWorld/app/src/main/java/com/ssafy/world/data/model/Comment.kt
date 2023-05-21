package com.ssafy.world.data.model

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds.Nickname
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    var id: String,
    var commentId: String,
    var communityId: String,
    var userId: String,
    var userNickname: String,
    var userProfile: String,
    var comment: String,
    var time: Long,
    var fcmToken: String,
    var replies: ArrayList<Comment> = ArrayList()
) : Parcelable {

    constructor() : this("", "", "", "", "", "", "", 0, "")

}