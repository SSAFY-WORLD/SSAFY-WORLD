package com.ssafy.world.data.model

data class User(
    var id: String,
    var email: String,
    var name: String,
    var pwd: String,
    var nickname: String,
    var profilePhoto: String,
    var kakaoId: String,
    var token: String,
) {
    constructor() : this("", "", "", "", "", "", "", "")

}
