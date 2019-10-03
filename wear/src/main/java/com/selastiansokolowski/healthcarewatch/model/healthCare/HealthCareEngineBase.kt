package com.selastiansokolowski.healthcarewatch.model.healthCare

import android.hardware.SensorEvent
import com.selastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
abstract class HealthCareEngineBase {

    lateinit var healthCareEventObservable: PublishSubject<HealthCareEvent>
    lateinit var sensorEventSubject: PublishSubject<SensorEvent>

    abstract fun startEngine()

    abstract fun stopEngine()

    abstract fun getHealthCareEventType(): HealthCareEventType

    abstract fun requiredSensors(): Set<Int>

    open fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthCareEvent>) {
        this.sensorEventSubject = sensorsObservable
        this.healthCareEventObservable = notifyObservable
    }

    fun notifyHealthCareEvent(sensorEvent: SensorEvent) {
        val healthCareEvent = HealthCareEvent(sensorEvent, getHealthCareEventType())
        healthCareEventObservable.onNext(healthCareEvent)
    }

}