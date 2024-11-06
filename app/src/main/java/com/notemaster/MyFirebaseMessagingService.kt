package com.notemaster

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "New Note", it.body ?: "A new note has been created.")
        }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "note_notifications_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Check if notifications are enabled
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Log.w("MyFirebaseMessagingService", "Notifications are disabled for this app.")
            return
        }

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notes Notifications"
            val descriptionText = "Notifications for new notes"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notemaster)  // Make sure to replace with your actual drawable
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Attempt to show the notification with exception handling
        try {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("MyFirebaseMessagingService", "Failed to send notification due to missing permissions", e)
        }
    }
}
