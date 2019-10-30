package com.sebastiansokolowski.healthcarewatch.model

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.wearable.*
import com.sebastiansokolowski.healthcarewatch.client.WearableDataClient
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.sebastiansokolowski.healthcarewatch.service.MeasurementService
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.healthCare.HealthCareEventType
import io.objectbox.BoxStore
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
class SensorDataModel(val context: Context, private val wearableDataClient: WearableDataClient, private val notificationModel: NotificationModel, private val boxStore: BoxStore, private val settingsModel: SettingsModel) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    var measurementRunning: Boolean = false
    private var saveDataDisposable: Disposable? = null

    val sensorsObservable: PublishSubject<SensorEventData> = PublishSubject.create()
    val heartRateObservable: BehaviorSubject<SensorEventData> = BehaviorSubject.create()
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val supportedHealthCareEventsObservable: PublishSubject<List<HealthCareEventType>> = PublishSubject.create()

    init {
        Wearable.getDataClient(context).addListener(this)
    }

    private fun notifyHeartRateObservable(sensorEventData: SensorEventData) {
        heartRateObservable.onNext(sensorEventData)
    }

    private fun notifySensorsObservable(sensorEventData: SensorEventData) {
        sensorsObservable.onNext(sensorEventData)
    }

    private fun notifyMeasurementStateObservable(measurementState: Boolean) {
        measurementStateObservable.onNext(measurementState)
    }

    private fun notifySupportedHealthCareEventsObservable(healthCareEvents: List<HealthCareEventType>) {
        supportedHealthCareEventsObservable.onNext(healthCareEvents)
        supportedHealthCareEventsObservable.onComplete()
    }

    private fun changeMeasurementState(state: Boolean) {
        if (measurementRunning == state) {
            return
        }
        measurementRunning = state
        measurementStateObservable.onNext(measurementRunning)
        val serviceIntent = Intent(context, MeasurementService::class.java)
        if (state) {
            context.startService(serviceIntent)
        } else {
            context.stopService(serviceIntent)
        }
    }

    fun toggleMeasurementState() {
        if (measurementRunning) {
            wearableDataClient.sendStopMeasurementEvent()
            stopMeasurement()
        } else {
            requestStartMeasurement()
        }
    }

    fun requestStartMeasurement() {
        val measurementSettings = settingsModel.getMeasurementSettings()
        wearableDataClient.sendStartMeasurementEvent(measurementSettings)
    }

    fun startMeasurement() {
        notifyMeasurementStateObservable(true)
        if (measurementRunning) {
            return
        }
        changeMeasurementState(true)
        saveDataToDatabase()
    }

    fun stopMeasurement() {
        notifyMeasurementStateObservable(false)
        if (!measurementRunning) {
            return
        }
        changeMeasurementState(false)
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
            Log.d(TAG, "onDataChanged path:${event.dataItem.uri.path}")
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

                        Log.d(TAG, "sensorEvent=$sensorEvent")
                    }
                }
                DataClientPaths.HEALTH_CARE_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val type = getString(DataClientPaths.HEALTH_CARE_TYPE)
                        val eventDataMap = getDataMap(DataClientPaths.HEALTH_CARE_EVENT_DATA)
                        val sensorEventData = createSensorEventData(eventDataMap)

                        val healthCareEvent = HealthCareEvent().apply {
                            try {
                                this.careEvent = HealthCareEventType.valueOf(type)
                            } catch (e: IllegalArgumentException) {
                                return@forEach
                            }
                            this.sensorEventData.target = sensorEventData
                        }

                        val eventBox = boxStore.boxFor(HealthCareEvent::class.java)
                        eventBox.put(healthCareEvent)

                        notificationModel.notifyHealthCareEvent(healthCareEvent)

                        Log.d(TAG, "healthCareEvent=$healthCareEvent")
                    }
                }
                DataClientPaths.SUPPORTED_HEALTH_CARE_EVENTS_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val supportedHealthCareEventsNames = getStringArrayList(DataClientPaths.SUPPORTED_HEALTH_CARE_EVENTS_MAP_TYPES)
                        val supportedHealthCareEvents = supportedHealthCareEventsNames.mapNotNull {
                            try {
                                HealthCareEventType.valueOf(it)
                            } catch (e: IllegalArgumentException) {
                                null
                            }
                        }
                        notifySupportedHealthCareEventsObservable(supportedHealthCareEvents)

                        Log.d(TAG, "supported health care events :$supportedHealthCareEventsNames")
                    }
                }
            }
        }
    }

    private fun createSensorEventData(dataMap: DataMap): SensorEventData {
        val type = dataMap.getInt(DataClientPaths.DATA_MAP_SENSOR_EVENT_SENSOR_TYPE)
        val accuracy = dataMap.getInt(DataClientPaths.DATA_MAP_SENSOR_EVENT_ACCURACY_KEY)
        val timestamp = dataMap.getLong(DataClientPaths.DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY)
        val values = dataMap.getFloatArray(DataClientPaths.DATA_MAP_SENSOR_EVENT_VALUES_KEY)

        return SensorEventData().apply {
            this.type = type
            this.accuracy = accuracy
            this.timestamp = timestamp
            this.values = values
        }
    }
}