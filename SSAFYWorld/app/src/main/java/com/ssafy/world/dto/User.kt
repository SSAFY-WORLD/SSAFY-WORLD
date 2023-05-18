package com.ssafy.world.dto

data class User(
    val id: String,
    val email: String,
    val name: String,
    val pwd: String,
    val nickname: String,
    val profilePhoto: String,
    val token: String,
    val isKAKAO: Boolean
) {
    constructor() : this("", "", "", "", "", "", "", false)
}
