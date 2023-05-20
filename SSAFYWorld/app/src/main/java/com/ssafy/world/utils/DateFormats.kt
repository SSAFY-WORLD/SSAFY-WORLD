package com.ssafy.world.utils

import java.text.SimpleDateFormat
import java.util.*

fun getReadableDateTime(date: Long): String {
    return SimpleDateFormat("a hh:mm", Locale.KOREA).format(date)
}

fun getFormattedTime(time: Long): String {
    val currentTime = System.currentTimeMillis()
    val diffMillis = currentTime - time

    val seconds = diffMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> {
            // 오늘이 아닌 경우
            val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
            val date = Date(time)
            dateFormat.format(date)
        }
        hours > 0 -> {
            // 오늘 몇 시간 전인 경우
            "$hours 시간 전"
        }
        minutes > 0 -> {
            // 오늘 몇 분 전인 경우
            "$minutes 분 전"
        }
        else -> {
            // 방금 전인 경우
            "방금 전"
        }
    }
}