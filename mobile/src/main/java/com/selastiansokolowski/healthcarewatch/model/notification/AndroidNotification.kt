package com.selastiansokolowski.healthcarewatch.model.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat.getSystemService
import com.selastiansokolowski.healthcarewatch.MainActivity
import com.selastiansokolowski.healthcarewatch.R

/**
 * Created by Sebastian Sokołowski on 05.06.19.
 */
class AndroidNotification(val context: Context) {

    private val CHANNEL_ID: String = context.packageName
    private var NOTIFICATION_ID: Int = 1

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            val notificationManager: NotificationManager =
                    getSystemService(context, NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(notification: Notification) {
        notificationManagerCompat.notify(NOTIFICATION_ID, notification)
        NOTIFICATION_ID++
    }

    private fun buildNotification(title: String, message: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_heart_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
    }

    fun showAlertNotification(message: String) {
        val notification = buildNotification("Alert!!", message)

        showNotification(notification)
    }

}