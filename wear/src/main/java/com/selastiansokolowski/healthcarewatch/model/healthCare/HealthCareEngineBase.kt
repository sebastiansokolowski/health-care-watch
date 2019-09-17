package com.selastiansokolowski.healthcarewatch.model.healthCare

import android.hardware.SensorEvent
import com.selastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
abstract class HealthCareEngineBase {

    private var healthCareEventObservable: PublishSubject<HealthCareEvent>? = null

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    abstract fun setSensorEventObservable(sensorObservable: PublishSubject<SensorEvent>)

    abstract fun getHealthCareEventType(): HealthCareEventType

    abstract fun requiredSensors(): Set<Int>

    fun setNotifyObservable(sensorObservable: PublishSubject<HealthCareEvent>) {
        this.healthCareEventObservable = sensorObservable
    }

    fun notifyHealthCareEvent(sensorEvent: SensorEvent) {
        val healthCareEvent = HealthCareEvent(sensorEvent, getHealthCareEventType())
        healthCareEventObservable?.onNext(healthCareEvent)
    }

}