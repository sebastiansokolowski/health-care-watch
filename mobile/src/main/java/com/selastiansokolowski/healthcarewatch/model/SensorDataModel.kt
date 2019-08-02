package com.selastiansokolowski.healthcarewatch.model

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.wearable.*
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.service.MeasurementService
import com.selastiansokolowski.shared.DataClientPaths
import io.objectbox.BoxStore
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
class SensorDataModel(val context: Context, private val wearableDataClient: WearableDataClient, private val boxStore: BoxStore) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    private var measurementRunning: Boolean = false
    private var saveDataDisposable: Disposable? = null

    val sensorsObservable: PublishSubject<SensorEventData> = PublishSubject.create()
    val heartRateObservable: PublishSubject<SensorEventData> = PublishSubject.create()
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()

    init {
        Wearable.getDataClient(context).addListener(this)
        wearableDataClient.getMeasurementState()
    }

    private fun notifyHeartRateObservable(sensorEventData: SensorEventData) {
        heartRateObservable.onNext(sensorEventData)
    }

    private fun notifySensorsObservable(sensorEventData: SensorEventData) {
        sensorsObservable.onNext(sensorEventData)
    }

    private fun changeMeasurementState(state: Boolean) {
        if (measurementRunning == state) {
            return
        }
        measurementRunning = state
        measurementStateObservable.onNext(measurementRunning)
        wearableDataClient.sendMeasurementEvent(state)
        val serviceIntent = Intent(context, MeasurementService::class.java)
        if (state) {
            context.startService(serviceIntent)
        } else {
            context.stopService(serviceIntent)
        }
    }

    fun toggleMeasurementState() {
        synchronized(this) {
            if (measurementRunning) {
                stopMeasurement()
            } else {
                startMeasurement()
            }
        }
    }

    fun startMeasurement() {
        synchronized(this) {
            if (measurementRunning) {
                return
            }
            changeMeasurementState(true)
            saveDataToDatabase()
        }
    }

    fun stopMeasurement() {
        synchronized(this) {
            if (!measurementRunning) {
                return
            }
            changeMeasurementState(false)
        }
    }

    private fun saveDataToDatabase() {
        saveDataDisposable = sensorsObservable
                .subscribeOn(Schedulers.io())
                .buffer(10, TimeUnit.SECONDS)
                .subscribe {
                    Log.d(TAG, "save data to database")
                    val eventBox = boxStore.boxFor(SensorEventData::class.java)
                    eventBox.put(it)

                    synchronized(this) {
                        if (!measurementRunning) {
                            saveDataDisposable?.dispose()
                        }
                    }
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
                        val accuracy = getInt(DataClientPaths.DATA_MAP_SENSOR_EVENT_ACCURACY_KEY)
                        val timestamp = getLong(DataClientPaths.DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY)
                        val values = getFloatArray(DataClientPaths.DATA_MAP_SENSOR_EVENT_VALUES_KEY)

                        val sensorEvent = SensorEventData().apply {
                            this.type = type
                            this.accuracy = accuracy
                            this.timestamp = timestamp
                            this.values = values
                        }

                        if (type == Sensor.TYPE_HEART_RATE) {
                            notifyHeartRateObservable(sensorEvent)
                        }
                        notifySensorsObservable(sensorEvent)

                        Log.d(TAG, "$sensorEvent")
                    }
                }
            }
        }
    }
}