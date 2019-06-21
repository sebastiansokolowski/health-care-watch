package com.selastiansokolowski.healthcarewatch.receiver

import android.content.Context
import android.content.Intent
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import dagger.android.DaggerBroadcastReceiver
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 04.06.19.
 */
class BatteryLowLevelReceiver : DaggerBroadcastReceiver() {

    @Inject
    lateinit var sensorDataModel: SensorDataModel

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        context?.let {
            sensorDataModel.stopMeasurement()
        }
    }
}