package com.selastiansokolowski.healthcarewatch.model.healthCare

import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
abstract class HealthCareEngineBase {

    private var sensorObservable: PublishSubject<HealthCareEvent>? = null

    abstract fun setSensorObservable(sensorObservable: PublishSubject<SensorEventData>)

    abstract fun getHealthCareEventType(): HealthCareEvent.HealthCareEventType

    fun setNotifyObservable(sensorObservable: PublishSubject<HealthCareEvent>) {
        this.sensorObservable = sensorObservable
    }

    fun notifyHealthCareEvent(sensorEventData: SensorEventData) {
        val healthCareEvent = HealthCareEvent(getHealthCareEventType(), sensorEventData)
        sensorObservable?.onNext(healthCareEvent)
    }
}