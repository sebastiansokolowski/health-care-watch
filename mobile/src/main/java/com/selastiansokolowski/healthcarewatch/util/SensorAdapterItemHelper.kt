package com.selastiansokolowski.healthcarewatch.util

import android.content.Context
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.ui.sensorData.SensorAdapterItem

/**
 * Created by Sebastian Sokołowski on 20.08.19.
 */
class SensorAdapterItemHelper {

    companion object {
        fun getTitle(context: Context?, sensorAdapterItem: SensorAdapterItem): String {
            context?.let {
                return when (sensorAdapterItem) {
                    SensorAdapterItem.HEART_RATE -> context.getString(R.string.sensor_adapter_heart_rate_title)
                    SensorAdapterItem.STEP_COUNTER -> context.getString(R.string.sensor_adapter_step_counter_title)
                    SensorAdapterItem.GRAVITY -> context.getString(R.string.sensor_adapter_gravity_title)
                    SensorAdapterItem.LINEAR_ACCELERATION -> context.getString(R.string.sensor_adapter_linear_acceleration)
                }
            }
            return "null"
        }
    }
}