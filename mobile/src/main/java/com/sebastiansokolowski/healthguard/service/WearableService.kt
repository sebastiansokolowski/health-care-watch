package com.sebastiansokolowski.healthguard.service

import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import com.sebastiansokolowski.healthguard.model.SetupModel
import com.sebastiansokolowski.shared.DataClientPaths
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class WearableService : WearableListenerService() {

    @Inject
    lateinit var sensorDataModel: SensorDataModel

    @Inject
    lateinit var wearableClient: WearableClient

    @Inject
    lateinit var setupModel: SetupModel

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo?) {
        super.onCapabilityChanged(capabilityInfo)
        capabilityInfo?.let {
            if(it.nodes.isNullOrEmpty()){
                sensorDataModel.stopMeasurement()
            }
        }
    }

    override fun onMessageReceived(event: MessageEvent?) {
        when (event?.path) {
            DataClientPaths.START_MEASUREMENT -> {
                sensorDataModel.startMeasurement()
            }
            DataClientPaths.STOP_MEASUREMENT -> {
                sensorDataModel.stopMeasurement()
            }
            DataClientPaths.REQUEST_START_MEASUREMENT -> {
                sensorDataModel.requestStartMeasurement()
            }
            else -> super.onMessageReceived(event)
        }
    }
}