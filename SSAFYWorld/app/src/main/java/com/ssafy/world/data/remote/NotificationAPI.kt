package com.ssafy.world.data.remote
import com.ssafy.world.data.model.PushNotification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface NotificationAPI {
    @POST("send")
    suspend fun postNotification(
        @HeaderMap headers: HashMap<String, String>,
        @Body messageBody: PushNotification
    ): Response<String>
}
