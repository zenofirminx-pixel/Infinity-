package com.infinity.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CHAT_CHANNEL_ID = "infinity_chat"
    fun create(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHAT_CHANNEL_ID, "Infinity", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notifications de fin de réponse Infinity"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
