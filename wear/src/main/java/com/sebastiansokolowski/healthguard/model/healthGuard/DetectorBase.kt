package com.sebastiansokolowski.healthguard.model.healthGuard

import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
abstract class DetectorBase {

    var scheduler = Schedulers.computation()

    lateinit var sensorsObservable: SensorsObservable

    fun setupDetector(sensorsObservable: SensorsObservable) {
        this.sensorsObservable = sensorsObservable
    }

    abstract fun startDetector()

    abstract fun stopDetector()
}