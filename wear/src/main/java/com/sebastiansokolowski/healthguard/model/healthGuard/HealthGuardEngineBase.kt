package com.sebastiansokolowski.healthguard.model.healthGuard

import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
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
    lateinit var sensorsObservable: SensorsObservable
    lateinit var measurementSettings: MeasurementSettings

    abstract fun startEngine()

    abstract fun stopEngine()

    abstract fun getHealthEventType(): HealthEventType

    abstract fun requiredSensors(): Set<Int>

    open fun setupEngine(sensorsObservable: SensorsObservable, notifyObservable: PublishSubject<HealthEvent>, measurementSettings: MeasurementSettings) {
        this.sensorsObservable = sensorsObservable
        this.healthEventObservable = notifyObservable
        this.measurementSettings = measurementSettings
    }

    fun notifyHealthEvent(sensorEvent: SensorEvent, value: Float, sensorEventsSample: List<SensorEvent> = mutableListOf(), details: String = "") {
        val healthEvent = HealthEvent(getHealthEventType(), sensorEvent, value, sensorEventsSample, details, measurementSettings.measurementId)
        healthEventObservable.onNext(healthEvent)
    }

}