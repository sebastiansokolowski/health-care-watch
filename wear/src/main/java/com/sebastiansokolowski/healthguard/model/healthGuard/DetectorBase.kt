package com.sebastiansokolowski.healthguard.model.healthGuard

import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable

/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
abstract class DetectorBase {

    lateinit var sensorsObservable: SensorsObservable

    fun setupDetector(sensorsObservable: SensorsObservable) {
        this.sensorsObservable = sensorsObservable
    }

    abstract fun startDetector()

    abstract fun stopDetector()
}