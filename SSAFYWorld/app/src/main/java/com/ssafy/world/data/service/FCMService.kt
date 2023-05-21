package com.ssafy.world.data.service

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

private const val TAG = "FCMService_싸피"

object FCMService {
    private val messaging: FirebaseMessaging by lazy {
        Firebase.messaging
    }

    suspend fun getToken(): String {
        return try {
            val token = Firebase.messaging.token.await()
            Log.d(TAG, "onCreate token: $token")
            token
        } catch (e: Exception) {
            Log.w(TAG, "Fetching FCM registration token failed", e)
            ""
        }
    }
}