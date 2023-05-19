package com.ssafy.world.data.model

data class Photo(
    var url: String,
    var isSelected: Boolean
) {
    constructor() : this("", false)
}