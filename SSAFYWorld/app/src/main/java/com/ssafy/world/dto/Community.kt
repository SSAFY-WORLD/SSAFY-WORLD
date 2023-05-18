package com.ssafy.world.dto

data class Community(
    var id: String,
    var userId: String,
    var title: String,
    var content: String,
    var time: Long,
    var comments: ArrayList<Comment> = ArrayList(),
    var photoUrls: ArrayList<String> = ArrayList()
) {
}