package com.selastiansokolowski.healthcarewatch.client

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.selastiansokolowski.healthcarewatch.BuildConfig
import com.selastiansokolowski.shared.DataClientPaths
import com.selastiansokolowski.shared.DataClientPaths.Companion.ACCURACY_MAP_PATH
import com.selastiansokolowski.shared.DataClientPaths.Companion.ACCURACY_MAP_SENSOR_ACCURACY
import com.selastiansokolowski.shared.DataClientPaths.Companion.ACCURACY_MAP_SENSOR_TYPE
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_PATH
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_ACCURACY_KEY
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_SENSOR_TYPE
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_VALUES_KEY


/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class WearableDataClient(context: Context) {
    private val TAG = javaClass.canonicalName

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)

    var urgentData = false

    fun sendMeasurementEvent(state: Boolean) {
        Log.d(TAG, "sendMeasurementEvent state: $state")

        if (state) {
            sendMessage(DataClientPaths.START_MEASUREMENT)
        } else {
            sendMessage(DataClientPaths.STOP_MEASUREMENT)
        }
    }

    private fun sendMessage(message: String) {
        Thread(Runnable {
            val task = capabilityClient.getCapability(
                    DataClientPaths.NODE_CAPABILITY,
                    CapabilityClient.FILTER_REACHABLE)
            val capabilityInfo: CapabilityInfo = Tasks.await(task)

            if (capabilityInfo.nodes.isNotEmpty()) {
                val nodeId = capabilityInfo.nodes.iterator().next()
                messageClient.sendMessage(nodeId.id, message, null).apply {
                    if (BuildConfig.DEBUG) {
                        addOnSuccessListener {
                            Log.d(TAG, "Success sent message")
                        }
                        addOnFailureListener { ex ->
                            Log.d(TAG, "Error sending message $ex")
                        }
                    }
                }
            } else {
                Log.d(TAG, "missing node!")
            }
        }).start()
    }

    fun sendSensorEvent(event: SensorEvent) {
        Log.d(TAG, "sendSensorEvent event=${event.sensor.name}")

        val putDataMapReq = PutDataMapRequest.create(DATA_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putFloatArray(DATA_MAP_SENSOR_EVENT_VALUES_KEY, event.values)
            putInt(DATA_MAP_SENSOR_EVENT_SENSOR_TYPE, event.sensor.type)
            putInt(DATA_MAP_SENSOR_EVENT_ACCURACY_KEY, event.accuracy)
            putLong(DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY, System.currentTimeMillis())
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

    private fun send(request: PutDataMapRequest) {
        var putDataReq = request
                .asPutDataRequest()
        if (urgentData) {
            putDataReq = putDataReq.setUrgent()
        }

        val dataItemTask = dataClient.putDataItem(putDataReq)

        if (BuildConfig.DEBUG) {
            dataItemTask.addOnSuccessListener {
                Log.d(TAG, "Success sent data urgent:$urgentData")
            }
            dataItemTask.addOnFailureListener { ex ->
                Log.d(TAG, "Error sending data $ex")
            }
        }
    }
}
