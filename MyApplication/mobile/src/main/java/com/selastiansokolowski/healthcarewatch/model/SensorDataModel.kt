package com.selastiansokolowski.healthcarewatch.model

import android.content.Context
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.wearable.*
import com.selastiansokolowski.healthcarewatch.dataModel.SensorEventAccuracy
import com.selastiansokolowski.healthcarewatch.dataModel.SensorEventData
import com.selastiansokolowski.healthcarewatch.dataModel.SensorEventSupportedInfo
import com.selastiansokolowski.shared.DataClientPaths
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
class SensorDataModel(context: Context) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    private val sensorEventData = arrayListOf<SensorEventData>()
    private val sensorEventAccuracy = arrayListOf<SensorEventAccuracy>()
    private val sensorEventSupportedInfo = arrayListOf<SensorEventSupportedInfo>()

    val observableSensorEventData = BehaviorSubject.create<SensorEventData>()
    val observableSensorEventAccuracy = BehaviorSubject.create<SensorEventAccuracy>()
    val observableEventSupportedInfo = BehaviorSubject.create<SensorEventSupportedInfo>()

    init {
        Wearable.getDataClient(context).addListener(this)
//        generateDataLine()
    }

    private fun generateDataLine() {
        for (i in 1..100) {
            val array = FloatArray(3)
            array[0] = ((Math.random() * 65f).toFloat())
            array[1] = ((Math.random() * 65f).toFloat())
            array[2] = ((Math.random() * 65f).toFloat())
            val sensorDataEvent = SensorEventData(Sensor.TYPE_HEART_RATE, 10, Date().time - i * 100000, array)

            sensorEventData.add(sensorDataEvent)
        }
    }

    override fun onDataChanged(dataEvent: DataEventBuffer) {
        dataEvent.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "type not changed")
                return
            }

            when (event.dataItem.uri.path) {
                DataClientPaths.DATA_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val type = getInt(DataClientPaths.DATA_MAP_SENSOR_EVENT_SENSOR_TYPE)
                        val values = getFloatArray(DataClientPaths.DATA_MAP_SENSOR_EVENT_VALUES_KEY)
                        val accuracy = getInt(DataClientPaths.DATA_MAP_SENSOR_EVENT_ACCURACY_KEY)
                        val timestamp = getLong(DataClientPaths.DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY)


                        val sensorEvent = SensorEventData(type, accuracy, timestamp, values)

                        sensorEventData.add(sensorEvent)
                        observableSensorEventData.onNext(sensorEvent)
                        Log.d(TAG, "$sensorEvent")
                    }
                }
                DataClientPaths.ACCURACY_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val type = getInt(DataClientPaths.ACCURACY_MAP_SENSOR_TYPE)
                        val accuracy = getInt(DataClientPaths.ACCURACY_MAP_SENSOR_ACCURACY)

                        val sensorEvent = SensorEventAccuracy(type, accuracy)

                        sensorEventAccuracy.add(sensorEvent)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
                DataClientPaths.SUPPORTED_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val type = getInt(DataClientPaths.SUPPORTED_MAP_SENSOR_TYPE)
                        val supported = getBoolean(DataClientPaths.SUPPORTED_MAP_SENSOR_SUPPORTED)

                        val sensorEvent = SensorEventSupportedInfo(type, supported)

                        sensorEventSupportedInfo.add(sensorEvent)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
            }
        }
    }
}