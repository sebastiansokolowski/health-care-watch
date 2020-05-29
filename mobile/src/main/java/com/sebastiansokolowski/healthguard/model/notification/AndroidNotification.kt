package com.sebastiansokolowski.healthguard.model.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat.getSystemService
import com.sebastiansokolowski.healthguard.MainActivity
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.receiver.CancelSMSNotificationReceiver
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 05.06.19.
 */
class AndroidNotification(val context: Context) {

    val CHANNEL_ID: String = context.packageName
    var NOTIFICATION_ID: Int = 10
    var FOREGROUND_NOTIFICATION_ID: Int = 5

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    private val alertNotificationMap = HashMap<Int, Disposable>()

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

    fun createMeasurementNotificationBuilder(title: String, message: String): NotificationCompat.Builder {
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
    }

    fun createAlertNotificationBuilder(title: String, message: String, notificationId: Int, cancelSMSNotificationAction: Boolean = false, remainingTimeToCancel: Int = 0): NotificationCompat.Builder {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val clickPendingIntent: PendingIntent = PendingIntent.getActivity(context, notificationId, clickIntent, 0)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_heart_black_24dp)
            setContentTitle(title)
            setContentText(message)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setContentIntent(clickPendingIntent)
            setAutoCancel(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        if (cancelSMSNotificationAction) {
            val cancelSMSNotificationIntent = Intent(context, CancelSMSNotificationReceiver::class.java).apply {
                putExtra(CancelSMSNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val cancelSMSNotificationPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, notificationId, cancelSMSNotificationIntent, 0)

            builder.apply {
                setDeleteIntent(cancelSMSNotificationPendingIntent)
                val cancelActionTitle = context.getString(R.string.cancel_sending_sms_notification)
                addAction(R.drawable.ic_cancel_black_24dp, "$cancelActionTitle (${remainingTimeToCancel}s)", cancelSMSNotificationPendingIntent)
            }
        }

        return builder
    }

    fun showAlertNotification(message: String, showSMSCancelActionButton: Boolean = false, smsNotificationTimeout: Int = 0, sendSMS: (Boolean) -> Unit = {}) {
        val notificationId = NOTIFICATION_ID++
        val title = context.getString(R.string.notification_alert_title)

        if (showSMSCancelActionButton) {
            val disposable = Observable
                    .intervalRange(1, smsNotificationTimeout.toLong(), 0, 1, TimeUnit.SECONDS)
                    .observeOn(Schedulers.io())
                    .doOnDispose {
                        sendSMS(false)
                    }
                    .doOnComplete {
                        sendSMS(true)
                        val builder = createAlertNotificationBuilder(title, message, notificationId)
                        notificationManagerCompat.notify(notificationId, builder.build())
                    }
                    .subscribe {
                        val builder = createAlertNotificationBuilder(title, message, notificationId, showSMSCancelActionButton, (smsNotificationTimeout - it).toInt())
                        notificationManagerCompat.notify(notificationId, builder.build())
                    }
            alertNotificationMap.put(notificationId, disposable)
        } else {
            val builder = createAlertNotificationBuilder(title, message, notificationId)
            notificationManagerCompat.notify(notificationId, builder.build())
        }
    }

    fun dismissAlertNotification(notificationId: Int) {
        val disposable = alertNotificationMap.remove(notificationId)
        disposable?.let {
            disposable.dispose()
        }
        notificationManagerCompat.cancel(notificationId)
    }

}