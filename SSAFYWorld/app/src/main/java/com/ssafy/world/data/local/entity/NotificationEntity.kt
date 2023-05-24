package com.ssafy.world.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ssafy.world.utils.Constants

@Entity(tableName = "notifications")
data class NotificationEntity (
    @PrimaryKey
    var id: String,
    val destination: String,
    val title: String,
    val message: String,
    val receiveTime: Long
)