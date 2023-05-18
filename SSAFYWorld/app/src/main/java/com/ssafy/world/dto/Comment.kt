package com.ssafy.world.dto

data class Comment(
    val id: String,
    val userId: String,
    val comment: String,
    val time: Long,
) {

}