package com.sebastiansokolowski.healthguard.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.model.NotificationModel
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import dagger.android.DaggerService
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
class MeasurementService : DaggerService() {

    @Inject
    lateinit var sensorDataModel: SensorDataModel

    @Inject
    lateinit var notificationModel: NotificationModel

    private val mBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        internal val service: MeasurementService
            get() = this@MeasurementService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val androidNotification = notificationModel.androidNotificationModel
        val builder = androidNotification.createMeasurementNotificationBuilder(getString(R.string.notification_measurement_title), getString(R.string.notification_measurement_message))
        val notificationId = androidNotification.FOREGROUND_NOTIFICATION_ID
        startForeground(notificationId, builder.build())
    }

    override fun onDestroy() {
        sensorDataModel.stopMeasurement()
        super.onDestroy()
    }
}