package com.ssafy.world.config

import android.util.Log
import com.ssafy.world.config.ApplicationClass.Companion.sharedPreferences
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

private const val TAG = "AddCoIncep_싸피"
class AddCookiesInterceptor : Interceptor{

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()

        // cookie 가져오기
        val getCookies = sharedPreferences.getUserCookie()
        for (cookie in getCookies!!) {
            builder.addHeader("Cookie", cookie)
            Log.d(TAG,"Adding Header: $cookie")
        }
        return chain.proceed(builder.build())
    }
}