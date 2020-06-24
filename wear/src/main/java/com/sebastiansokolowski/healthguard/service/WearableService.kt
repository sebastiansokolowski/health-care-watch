package com.sebastiansokolowski.healthguard.service

import com.google.android.gms.wearable.*
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.model.MeasurementModel
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import com.sebastiansokolowski.shared.DataClientPaths
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class WearableService : WearableListenerService() {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var measurementModel: MeasurementModel

    @Inject
    lateinit var sensorDataModel: SensorDataModel

    @Inject
    lateinit var wearableClient: WearableClient

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        measurementModel.stopMeasurement()
    }

    override fun onDestroy() {
        super.onDestroy()
        measurementModel.stopMeasurement()
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo?) {
        capabilityInfo?.let {
            if (it.nodes.isNullOrEmpty()) {
                measurementModel.stopMeasurement()
            }
        }
    }

    override fun onDataChanged(dataEvent: DataEventBuffer?) {
        dataEvent?.forEach { event ->
            Timber.v("onDataChanged path:${event.dataItem.uri.path}")
            if (event.type != DataEvent.TYPE_CHANGED) {
                Timber.d( "type not changed")
                return
            }
            when ("/" + event.dataItem.uri.pathSegments.getOrNull(0)) {
                DataClientPaths.MEASUREMENT_START_DATA_PATH -> {
                    measurementModel.onDataChanged(event.dataItem)
                }
            }
        }
    }

    override fun onMessageReceived(event: MessageEvent?) {
        when (event?.path) {
            DataClientPaths.STOP_MEASUREMENT_PATH -> {
                measurementModel.stopMeasurement()
            }
            DataClientPaths.GET_MEASUREMENT_PATH -> {
                measurementModel.notifyMeasurementState()
            }
            DataClientPaths.START_LIVE_DATA_PATH -> {
                sensorDataModel.changeLiveDataState(true)
            }
            DataClientPaths.STOP_LIVE_DATA_PATH -> {
                sensorDataModel.changeLiveDataState(false)
            }
            DataClientPaths.GET_SUPPORTED_HEALTH_EVENTS_PATH -> {
                measurementModel.notifySupportedHealthEvents()
            }
        }
    }
}