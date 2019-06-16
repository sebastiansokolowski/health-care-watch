package com.selastiansokolowski.healthcarewatch.model

import android.content.Context
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.wearable.*
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventAccuracy
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventSupportedInfo
import com.selastiansokolowski.shared.DataClientPaths
import io.objectbox.BoxStore
import io.reactivex.subjects.PublishSubject


/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
class SensorDataModel(context: Context, val boxStore: BoxStore) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    init {
        Wearable.getDataClient(context).addListener(this)
    }

    val heartRateObservable: PublishSubject<SensorEventData> = PublishSubject.create()
    val sensorsObservable: PublishSubject<SensorEventData> = PublishSubject.create()

    private fun notifyHeartRateObservable(sensorEventData: SensorEventData) {
        heartRateObservable.onNext(sensorEventData)
    }

    private fun notifySensorsObservable(sensorEventData: SensorEventData) {
        sensorsObservable.onNext(sensorEventData)
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
                        val accuracy = getInt(DataClientPaths.DATA_MAP_SENSOR_EVENT_ACCURACY_KEY)
                        val timestamp = getLong(DataClientPaths.DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY)
                        val values = getFloatArray(DataClientPaths.DATA_MAP_SENSOR_EVENT_VALUES_KEY)

                        val sensorEvent = SensorEventData().apply {
                            this.type = type
                            this.accuracy = accuracy
                            this.timestamp = timestamp
                            this.values = values
                        }

                        val eventBox = boxStore.boxFor(SensorEventData::class.java)
                        eventBox.put(sensorEvent)

                        if (type == Sensor.TYPE_HEART_RATE) {
                            notifyHeartRateObservable(sensorEvent)
                        }
                        notifySensorsObservable(sensorEvent)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
                DataClientPaths.ACCURACY_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val type = getInt(DataClientPaths.ACCURACY_MAP_SENSOR_TYPE)
                        val accuracy = getInt(DataClientPaths.ACCURACY_MAP_SENSOR_ACCURACY)

                        val sensorEvent = SensorEventAccuracy().apply {
                            this.type = type
                            this.accuracy = accuracy
                        }

                        val eventBox = boxStore.boxFor(SensorEventAccuracy::class.java)
                        eventBox.put(sensorEvent)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
                DataClientPaths.SUPPORTED_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val type = getInt(DataClientPaths.SUPPORTED_MAP_SENSOR_TYPE)
                        val supported = getBoolean(DataClientPaths.SUPPORTED_MAP_SENSOR_SUPPORTED)

                        val sensorEvent = SensorEventSupportedInfo().apply {
                            this.type = type
                            this.supported = supported
                        }

                        val eventBox = boxStore.boxFor(SensorEventSupportedInfo::class.java)
                        eventBox.put(sensorEvent)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
            }
        }
    }
}