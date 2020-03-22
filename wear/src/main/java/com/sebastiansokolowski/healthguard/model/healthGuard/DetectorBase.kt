package com.sebastiansokolowski.healthguard.model.healthGuard

import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
abstract class DetectorBase {

    lateinit var sensorsObservable: PublishSubject<SensorEvent>

    fun setupDetector(sensorsObservable: PublishSubject<SensorEvent>) {
        this.sensorsObservable = sensorsObservable
    }

    abstract fun startDetector()

    abstract fun stopDetector()
}