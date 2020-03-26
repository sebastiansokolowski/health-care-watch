package com.sebastiansokolowski.healthguard.model

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.client.WearableDataClient
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.service.MeasurementService
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.SupportedHealthEventTypes
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

    val sensorsObservable: PublishSubject<SensorEventEntity> = PublishSubject.create()
    val healthEventObservable: PublishSubject<HealthEventEntity> = PublishSubject.create()
    val heartRateObservable: BehaviorSubject<SensorEventEntity> = BehaviorSubject.create()
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val supportedHealthEventsObservable: PublishSubject<Set<HealthEventType>> = PublishSubject.create()

    init {
        Wearable.getDataClient(context).addListener(this)
    }

    private fun notifyHeartRateObservable(sensorEventEntity: SensorEventEntity) {
        heartRateObservable.onNext(sensorEventEntity)
    }

    private fun notifySensorsObservable(sensorEventEntity: SensorEventEntity) {
        sensorsObservable.onNext(sensorEventEntity)
    }

    private fun notifyHealthEventObservable(healthEventEntity: HealthEventEntity) {
        notificationModel.notifyHealthEvent(healthEventEntity)
        healthEventObservable.onNext(healthEventEntity)
    }

    private fun notifyMeasurementStateObservable(measurementState: Boolean) {
        measurementStateObservable.onNext(measurementState)
    }

    private fun notifySupportedHealthEventsObservable(healthEvents: Set<HealthEventType>) {
        supportedHealthEventsObservable.onNext(healthEvents)
        supportedHealthEventsObservable.onComplete()
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
                    val eventBox = boxStore.boxFor(SensorEventEntity::class.java)
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
                DataClientPaths.SENSOR_EVENT_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val json = getString(DataClientPaths.SENSOR_EVENT_MAP_JSON)
                        val sensorEvent = Gson().fromJson(json, SensorEvent::class.java)

                        val sensorEventData = createSensorEventDataEntity(sensorEvent)

                        if (sensorEventData.type == Sensor.TYPE_HEART_RATE) {
                            notifyHeartRateObservable(sensorEventData)
                        }
                        notifySensorsObservable(sensorEventData)

                        Log.d(TAG, "sensorEvent=$sensorEventData")
                    }
                }
                DataClientPaths.SENSOR_EVENTS_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val sensorEventsJson = getStringArrayList(DataClientPaths.SENSOR_EVENTS_MAP_ARRAY_LIST)

                        val dataToSave = mutableListOf<SensorEventEntity>()
                        sensorEventsJson.forEach {
                            val sensorEvent = Gson().fromJson(it, SensorEvent::class.java)
                            val sensorEventData = createSensorEventDataEntity(sensorEvent)
                            dataToSave.add(sensorEventData)
                        }

                        val eventBox = boxStore.boxFor(SensorEventEntity::class.java)
                        eventBox.put(dataToSave)

                        Log.d(TAG, "sensorEvents size=${sensorEventsJson.size}")
                    }
                }
                DataClientPaths.HEALTH_EVENT_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val json = getString(DataClientPaths.HEALTH_EVENT_MAP_JSON)
                        val healthEvent = Gson().fromJson(json, HealthEvent::class.java)

                        val healthEventEntity = createHealthEvent(healthEvent)

                        val eventBox = boxStore.boxFor(HealthEventEntity::class.java)
                        eventBox.put(healthEventEntity)

                        notifyHealthEventObservable(healthEventEntity)

                        Log.d(TAG, "healthEvent=$healthEventEntity")
                    }
                }
                DataClientPaths.SUPPORTED_HEALTH_EVENTS_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val json = getString(DataClientPaths.HEALTH_EVENT_MAP_JSON)
                        val healthEventTypesSupported = Gson().fromJson(json, SupportedHealthEventTypes::class.java)

                        notifySupportedHealthEventsObservable(healthEventTypesSupported.supportedTypes)

                        Log.d(TAG, "supported health events :$healthEventTypesSupported")
                    }
                }
            }
        }
    }

    private fun createSensorEventDataEntity(sensorEvent: SensorEvent): SensorEventEntity {
        return SensorEventEntity().apply {
            this.type = sensorEvent.type
            this.accuracy = sensorEvent.accuracy
            this.timestamp = sensorEvent.timestamp
            this.values = sensorEvent.values
        }
    }

    private fun createHealthEvent(healthEvent: HealthEvent): HealthEventEntity {
        return HealthEventEntity().apply {
            this.event = healthEvent.healthEventType
            this.value = healthEvent.value
            this.sensorEventEntity.target = createSensorEventDataEntity(healthEvent.sensorEvent)
            this.details = healthEvent.details
            this.measurementSettings = healthEvent.measurementSettings
        }
    }
}