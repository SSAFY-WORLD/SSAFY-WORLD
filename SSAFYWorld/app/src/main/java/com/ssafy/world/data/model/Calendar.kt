package com.ssafy.world.data.model

data class Calendar(
    var id: Long = 0,
    var title: String = "",
    var startTime: Long = 0,
    var endTime: Long = 0,
    var description: String = "",
)