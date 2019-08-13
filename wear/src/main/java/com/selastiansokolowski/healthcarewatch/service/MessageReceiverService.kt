package com.selastiansokolowski.healthcarewatch.service

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.shared.DataClientPaths
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class MessageReceiverService : WearableListenerService() {

    @Inject
    lateinit var sensorDataModel: SensorDataModel

    @Inject
    lateinit var wearableDataClient: WearableDataClient

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onMessageReceived(event: MessageEvent?) {
        when (event?.path) {
            DataClientPaths.START_MEASUREMENT -> {
                sensorDataModel.startMeasurement()
            }
            DataClientPaths.STOP_MEASUREMENT -> {
                sensorDataModel.stopMeasurement()
            }
            DataClientPaths.GET_MEASUREMENT -> {
                sensorDataModel.notifyMeasurementState()
            }
            DataClientPaths.START_LIVE_DATA -> {
                wearableDataClient.liveData = true
            }
            DataClientPaths.STOP_LIVE_DATA -> {
                wearableDataClient.liveData = false
            }
            DataClientPaths.GET_SUPPORTED_HEALTH_CARE_EVENTS -> {
                sensorDataModel.notifySupportedHealthCareEvents()
            }
            else -> super.onMessageReceived(event)
        }
    }
}