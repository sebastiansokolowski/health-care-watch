package com.selastiansokolowski.healthcarewatch.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.healthcarewatch.model.notification.AndroidNotification
import dagger.android.DaggerService
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
class MeasurementService : DaggerService() {

    @Inject
    lateinit var sensorDataModel: SensorDataModel

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
        val androidNotification = AndroidNotification(this)
        val notification = androidNotification.buildNotification("Health Care Watch running", "Measurement running")
        val notificationId = androidNotification.FOREGROUND_NOTIFICATION_ID
        startForeground(notificationId, notification)
    }

    override fun onDestroy() {
        sensorDataModel.stopMeasurement()
        super.onDestroy()
    }
}