package com.sebastiansokolowski.healthcarewatch.model.healthCare

import com.sebastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthSensorEvent
import com.sebastiansokolowski.shared.dataModel.MeasurementSettings
import com.sebastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
abstract class HealthCareEngineBase {

    lateinit var healthCareEventObservable: PublishSubject<HealthCareEvent>
    lateinit var healthSensorEventSubject: PublishSubject<HealthSensorEvent>
    lateinit var measurementSettings: MeasurementSettings

    abstract fun startEngine()

    abstract fun stopEngine()

    abstract fun getHealthCareEventType(): HealthCareEventType

    abstract fun requiredSensors(): Set<Int>

    open fun setupEngine(sensorsObservable: PublishSubject<HealthSensorEvent>, notifyObservable: PublishSubject<HealthCareEvent>, measurementSettings: MeasurementSettings) {
        this.healthSensorEventSubject = sensorsObservable
        this.healthCareEventObservable = notifyObservable
        this.measurementSettings = measurementSettings
    }

    fun notifyHealthCareEvent(healthSensorEvent: HealthSensorEvent) {
        val healthCareEvent = HealthCareEvent(healthSensorEvent, getHealthCareEventType())
        healthCareEventObservable.onNext(healthCareEvent)
    }

}