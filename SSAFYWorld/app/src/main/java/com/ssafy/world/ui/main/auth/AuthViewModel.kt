package com.ssafy.world.ui.main.auth

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

    private val _isDuplicated = MutableLiveData<User>()
    val isDuplicated: LiveData<User>
        get() = _isDuplicated

    private val _tokenSuccess = MutableLiveData<Boolean>()
    val tokenSuccess: LiveData<Boolean>
        get() = _tokenSuccess

    private val _updateUserPwdSuccess = MutableLiveData<Boolean>()
    val updateUserPwdSuccess: LiveData<Boolean>
        get() = _updateUserPwdSuccess

	private val _deleteUserSuccess = MutableLiveData<Boolean>()
	val deleteUserSuccess: LiveData<Boolean>
		get() = _deleteUserSuccess

    private val _validationSuccess = MutableLiveData<String>()
    val validationSuccess: LiveData<String>
        get() = _validationSuccess

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
            _isDuplicated.value = repository.isEmailDuplicate(email) ?: User()
            Log.d(TAG, "isEmailDuplicate: ${_isDuplicated.value}")
        } catch (e: Exception) {
            User()
        }

    }

    fun login(email: String, password: String) = viewModelScope.launch {
        try {
            val getUser: User = repository.login(email, password)
            _user.postValue(getUser)
        } catch (e: Exception) {

        }
    }

    fun deleteUser(userId: String) = viewModelScope.launch {
	    try {
            _deleteUserSuccess.postValue(repository.deleteUser(userId))
	    } catch (e: Exception) {
			_deleteUserSuccess.postValue(false)
		}
    }

    fun updateUserPwd(userId: String, userPwd: String) = viewModelScope.launch {
        try {
            _updateUserPwdSuccess.postValue(repository.updateUserPwd(userId, userPwd))
        } catch (e: Exception) {
            _updateUserPwdSuccess.postValue(false)
        }
    }
    // FCM Token Update
    fun updateUserToken(userEmail: String) = viewModelScope.launch {
        try {
            Log.d(TAG, "updateToken: ${_tokenSuccess.value}")
            _tokenSuccess.postValue(repository.updateUserToken(userEmail))
        } catch (e: Exception) {
            _tokenSuccess.postValue(false)
        }
        Log.d(TAG, "updateToken: ${_tokenSuccess.value}")
    }

    fun saveToValidationCollection(validationCode: String) = viewModelScope.launch {
        if(repository.saveToValidationCollection(validationCode)) {
            _validationSuccess.postValue(validationCode)
        }
    }

    fun getValidationCode() = viewModelScope.launch {
        _validationSuccess.postValue(repository.getValidationCode())
    }
}