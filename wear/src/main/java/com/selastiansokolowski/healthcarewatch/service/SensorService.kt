package com.selastiansokolowski.healthcarewatch.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import dagger.android.DaggerService
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
class SensorService : DaggerService() {

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

}