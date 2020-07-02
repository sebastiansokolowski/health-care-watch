package com.sebastiansokolowski.healthguard.util

import android.content.Context
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.ui.sensorData.SensorAdapterItem

/**
 * Created by Sebastian Sokołowski on 20.08.19.
 */
class SensorAdapterItemHelper {

    companion object {
        fun getTitle(context: Context?, sensorAdapterItem: SensorAdapterItem): String {
            context?.let {
                return when (sensorAdapterItem) {
                    SensorAdapterItem.HEART_RATE -> context.getString(R.string.sensor_adapter_heart_rate_title)
                    SensorAdapterItem.ACCELERATION_VECTOR -> context.getString(R.string.sensor_adapter_acceleration_vector)
                }
            }
            return "null"
        }

        fun getUnit(context: Context?, sensorAdapterItem: SensorAdapterItem): String {
            context?.let {
                return when (sensorAdapterItem) {
                    SensorAdapterItem.HEART_RATE -> context.getString(R.string.unit_heart_rate)
                    SensorAdapterItem.ACCELERATION_VECTOR -> context.getString(R.string.unit_linear_acceleration)
                }
            }
            return "null"
        }
    }
}