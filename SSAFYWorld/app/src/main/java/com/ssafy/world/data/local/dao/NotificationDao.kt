package com.ssafy.world.data.local.dao

import androidx.room.*
import com.ssafy.world.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY receiveTime DESC")
    fun getAllNotifications() : Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications where id = (:id)")
    suspend fun getNotification(id: Long) : NotificationEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification:  NotificationEntity)
}