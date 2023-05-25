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

    private var _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    private val _userList: MutableLiveData<ArrayList<User>> = MutableLiveData()
    val userList: LiveData<ArrayList<User>> = _userList

    private val _updateUserSuccess = MutableLiveData<Boolean>()
    val updateUserSuccess: LiveData<Boolean>
        get() = _updateUserSuccess

    fun getUser(id: String) = viewModelScope.launch {
        try {
            _user.value = repository.getUser(id)
        } catch (e: Exception) {
            _user.value = User()
        }
    }
    fun getAllUsers(email: String) {
        viewModelScope.launch {
            val userList = repository.getAllUsers(email)
            _userList.value = userList
        }
    }

    fun updateUser(updateUser: User) = viewModelScope.launch {
        try {
            _updateUserSuccess.postValue(repository.updateUserDetails(updateUser))
        } catch (e: Exception) {
            _updateUserSuccess.postValue(false)
        }
    }

    fun initialize() {
        _user.value = null
    }

}