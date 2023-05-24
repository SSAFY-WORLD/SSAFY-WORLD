package com.ssafy.world.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssafy.world.data.local.dao.NotificationDao
import com.ssafy.world.data.local.entity.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1)
abstract class NotificationDatabase: RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}