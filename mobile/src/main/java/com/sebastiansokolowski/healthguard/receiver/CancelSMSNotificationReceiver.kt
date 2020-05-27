package com.sebastiansokolowski.healthguard.receiver

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sebastiansokolowski.healthguard.model.NotificationModel
import dagger.android.DaggerBroadcastReceiver
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 04.06.19.
 */
class CancelSMSNotificationReceiver : DaggerBroadcastReceiver() {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var notificationModel: NotificationModel

    companion object {
        val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive action=${intent?.action}")
        super.onReceive(context, intent)

        intent?.getIntExtra(EXTRA_NOTIFICATION_ID, 0)?.let {
            Log.d(TAG, "notificationId: $it")
            notificationModel.dismissAlertNotification(it)
        }
    }
}