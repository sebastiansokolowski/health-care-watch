package com.sebastiansokolowski.healthcarewatch.model.healthCare

import com.sebastiansokolowski.shared.dataModel.HealthCareEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import com.sebastiansokolowski.shared.dataModel.HealthCareEventType
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
abstract class HealthCareEngineBase {

    lateinit var healthCareEventObservable: PublishSubject<HealthCareEvent>
    lateinit var sensorEventSubject: PublishSubject<SensorEvent>
    lateinit var measurementSettings: MeasurementSettings

    abstract fun startEngine()

    abstract fun stopEngine()

    abstract fun getHealthCareEventType(): HealthCareEventType

    abstract fun requiredSensors(): Set<Int>

    open fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthCareEvent>, measurementSettings: MeasurementSettings) {
        this.sensorEventSubject = sensorsObservable
        this.healthCareEventObservable = notifyObservable
        this.measurementSettings = measurementSettings
    }

    fun notifyHealthCareEvent(sensorEvent: SensorEvent) {
        val healthCareEvent = HealthCareEvent(getHealthCareEventType(), sensorEvent)
        healthCareEventObservable.onNext(healthCareEvent)
    }

}