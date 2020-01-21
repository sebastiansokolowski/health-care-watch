package com.sebastiansokolowski.healthcarewatch.client

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.sebastiansokolowski.healthcarewatch.BuildConfig
import com.sebastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.sebastiansokolowski.shared.DataClientPaths
import java.util.*

/**
 * Created by Sebastian Sokołowski on 17.03.19.
 */
class WearableDataClient(context: Context) {
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

        val putDataMapReq = PutDataMapRequest.create(DataClientPaths.MEASUREMENT_START_DATA)
        putDataMapReq.dataMap.apply {
            putInt(DataClientPaths.MEASUREMENT_START_DATA_SAMPLING_US, measurementSettings.samplingUs)
            putInt(DataClientPaths.MEASUREMENT_START_DATA_FALL_THRESHOLD, measurementSettings.fallThreshold)
            putBoolean(DataClientPaths.MEASUREMENT_START_DATA_FALL_STEP_DETECTOR, measurementSettings.fallStepDetector)
            putStringArrayList(DataClientPaths.MEASUREMENT_START_DATA_HEALTH_CARE_EVENTS, measurementSettings.healthCareEvents)
            putLong(DataClientPaths.MEASUREMENT_START_DATA_TIMESTAMP, Date().time)
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

    fun getSupportedHealthCareEvents() {
        Log.d(TAG, "getSupportedHealthCareEvents")

        sendMessage(DataClientPaths.GET_SUPPORTED_HEALTH_CARE_EVENTS)
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

    data class Settings(val samplingUs: Int, val healthCareEvents: Set<String>)
}