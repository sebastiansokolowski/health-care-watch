package com.sebastiansokolowski.healthguard.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.sebastiansokolowski.healthguard.model.MeasurementModel
import dagger.android.DaggerService
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
class SensorService : DaggerService() {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var measurementModel: MeasurementModel

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
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
        measurementModel.stopMeasurement()

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        measurementModel.stopMeasurement()
        super.onDestroy()
    }
}