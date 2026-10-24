package com.infinity.app.util

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.infinity.app.R

class InfinityNotificationManager(private val context: Context) {
    init { NotificationChannels.create(context) }
    fun hasPermission(): Boolean = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    fun notifyReplyReady() {
        if (!hasPermission()) return
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHAT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_infinity)
            .setContentTitle("Infinity")
            .setContentText("Ta réponse est prête.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(1001, notification)
    }
}
