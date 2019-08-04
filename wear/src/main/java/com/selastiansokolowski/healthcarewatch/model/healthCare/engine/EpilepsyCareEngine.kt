package com.selastiansokolowski.healthcarewatch.model.healthCare.engine

import android.annotation.SuppressLint
import android.hardware.SensorEvent
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class EpilepsyCareEngine : HealthCareEngineBase() {

    @SuppressLint("CheckResult")
    override fun setSensorEventObservable(sensorObservable: PublishSubject<SensorEvent>) {
        sensorObservable
                .subscribeOn(Schedulers.io())
                .subscribe {

                }
    }

    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.EPILEPSY
    }

}