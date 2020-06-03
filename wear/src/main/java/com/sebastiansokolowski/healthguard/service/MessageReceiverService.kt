package com.sebastiansokolowski.healthguard.service

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.sebastiansokolowski.healthguard.client.WearableDataClient
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import com.sebastiansokolowski.shared.DataClientPaths
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
            DataClientPaths.STOP_MEASUREMENT -> {
                sensorDataModel.stopMeasurement()
            }
            DataClientPaths.GET_MEASUREMENT -> {
                sensorDataModel.notifyMeasurementState()
            }
            DataClientPaths.START_LIVE_DATA -> {
                wearableDataClient.changeLiveDataState(true)
            }
            DataClientPaths.STOP_LIVE_DATA -> {
                wearableDataClient.changeLiveDataState(false)
            }
            DataClientPaths.GET_SUPPORTED_HEALTH_EVENTS -> {
                sensorDataModel.notifySupportedHealthEvents()
            }
            else -> super.onMessageReceived(event)
        }
    }
}