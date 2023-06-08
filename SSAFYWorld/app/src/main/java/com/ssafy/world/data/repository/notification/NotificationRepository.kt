package com.ssafy.world.data.repository.notification

import androidx.room.withTransaction
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

object NotificationRepository {
    private val database by lazy {
        ApplicationClass.database
    }
    private val dao by lazy {
        database.notificationDao()
    }

    // 전체 알림 목록 가져오기
    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return dao.getAllNotifications()
    }

    // 알림 넣기
    suspend fun insertNotification(notification: NotificationEntity) = database.withTransaction {
        dao.insertNotification(notification)
    }

    // 알림 전체 삭제하기
    suspend fun deleteNotification(notification: NotificationEntity) = database.withTransaction {
        dao.deleteNotification(notification)
    }



}