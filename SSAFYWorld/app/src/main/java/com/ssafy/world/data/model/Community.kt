package com.ssafy.world.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class Community(
    var id: String,
    var userId: String,
    var userNickname: String,
    var userProfile: String,
    var title: String,
    var content: String,
    var time: Long,
    var commentCount: Int = 0,
    var likeCount:Int = 0,
    var collection:String = "",
    var fcmToken:String = "",
    var photoUrls: ArrayList<String> = ArrayList(),
    var likedUserIds: ArrayList<String> = ArrayList(),
    var placeName: String = "",
    var placeAddress: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var rating: Double = 0.0
) : Parcelable {
    constructor() : this("", "","", "","","", 0)

    fun getFormattedTime(): String {
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val date = Date(time)
        return dateFormat.format(date)
    }

}