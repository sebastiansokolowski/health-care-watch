package com.sebastiansokolowski.healthguard.client

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.utils.SensorEventValuesSerializer
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.DataClientPaths.Companion.HEALTH_EVENT_MAP_JSON
import com.sebastiansokolowski.shared.DataClientPaths.Companion.HEALTH_EVENT_MAP_PATH
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SENSOR_EVENTS_HIGH_FREQUENCY_DATA
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SENSOR_EVENTS_MAP_ARRAY_LIST
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SENSOR_EVENTS_MAP_PATH
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SUPPORTED_HEALTH_EVENTS_MAP_JSON
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SUPPORTED_HEALTH_EVENTS_MAP_PATH
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.SupportedHealthEventTypes
import timber.log.Timber


/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class WearableClient(context: Context) {
    private val maxSizeOfDataToSend = 500

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)

    fun sendMeasurementEvent(state: Boolean) {
        Timber.d("sendMeasurementEvent state=$state")

        if (state) {
            sendMessage(DataClientPaths.START_MEASUREMENT_PATH)
        } else {
            sendMessage(DataClientPaths.STOP_MEASUREMENT_PATH)
        }
    }

    fun sendSensorEvents(events: List<SensorEvent>, urgent: Boolean, highFrequencyData: Boolean = false) {
        Timber.d("sendSensorEvents size=${events.size}")

        val gson = GsonBuilder()
                .registerTypeAdapter(FloatArray::class.java, SensorEventValuesSerializer())
                .create()

        events.chunked(maxSizeOfDataToSend).iterator().forEach {
            val data = ArrayList<String>()
            it.forEach {
                if (BuildConfig.EXTRA_LOGGING) {
                    Timber.d("sendSensorEvents sensorEvent=${it}")
                }
                data.add(gson.toJson(it))
            }

            val putDataMapReq = PutDataMapRequest.createWithAutoAppendedId(SENSOR_EVENTS_MAP_PATH)
            Timber.v("sendSensorEvents uri=${putDataMapReq.uri} size=${it.size}")
            putDataMapReq.dataMap.apply {
                putStringArrayList(SENSOR_EVENTS_MAP_ARRAY_LIST, data)
                putBoolean(SENSOR_EVENTS_HIGH_FREQUENCY_DATA, highFrequencyData)
            }

            send(putDataMapReq, urgent)
        }
    }

    fun requestStartMeasurement() {
        Timber.d("requestStartMeasurement")

        sendMessage(DataClientPaths.REQUEST_START_MEASUREMENT_PATH)
    }

    fun sendSupportedHealthEvents(supportedHealthEventTypes: SupportedHealthEventTypes) {
        Timber.d("sendSupportedHealthEvents healthEventTypesSupported=$supportedHealthEventTypes")

        val putDataMapReq = PutDataMapRequest.createWithAutoAppendedId(SUPPORTED_HEALTH_EVENTS_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putString(SUPPORTED_HEALTH_EVENTS_MAP_JSON, Gson().toJson(supportedHealthEventTypes))
        }

        send(putDataMapReq, true)
    }

    fun sendHealthEvent(healthEvent: HealthEvent) {
        Timber.d("sendHealthEvent sensorEvent=${healthEvent.sensorEvent}")

        val putDataMapReq = PutDataMapRequest.createWithAutoAppendedId(HEALTH_EVENT_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putString(HEALTH_EVENT_MAP_JSON, Gson().toJson(healthEvent))
        }

        send(putDataMapReq, true)
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
                            Timber.d("Success sent message")
                        }
                        addOnFailureListener { ex ->
                            Timber.d("Error sending message $ex")
                        }
                    }
                }
            } else {
                Timber.d("missing node!")
            }
        }).start()
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
                Timber.v("Success uri=${request.uri} urgent=$urgent")
            }
            dataItemTask.addOnFailureListener { ex ->
                Timber.v("Error sending data $ex")
            }
        }
    }
}
