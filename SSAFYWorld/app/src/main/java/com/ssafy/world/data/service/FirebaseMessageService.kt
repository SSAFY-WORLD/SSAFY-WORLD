package com.ssafy.world.data.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.world.R
import com.ssafy.world.src.main.MainActivity
import com.ssafy.world.utils.Constants
import com.ssafy.world.utils.Constants.SUMMARY_ID

private const val TAG = "FirebaseMessageService_싸피"
class FirebaseMessageService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
    }

    // Foreground, Background 모두 처리하기 위해서는 data에 값을 담아서 넘긴다.
    //https://firebase.google.com/docs/cloud-messaging/android/receive
    @SuppressLint("MissingPermission")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var messageDestination = ""
        var messageTitle = ""
        var messageContent = ""

        // background 에 있을경우 혹은 foreground에 있을경우 두 경우 모두
        val data = remoteMessage.data
        Log.d(TAG, "data.message: ${data[Constants.DESTINATION]}")
        Log.d(TAG, "data.message: ${data[Constants.TITLE]}")
        Log.d(TAG, "data.message: ${data[Constants.MESSAGE]}")
        Log.d(TAG, "data.message: ${data[Constants.IMAGE]}")
        messageDestination = data[Constants.DESTINATION].toString()
        messageTitle = data[Constants.TITLE].toString()
        messageContent = data[Constants.MESSAGE].toString()
        // 받아온 destination 정보를 바탕으로 알람이 어떤 Fragment로 갈지 지정
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.DESTINATION, messageDestination)
        }

        val mainPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 101, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(messageTitle)
            .setContentText(messageContent)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)

        val summaryNotification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(messageTitle)
            .setContentText(messageContent)
            .setGroup(messageDestination)
            .setGroupSummary(true)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).apply {
            notify(101, notification.build())
            notify(SUMMARY_ID, summaryNotification.build())
        }
    }
}