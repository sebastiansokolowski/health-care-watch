package com.sebastiansokolowski.healthguard.model

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.client.WearableDataClient
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.db.entity.MeasurementEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.service.MeasurementService
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.SupportedHealthEventTypes
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.objectbox.BoxStore
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
class SensorDataModel(val context: Context, private val wearableDataClient: WearableDataClient, private val notificationModel: NotificationModel, private val boxStore: BoxStore, private val settingsModel: SettingsModel) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    var measurementRunning: Boolean = false
    private var measurementEventEntity: MeasurementEventEntity? = null
    private var saveDataDisposable: Disposable? = null

    //observables
    var heartRateObservable: BehaviorSubject<SensorEventEntity> = BehaviorSubject.create()
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val sensorsObservable: PublishSubject<SensorEventEntity> = PublishSubject.create()
    val healthEventObservable: PublishSubject<HealthEventEntity> = PublishSubject.create()
    val supportedHealthEventsObservable: PublishSubject<Set<HealthEventType>> = PublishSubject.create()

    //boxes
    val sensorEventEntityBox = boxStore.boxFor(SensorEventEntity::class.java)
    val measurementEventEntityBox = boxStore.boxFor(MeasurementEventEntity::class.java)

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
        val serviceIntent = Intent(context, MeasurementService::class.java)
        if (state) {
            context.startService(serviceIntent)
            heartRateObservable = BehaviorSubject.create()
        } else {
            context.stopService(serviceIntent)
            heartRateObservable.onComplete()
        }
        measurementStateObservable.onNext(measurementRunning)
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

        measurementEventEntity = createMeasurementEventEntity(measurementSettings).apply {
            measurementEventEntityBox.put(this)
            measurementSettings.measurementId = id
        }

        wearableDataClient.sendStartMeasurementEvent(measurementSettings)
    }

    fun startMeasurement() {
        measurementEventEntity?.apply {
            startTimestamp = Date().time
            measurementEventEntityBox.put(this)
        }

        notifyMeasurementStateObservable(true)
        if (measurementRunning) {
            return
        }
        changeMeasurementState(true)
        saveDataToDatabase()
    }

    fun stopMeasurement() {
        measurementEventEntity?.apply {
            stopTimestamp = Date().time
            measurementEventEntityBox.put(this)
        }

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
                    if (it.isEmpty()) {
                        return@subscribe
                    }
                    Log.d(TAG, "save data to database")
                    sensorEventEntityBox.put(it)

                    if (!measurementRunning) {
                        saveDataDisposable?.dispose()
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
                DataClientPaths.SENSOR_EVENTS_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val sensorEventsJson = getStringArrayList(DataClientPaths.SENSOR_EVENTS_MAP_ARRAY_LIST)

                        val dataToSave = mutableListOf<SensorEventEntity>()
                        sensorEventsJson.forEach {
                            val sensorEvent = Gson().fromJson(it, SensorEvent::class.java)
                            val sensorEventData = createSensorEventEntity(sensorEvent)

                            if (sensorEventData.type == Sensor.TYPE_HEART_RATE) {
                                notifyHeartRateObservable(sensorEventData)
                            }
                            notifySensorsObservable(sensorEventData)

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

                        val healthEventEntity = createHealthEventEntity(healthEvent)

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

    private fun createMeasurementEventEntity(measurementSettings: MeasurementSettings): MeasurementEventEntity {
        return MeasurementEventEntity().apply {
            this.measurementSettings = Gson().toJson(measurementSettings)
        }
    }

    private fun createSensorEventEntity(sensorEvent: SensorEvent): SensorEventEntity {
        return SensorEventEntity().apply {
            this.type = sensorEvent.type
            this.accuracy = sensorEvent.accuracy
            this.timestamp = sensorEvent.timestamp
            this.values = sensorEvent.values
            linkMeasurementEventEntity(sensorEvent.measurementId, this)
        }
    }

    private fun createHealthEventEntity(healthEvent: HealthEvent): HealthEventEntity {
        return HealthEventEntity().apply {
            this.event = healthEvent.healthEventType
            this.value = healthEvent.value
            this.sensorEventEntity.target = createSensorEventEntity(healthEvent.sensorEvent)
            this.details = healthEvent.details
            linkMeasurementEventEntity(healthEvent.measurementId, this)
        }
    }

    private fun linkMeasurementEventEntity(measurementId: Long, sensorEventEntity: SensorEventEntity) {
        var measurementEventEntity = measurementEventEntity
        if (measurementEventEntity == null || measurementEventEntity.id != measurementId) {
            measurementEventEntity = measurementEventEntityBox.get(measurementId)
        }
        measurementEventEntity?.sensorEventEntities?.add(sensorEventEntity)
        sensorEventEntity.measurementEventEntity.target = measurementEventEntity
    }

    private fun linkMeasurementEventEntity(measurementId: Long, healthEventEntity: HealthEventEntity) {
        var measurementEventEntity = measurementEventEntity
        if (measurementEventEntity == null || measurementEventEntity.id != measurementId) {
            measurementEventEntity = measurementEventEntityBox.get(measurementId)
        }
        measurementEventEntity?.healthEventEntities?.add(healthEventEntity)
        healthEventEntity.measurementEventEntity.target = measurementEventEntity
    }
}