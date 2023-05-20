package com.ssafy.world.src.main.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.User
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {
    private val repository = ApplicationClass.userRepository

    private val _users: MutableLiveData<ArrayList<User>> = MutableLiveData()
    val users: LiveData<ArrayList<User>> = _users

    fun getAllUsers(email: String) {
        viewModelScope.launch {
            val userList = repository.getAllUsers(email)
            _users.value = userList
        }
    }
}