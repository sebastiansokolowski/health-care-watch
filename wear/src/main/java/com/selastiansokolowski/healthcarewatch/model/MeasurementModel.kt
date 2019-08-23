package com.selastiansokolowski.healthcarewatch.model

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import com.selastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.selastiansokolowski.healthcarewatch.utils.HealthCareEnginesUtils
import com.selastiansokolowski.shared.DataClientPaths

/**
 * Created by Sebastian Sokołowski on 06.07.19.
 */
class MeasurementModel(context: Context) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    var sensorDataModel: SensorDataModel? = null

    init {
        Wearable.getDataClient(context).addListener(this)
    }

    override fun onDataChanged(dataEvent: DataEventBuffer) {
        dataEvent.forEach { event ->
            Log.d(TAG, "onDataChanged path:${event.dataItem.uri.path}")
            if (event.type != DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "type not changed")
                return
            }
            when (event.dataItem.uri.path) {
                DataClientPaths.MEASUREMENT_START_DATA -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val samplingUs = getInt(DataClientPaths.MEASUREMENT_START_DATA_SAMPLING_US)

                        val healthCareEvents = getStringArrayList(DataClientPaths.MEASUREMENT_START_DATA_HEALTH_CARE_EVENTS)
                        val healthCareEngines = HealthCareEnginesUtils.getHealthCareEngines(healthCareEvents)
                        val sensors = healthCareEngines.flatMap { it.requiredSensors() }.toSet()

                        Log.d(TAG, "settings samplingUs=$samplingUs sensors=$healthCareEvents")
                        val measurementSettings = MeasurementSettings(samplingUs, sensors)
                        sensorDataModel?.let {
                            if (it.measurementRunning) {
                                it.stopMeasurement()
                            }
                            sensorDataModel?.startMeasurement(measurementSettings)
                        }
                    }
                }
            }
        }
    }
}