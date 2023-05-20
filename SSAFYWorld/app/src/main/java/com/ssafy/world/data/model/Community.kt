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
    var title: String,
    var content: String,
    var time: Long,
    var comments: ArrayList<Comment> = ArrayList(),
    var photoUrls: ArrayList<String> = ArrayList()
) : Parcelable {
    constructor() : this("", "", "","","", 0)

    fun getFormattedTime(): String {
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val date = Date(time)
        return dateFormat.format(date)
    }

}