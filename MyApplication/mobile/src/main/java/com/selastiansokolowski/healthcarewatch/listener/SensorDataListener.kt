package com.selastiansokolowski.healthcarewatch.listener

import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.selastiansokolowski.healthcarewatch.dataModel.SensorEventAccuracy
import com.selastiansokolowski.healthcarewatch.dataModel.SensorEventData
import com.selastiansokolowski.healthcarewatch.dataModel.SensorEventSupportedInfo
import com.selastiansokolowski.shared.DataClientPaths

/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
class SensorDataListener : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    override fun onDataChanged(dataEvent: DataEventBuffer) {
        dataEvent.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "type not changed")
                return
            }

            when (event.dataItem.uri.path) {
                DataClientPaths.DATA_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val values = getFloatArray(DataClientPaths.DATA_MAP_SENSOR_EVENT_VALUES_KEY)
                        val sensorName = getString(DataClientPaths.DATA_MAP_SENSOR_EVENT_SENSOR_KEY)
                        val accuracy = getInt(DataClientPaths.DATA_MAP_SENSOR_EVENT_ACCURACY_KEY)
                        val timestamp = getLong(DataClientPaths.DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY)

                        val sensorEvent = SensorEventData(sensorName, accuracy, timestamp, values)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
                DataClientPaths.ACCURACY_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val type = getInt(DataClientPaths.ACCURACY_MAP_SENSOR_TYPE)
                        val accuracy = getInt(DataClientPaths.ACCURACY_MAP_SENSOR_ACCURACY)

                        val sensorEvent = SensorEventAccuracy(type, accuracy)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
                DataClientPaths.SUPPORTED_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val type = getInt(DataClientPaths.SUPPORTED_MAP_SENSOR_TYPE)
                        val supported = getBoolean(DataClientPaths.SUPPORTED_MAP_SENSOR_SUPPORTED)

                        val sensorEvent = SensorEventSupportedInfo(type, supported)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
            }
        }
    }
}