package com.ssafy.world.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.User

class SharedPreferencesUtil(context: Context) {
    companion object {
        private const val KEY_USER = "user"
        private const val KEY_VALIDATION = "validation"
    }

    private var preferences: SharedPreferences =
        context.getSharedPreferences(ApplicationClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson: Gson = Gson()
    fun addUserCookie(cookies: HashSet<String>) {
        val editor = preferences.edit()
        editor.putStringSet(ApplicationClass.COOKIES_KEY_NAME, cookies)
        editor.apply()
    }

    fun setUserToken(token: String) {
        preferences.edit().putString("token", token).apply()
    }

    fun getUserCookie(): MutableSet<String>? {
        return preferences.getStringSet(ApplicationClass.COOKIES_KEY_NAME, HashSet())
    }

    fun getString(key: String): String? {
        return preferences.getString(key, null)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        preferences.edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = preferences.getString(KEY_USER, null)
        return gson.fromJson(userJson, User::class.java)
    }

    fun clearUser() {
        preferences.edit().remove(KEY_USER).apply()
    }

    fun saveValidation(code: String) {
        preferences.edit().putString(KEY_VALIDATION, code).apply()
    }

    fun getValidation() : String? {
        return preferences.getString(KEY_VALIDATION, null)
    }
}