package com.sebastiansokolowski.healthguard.model

import android.content.Context
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.db.entity.MeasurementEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity_
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.objectbox.BoxStore
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
class SensorDataModel(val context: Context, private val notificationModel: NotificationModel, boxStore: BoxStore) {
    private val TAG = javaClass.canonicalName

    var measurementEventEntity: MeasurementEventEntity? = null

    //observables
    var heartRateObservable: BehaviorSubject<SensorEventEntity> = BehaviorSubject.create()
    val sensorsObservable: PublishSubject<SensorEventEntity> = PublishSubject.create()
    val healthEventObservable: PublishSubject<HealthEventEntity> = PublishSubject.create()

    //boxes
    private val sensorEventEntityBox = boxStore.boxFor(SensorEventEntity::class.java)
    private val healthEventEntityBox = boxStore.boxFor(HealthEventEntity::class.java)
    private val measurementEventEntityBox = boxStore.boxFor(MeasurementEventEntity::class.java)

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

    private fun removeDuplicatedSensorEvents(sampleSensorEvents: MutableList<SensorEventEntity>) {
        if (sampleSensorEvents.isNotEmpty()) {
            val event = sampleSensorEvents.first()
            sensorEventEntityBox.query().apply {
                equal(SensorEventEntity_.type, event.type.toLong())
                between(SensorEventEntity_.timestamp, sampleSensorEvents.first().timestamp, sampleSensorEvents.last().timestamp)
            }.build().remove()
        }
    }

    fun onDataChanged(dataItem: DataItem) {
        when ("/" + dataItem.uri.pathSegments.getOrNull(0)) {
            DataClientPaths.SENSOR_EVENTS_MAP_PATH -> {
                DataMapItem.fromDataItem(dataItem).dataMap.apply {
                    val sensorEventsJson = getStringArrayList(DataClientPaths.SENSOR_EVENTS_MAP_ARRAY_LIST)
                    val highFrequencyData = getBoolean(DataClientPaths.SENSOR_EVENTS_HIGH_FREQUENCY_DATA)

                    val sensorEvents = mutableListOf<SensorEventEntity>()
                    sensorEventsJson.forEach {
                        val sensorEvent = Gson().fromJson(it, SensorEvent::class.java)
                        val sensorEventEntity = createSensorEventEntity(sensorEvent)

                        Log.d(TAG, "onDataChanged sensorEvent=${sensorEvent}")

                        if (isValid(sensorEventEntity)) {
                            if (sensorEventEntity.type == Sensor.TYPE_HEART_RATE) {
                                notifyHeartRateObservable(sensorEventEntity)
                            }
                            notifySensorsObservable(sensorEventEntity)

                            sensorEvents.add(sensorEventEntity)
                        }
                    }

                    if (highFrequencyData) {
                        removeDuplicatedSensorEvents(sensorEvents)
                    }

                    sensorEventEntityBox.put(sensorEvents)
                    Log.d(TAG, "onDataChanged dataToSave.size=${sensorEvents.size}")
                }
            }
            DataClientPaths.HEALTH_EVENT_MAP_PATH -> {
                DataMapItem.fromDataItem(dataItem).dataMap.apply {
                    val json = getString(DataClientPaths.HEALTH_EVENT_MAP_JSON)
                    val healthEvent = Gson().fromJson(json, HealthEvent::class.java)
                    val healthEventEntity = createHealthEventEntity(healthEvent)

                    if (isValid(healthEventEntity)) {
                        notifyHealthEventObservable(healthEventEntity)

                        healthEventEntityBox.put(healthEventEntity)
                    }
                    Log.d(TAG, "healthEvent=$healthEventEntity")
                }
            }
        }
    }

    private fun isValid(sensorEventEntity: SensorEventEntity): Boolean {
        sensorEventEntity.run {
            if (measurementEventEntity.isNull) {
                return false
            }
        }

        return true
    }

    private fun isValid(healthEventEntity: HealthEventEntity): Boolean {
        healthEventEntity.run {
            if (measurementEventEntity.isNull) {
                return false
            }
            if (sensorEventEntity.isNull) {
                return false
            }
            if (sensorEventEntity.target.measurementEventEntity.isNull) {
                return false
            }
        }

        return true
    }

    private fun createSensorEventEntity(sensorEvent: SensorEvent): SensorEventEntity {
        return SensorEventEntity().apply {
            this.type = sensorEvent.type
            this.accuracy = sensorEvent.accuracy
            this.timestamp = sensorEvent.timestamp
            this.value = sensorEvent.value
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
        if (measurementEventEntity == null || measurementEventEntity?.id != measurementId) {
            measurementEventEntity = measurementEventEntityBox.get(measurementId)
        }
        measurementEventEntity?.sensorEventEntities?.add(sensorEventEntity)
        sensorEventEntity.measurementEventEntity.target = measurementEventEntity
    }

    private fun linkMeasurementEventEntity(measurementId: Long, healthEventEntity: HealthEventEntity) {
        if (measurementEventEntity == null || measurementEventEntity?.id != measurementId) {
            measurementEventEntity = measurementEventEntityBox.get(measurementId)
        }
        measurementEventEntity?.healthEventEntities?.add(healthEventEntity)
        healthEventEntity.measurementEventEntity.target = measurementEventEntity
    }
}