package com.ssafy.world.data.service

import android.content.ContentValues
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.NotificationData
import com.ssafy.world.data.model.PushNotification
import com.ssafy.world.data.remote.NotificationAPI
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "FCMService_싸피"

object FCMService {
    private val messaging: FirebaseMessaging by lazy {
        Firebase.messaging
    }

    suspend fun getToken(): String {
        return try {
            val token = messaging.token.await()
            Log.d(TAG, "onCreate token: $token")
            token
        } catch (e: Exception) {
            Log.w(TAG, "Fetching FCM registration token failed", e)
            ""
        }
    }
    fun sendRemoteNotification(data: NotificationData, token: String) = CoroutineScope(Dispatchers.IO).launch {
        val service = ApplicationClass.retrofit.create(NotificationAPI::class.java)
        try {
            val notification = PushNotification(data, token)
            val response = service.postNotification(Constants.getRemoteMsgHeaders(), notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(ContentValues.TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(ContentValues.TAG, e.toString())
        }
    }
}