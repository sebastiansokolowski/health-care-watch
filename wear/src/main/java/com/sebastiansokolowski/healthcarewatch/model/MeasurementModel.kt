package com.sebastiansokolowski.healthcarewatch.model

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.dataModel.FallSettings
import com.sebastiansokolowski.shared.dataModel.MeasurementSettings

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
                        //watch
                        val samplingUs = getInt(DataClientPaths.MEASUREMENT_START_DATA_SAMPLING_US)
                        val healthCareEvents = getStringArrayList(DataClientPaths.MEASUREMENT_START_DATA_HEALTH_CARE_EVENTS)
                        //fall
                        val fallThreshold = getInt(DataClientPaths.MEASUREMENT_START_DATA_FALL_THRESHOLD)
                        val fallStepDetector = getBoolean(DataClientPaths.MEASUREMENT_START_DATA_FALL_STEP_DETECTOR)
                        val fallTimeOfInactivityS = getInt(DataClientPaths.MEASUREMENT_START_DATA_FALL_TIME_OF_INACTIVITY_S)
                        val fallActivityThreshold = getInt(DataClientPaths.MEASUREMENT_START_DATA_FALL_ACTIVITY_THRESHOLD)

                        Log.d(TAG, "settings samplingUs=$samplingUs sensors=$healthCareEvents")
                        Log.d(TAG, "fall settings fallThreshold=$fallThreshold fallStepDetector=$fallStepDetector fallTimeOfInactivityS=$fallTimeOfInactivityS fallActivityThreshold=$fallActivityThreshold")
                        val fallSettings = FallSettings(fallThreshold, fallStepDetector, fallTimeOfInactivityS, fallActivityThreshold)
                        val measurementSettings = MeasurementSettings(samplingUs, healthCareEvents, fallSettings)

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