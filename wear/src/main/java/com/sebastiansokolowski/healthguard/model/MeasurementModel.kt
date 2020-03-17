package com.sebastiansokolowski.healthguard.model

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings

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
                DataClientPaths.MEASUREMENT_START_DATA_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val json = getString(DataClientPaths.MEASUREMENT_START_DATA_JSON)
                        val measurementSettings = Gson().fromJson(json, MeasurementSettings::class.java)

                        Log.d(TAG, "measurementSettings=$measurementSettings")

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