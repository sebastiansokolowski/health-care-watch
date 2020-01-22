package com.sebastiansokolowski.healthcarewatch.model.healthCare

import com.sebastiansokolowski.shared.dataModel.HealthSensorEvent
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
abstract class DetectorBase {

    lateinit var sensorsObservable: PublishSubject<HealthSensorEvent>

    fun setupDetector(sensorsObservable: PublishSubject<HealthSensorEvent>) {
        this.sensorsObservable = sensorsObservable
    }

    abstract fun startDetector()

    abstract fun stopDetector()
}