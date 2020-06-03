package com.sebastiansokolowski.healthguard.client

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.DataClientPaths.Companion.HEALTH_EVENT_MAP_JSON
import com.sebastiansokolowski.shared.DataClientPaths.Companion.HEALTH_EVENT_MAP_PATH
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SENSOR_EVENTS_MAP_ARRAY_LIST
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SENSOR_EVENTS_MAP_PATH
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SUPPORTED_HEALTH_EVENTS_MAP_JSON
import com.sebastiansokolowski.shared.DataClientPaths.Companion.SUPPORTED_HEALTH_EVENTS_MAP_PATH
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.SupportedHealthEventTypes
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class WearableDataClient(context: Context) {
    private val TAG = javaClass.canonicalName

    private val maxSizeOfDataToSend = 500

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)

    private val sensorDataToSend = ArrayList<String>()
    private var sensorDataSynchronizerDisposable: Disposable = startSensorDataSynchronizer(false)

    fun changeLiveDataState(liveData: Boolean) {
        sensorDataSynchronizerDisposable.dispose()
        sensorDataSynchronizerDisposable = startSensorDataSynchronizer(liveData)
    }

    fun sendMeasurementEvent(state: Boolean) {
        Log.d(TAG, "sendMeasurementEvent state: $state")

        if (state) {
            sendMessage(DataClientPaths.START_MEASUREMENT)
        } else {
            sendMessage(DataClientPaths.STOP_MEASUREMENT)
        }
    }

    private fun startSensorDataSynchronizer(liveData: Boolean): Disposable {
        val period: Long = if (liveData) {
            1
        } else {
            300
        }

        return Observable.interval(period, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    syncSensorData(liveData)
                }
    }

    fun syncSensorData(urgent: Boolean) {
        Log.d(TAG, "syncSensorData size=${sensorDataToSend.size}")

        sensorDataToSend.chunked(maxSizeOfDataToSend).iterator().forEach {
            Log.v(TAG, "syncSensorData chunked size=${it.size}")

            val putDataMapReq = PutDataMapRequest.create(SENSOR_EVENTS_MAP_PATH)
            putDataMapReq.dataMap.apply {
                putStringArrayList(SENSOR_EVENTS_MAP_ARRAY_LIST, ArrayList(it))
            }

            send(putDataMapReq, urgent)
        }
        sensorDataToSend.clear()
    }

    fun requestStartMeasurement() {
        Log.d(TAG, "requestStartMeasurement")

        sendMessage(DataClientPaths.REQUEST_START_MEASUREMENT)
    }

    fun sendSupportedHealthEvents(supportedHealthEventTypes: SupportedHealthEventTypes) {
        Log.d(TAG, "sendSupportedHealthEvents healthEventTypesSupported: $supportedHealthEventTypes")

        val putDataMapReq = PutDataMapRequest.create(SUPPORTED_HEALTH_EVENTS_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putString(SUPPORTED_HEALTH_EVENTS_MAP_JSON, Gson().toJson(supportedHealthEventTypes))
        }

        send(putDataMapReq, true)
    }

    fun sendHealthEvent(healthEvent: HealthEvent) {
        Log.d(TAG, "sendHealthEvent sensorEvent=${healthEvent.sensorEvent}")

        val putDataMapReq = PutDataMapRequest.create(HEALTH_EVENT_MAP_PATH)
        putDataMapReq.dataMap.apply {
            putString(HEALTH_EVENT_MAP_JSON, Gson().toJson(healthEvent))
        }

        send(putDataMapReq, true)
    }

    fun sendSensorEvent(event: SensorEvent) {
        Schedulers.io().scheduleDirect {
            sensorDataToSend.add(Gson().toJson(event))
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

    private fun send(request: PutDataMapRequest, urgent: Boolean) {
        var putDataReq = request
                .asPutDataRequest()
        if (urgent) {
            putDataReq = putDataReq.setUrgent()
        }

        val dataItemTask = dataClient.putDataItem(putDataReq)

        if (BuildConfig.DEBUG) {
            dataItemTask.addOnSuccessListener {
                Log.v(TAG, "Success sent data path:${request.uri} urgent:$urgent")
            }
            dataItemTask.addOnFailureListener { ex ->
                Log.v(TAG, "Error sending data $ex")
            }
        }
    }
}
