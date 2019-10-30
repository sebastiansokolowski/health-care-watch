package com.sebastiansokolowski.healthcarewatch.model

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import com.sebastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.sebastiansokolowski.healthcarewatch.utils.HealthCareEnginesUtils
import com.sebastiansokolowski.shared.DataClientPaths

/**
 * Created by Sebastian Sokołowski on 06.07.19.
 */
class MeasurementModel(context: Context) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    lateinit var sensorDataModel: SensorDataModel

    init {
        Wearable.getDataClient(context).addListener(this)
    }

    override fun onDataChanged(dataEvent: DataEventBuffer) {
        dataEvent.forEach { event ->
            Log.v(TAG, "onDataChanged path:${event.dataItem.uri.path}")
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

                        Log.d(TAG, "settings samplingUs=$samplingUs sensors=$healthCareEvents engines=$healthCareEngines")
                        val measurementSettings = MeasurementSettings(samplingUs, sensors, healthCareEngines)

                        if (sensorDataModel.measurementRunning) {
                            sensorDataModel.stopMeasurement()
                        }
                        sensorDataModel.startMeasurement(measurementSettings)
                    }
                }
            }
        }
    }
}