package com.selastiansokolowski.healthcarewatch.client

import android.content.Context
import android.hardware.SensorEvent
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.selastiansokolowski.healthcarewatch.BuildConfig
import com.selastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.selastiansokolowski.shared.DataClientPaths
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_PATH
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_ACCURACY_KEY
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_SENSOR_TYPE
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY
import com.selastiansokolowski.shared.DataClientPaths.Companion.DATA_MAP_SENSOR_EVENT_VALUES_KEY
import com.selastiansokolowski.shared.DataClientPaths.Companion.HEALTH_CARE_EVENT_DATA
import com.selastiansokolowski.shared.DataClientPaths.Companion.HEALTH_CARE_MAP_PATH
import com.selastiansokolowski.shared.DataClientPaths.Companion.HEALTH_CARE_TYPE


/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class WearableDataClient(context: Context) {
    private val TAG = javaClass.canonicalName

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)

    var liveData = false

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

    fun sendHealthCareEvent(healthCareEvent: HealthCareEvent) {
        Log.d(TAG, "sendHealthCareEvent sensorEvent=${healthCareEvent.sensorEvent}")

        val putDataMapReq = PutDataMapRequest.create(HEALTH_CARE_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putString(HEALTH_CARE_TYPE, healthCareEvent.healthCareEventType.name)
            putDataMap(HEALTH_CARE_EVENT_DATA, setSensorEventDataMap(DataMap(), healthCareEvent.sensorEvent))
        }

        send(putDataMapReq, true)
    }

    fun sendSensorEvent(event: SensorEvent) {
        Log.d(TAG, "sendSensorEvent event=${event.sensor.name}")

        val putDataMapReq = PutDataMapRequest.create(DATA_MAP_PATH)
        putDataMapReq.dataMap.apply {
            setSensorEventDataMap(this, event)
        }

        send(putDataMapReq, liveData)
    }

    private fun setSensorEventDataMap(dataMap: DataMap, event: SensorEvent): DataMap {
        return dataMap.apply {
            putFloatArray(DATA_MAP_SENSOR_EVENT_VALUES_KEY, event.values)
            putInt(DATA_MAP_SENSOR_EVENT_SENSOR_TYPE, event.sensor.type)
            putInt(DATA_MAP_SENSOR_EVENT_ACCURACY_KEY, event.accuracy)
            putLong(DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY, System.currentTimeMillis())
        }
    }

    private fun send(request: PutDataMapRequest, urgent: Boolean) {
        var putDataReq = request
                .asPutDataRequest()
        if (urgent) {
            putDataReq = putDataReq.setUrgent()
        }

        val dataItemTask = dataClient.putDataItem(putDataReq)

        if (BuildConfig.DEBUG) {
            dataItemTask.addOnSuccessListener {
                Log.d(TAG, "Success sent data urgent:$urgent")
            }
            dataItemTask.addOnFailureListener { ex ->
                Log.d(TAG, "Error sending data $ex")
            }
        }
    }
}
