package com.sebastiansokolowski.healthguard.model.healthGuard

import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
abstract class HealthGuardEngineBase {

    lateinit var healthEventObservable: PublishSubject<HealthEvent>
    lateinit var sensorEventObservable: PublishSubject<SensorEvent>
    lateinit var measurementSettings: MeasurementSettings

    abstract fun startEngine()

    abstract fun stopEngine()

    abstract fun getHealthEventType(): HealthEventType

    abstract fun requiredSensors(): Set<Int>

    open fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthEvent>, measurementSettings: MeasurementSettings) {
        this.sensorEventObservable = sensorsObservable
        this.healthEventObservable = notifyObservable
        this.measurementSettings = measurementSettings
    }

    fun notifyHealthEvent(sensorEvent: SensorEvent, value: Float, details: String = "") {
        val healthEvent = HealthEvent(getHealthEventType(), sensorEvent, value, details, measurementSettings.measurementId)
        healthEventObservable.onNext(healthEvent)
    }

}