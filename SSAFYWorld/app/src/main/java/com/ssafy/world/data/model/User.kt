package com.ssafy.world.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id: String,
    var email: String,
    var name: String,
    var pwd: String,
    var nickname: String,
    var profilePhoto: String,
    var kakaoId: String,
    var token: String,
    var likeList: ArrayList<String> = arrayListOf<String>()
): Parcelable {
    constructor() : this("", "", "", "", "", "", "", "")

}
