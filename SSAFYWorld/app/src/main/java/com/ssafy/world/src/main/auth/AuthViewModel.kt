package com.ssafy.world.src.main.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.User
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"
class AuthViewModel : ViewModel() {
    private val repository = ApplicationClass.userRepository

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _isDuplicated = MutableLiveData<Boolean>()
    val isDuplicated: LiveData<Boolean>
        get() = _isDuplicated

    fun insertUser(user: User)  = viewModelScope.launch {
        try {
            _user.value = repository.insertUser(user)
        } catch (e: Exception) {
            _user.value = user
        }
    }
    fun getUser(id: String) = viewModelScope.launch {
        try {
            _user.value = repository.getUser(id)
        } catch (e: Exception) {
            _user.value = User()
        }
    }

    fun isEmailDuplicate(email: String) = viewModelScope.launch {
        try {
            _isDuplicated.value = repository.isEmailDuplicate(email)
        } catch (e: Exception) {
            _isDuplicated.value = false
        }
        Log.d(TAG, "isEmailDuplicate: ${isDuplicated.value}")
    }

}