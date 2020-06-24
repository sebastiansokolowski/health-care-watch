package com.sebastiansokolowski.healthguard.receiver

import android.content.Context
import android.content.Intent
import com.sebastiansokolowski.healthguard.model.MeasurementModel
import dagger.android.DaggerBroadcastReceiver
import timber.log.Timber
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 04.06.19.
 */
class BatteryLowLevelReceiver : DaggerBroadcastReceiver() {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var measurementModel: MeasurementModel

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("onReceive action=${intent?.action}")
        super.onReceive(context, intent)

        context?.let {
            measurementModel.stopMeasurement()
        }
    }
}