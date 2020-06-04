package com.sebastiansokolowski.healthguard.client

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings

/**
 * Created by Sebastian Sokołowski on 17.03.19.
 */
class WearableClient(context: Context) {
    private val TAG = javaClass.canonicalName

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)

    fun sendStopMeasurementEvent() {
        Log.d(TAG, "sendStopMeasurementEvent")

        sendMessage(DataClientPaths.STOP_MEASUREMENT)
    }

    fun sendStartMeasurementEvent(measurementSettings: MeasurementSettings) {
        Log.d(TAG, "sendStartMeasurementEvent measurementSettings: $measurementSettings")

        val putDataMapReq = PutDataMapRequest.create(DataClientPaths.MEASUREMENT_START_DATA_PATH)
        putDataMapReq.dataMap.apply {
            putString(DataClientPaths.MEASUREMENT_START_DATA_JSON, Gson().toJson(measurementSettings))
        }

        sendData(putDataMapReq)
    }

    fun sendLiveData(enabled: Boolean) {
        if (enabled) {
            sendMessage(DataClientPaths.START_LIVE_DATA)
        } else {
            sendMessage(DataClientPaths.STOP_LIVE_DATA)
        }
    }

    fun getMeasurementState() {
        Log.d(TAG, "getMeasurementState")

        sendMessage(DataClientPaths.GET_MEASUREMENT)
    }

    fun getSupportedHealthEvents() {
        Log.d(TAG, "getSupportedHealthEvents")

        sendMessage(DataClientPaths.GET_SUPPORTED_HEALTH_EVENTS)
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

    private fun sendData(request: PutDataMapRequest) {
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

    data class Settings(val samplingUs: Int, val healthEvents: Set<String>)
}