package com.ssafy.world.ui.main.notification

import androidx.lifecycle.*
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.local.entity.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationViewModel : ViewModel() {
    private val repository by lazy {
        ApplicationClass.notificationRepository
    }

    init {
        fetchNotifications()
    }

    private val _notifications: MutableLiveData<List<NotificationEntity>> = MutableLiveData()
    val notifications: LiveData<List<NotificationEntity>>
        get() = _notifications

    private fun fetchNotifications() = viewModelScope.launch {
        repository.getAllNotifications()
            .flowOn(Dispatchers.IO)
            .collect { notificationsList ->
                withContext(Dispatchers.Main) {
                    _notifications.value = notificationsList
                }
            }
    }

    fun deleteNotification(notification: NotificationEntity) = viewModelScope.launch {
        repository.deleteNotification(notification)
    }

}