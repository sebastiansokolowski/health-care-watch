package com.sebastiansokolowski.healthcarewatch.receiver

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sebastiansokolowski.healthcarewatch.model.SensorDataModel
import dagger.android.DaggerBroadcastReceiver
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 04.06.19.
 */
class BatteryLowLevelReceiver : DaggerBroadcastReceiver() {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var sensorDataModel: SensorDataModel

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        super.onReceive(context, intent)

        context?.let {
            sensorDataModel.stopMeasurement()
        }
    }
}