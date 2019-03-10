package com.selastiansokolowski.healthcarewatch.client

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.selastiansokolowski.healthcarewatch.BuildConfig
import com.selastiansokolowski.shared.DataClientPaths.Companion.ACCURACY_MAP_PATH
import com.selastiansokolowski.shared.DataClientPaths.Companion.ACCURACY_MAP_SENSOR_ACCURACY
import com.selastiansokolowski.shared.DataClientPaths.Companion.ACCURACY_MAP_SENSOR_TYPE
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_PATH
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_ACCURACY_KEY
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_SENSOR_TYPE
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_VALUES_KEY
import com.selastiansokolowski.shared.DataClientPaths.Companion.SUPPORTED_MAP_PATH
import com.selastiansokolowski.shared.DataClientPaths.Companion.SUPPORTED_MAP_SENSOR_SUPPORTED
import com.selastiansokolowski.shared.DataClientPaths.Companion.SUPPORTED_MAP_SENSOR_TYPE


/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class WearableDataClient(context: Context) {
    private val TAG = javaClass.canonicalName

    private val dataClient: DataClient = Wearable.getDataClient(context)

    fun sendSensorEvent(event: SensorEvent) {
        Log.d(TAG, "sendSensorEvent event=${event.sensor.name}")

        val putDataMapReq = PutDataMapRequest.create(DATA_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putFloatArray(DATA_MAP_SENSOR_EVENT_VALUES_KEY, event.values)
            putInt(DATA_MAP_SENSOR_EVENT_SENSOR_TYPE, event.sensor.type)
            putInt(DATA_MAP_SENSOR_EVENT_ACCURACY_KEY, event.accuracy)
            putLong(DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY, event.timestamp)
        }

        send(putDataMapReq)
    }

    fun sendSensorAccuracy(sensor: Sensor, accuracy: Int) {
        Log.d(TAG, "sendSensorAccuracy sensor=$sensor accuracy=$accuracy")

        val putDataMapReq = PutDataMapRequest.create(ACCURACY_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putInt(ACCURACY_MAP_SENSOR_TYPE, sensor.type)
            putInt(ACCURACY_MAP_SENSOR_ACCURACY, accuracy)
        }

        send(putDataMapReq)
    }

    fun sendSensorSupportedInfo(type: Int, supported: Boolean) {
        Log.d(TAG, "sendSensorSupportedInfo type=$type supported=$supported")

        val putDataMapReq = PutDataMapRequest.create(SUPPORTED_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putInt(SUPPORTED_MAP_SENSOR_TYPE, type)
            putBoolean(SUPPORTED_MAP_SENSOR_SUPPORTED, supported)
        }

        send(putDataMapReq)
    }

    private fun send(request: PutDataMapRequest) {
        val putDataReq = request
                .asPutDataRequest()
                .setUrgent()

        val dataItemTask = dataClient.putDataItem(putDataReq)

        if (BuildConfig.DEBUG) {
            dataItemTask.addOnSuccessListener {
                Log.d(TAG, "Success sent data")
            }
            dataItemTask.addOnFailureListener { ex ->
                Log.d(TAG, "Error sending data $ex")
            }
        }
    }
}
