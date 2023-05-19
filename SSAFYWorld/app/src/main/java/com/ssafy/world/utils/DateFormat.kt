package com.ssafy.world.utils

import java.text.SimpleDateFormat
import java.util.*

fun getReadableDateTime(date: Long): String {
    return SimpleDateFormat("a hh:mm", Locale.KOREA).format(date)
}