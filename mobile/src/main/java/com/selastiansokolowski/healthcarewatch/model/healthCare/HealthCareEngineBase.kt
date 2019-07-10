package com.selastiansokolowski.healthcarewatch.model.healthCare

import com.selastiansokolowski.shared.db.entity.HealthCareEvent
import com.selastiansokolowski.shared.db.entity.HealthCareEventType
import com.selastiansokolowski.shared.db.entity.SensorEventData
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
abstract class HealthCareEngineBase {

    private var sensorObservable: PublishSubject<HealthCareEvent>? = null

    abstract fun setSensorObservable(sensorObservable: PublishSubject<SensorEventData>)

    abstract fun getHealthCareEventType(): HealthCareEventType

    fun setNotifyObservable(sensorObservable: PublishSubject<HealthCareEvent>) {
        this.sensorObservable = sensorObservable
    }

    fun notifyHealthCareEvent(sensorEventData: SensorEventData) {
        val healthCareEvent = HealthCareEvent().apply {
            this.careEvent = getHealthCareEventType()
            this.sensorEventData?.target = sensorEventData
        }
        sensorObservable?.onNext(healthCareEvent)
    }
}