package com.selastiansokolowski.healthcarewatch.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import dagger.android.DaggerService
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
class SensorService : DaggerService() {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var sensorDataModel: SensorDataModel

    private val mBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        internal val service: SensorService
            get() = this@SensorService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        sensorDataModel.stopMeasurement()

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        sensorDataModel.stopMeasurement()
        super.onDestroy()
    }
}