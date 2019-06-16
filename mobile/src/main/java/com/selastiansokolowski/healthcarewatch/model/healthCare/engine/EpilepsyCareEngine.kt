package com.selastiansokolowski.healthcarewatch.model.healthCare.engine

import android.annotation.SuppressLint
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEvent
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class EpilepsyCareEngine : HealthCareEngineBase() {
    override fun getHealthCareEventType(): HealthCareEvent.HealthCareEventType {
        return HealthCareEvent.HealthCareEventType.EPILEPSY
    }

    @SuppressLint("CheckResult")
    override fun setSensorObservable(sensorObservable: PublishSubject<SensorEventData>) {
        sensorObservable
                .subscribeOn(Schedulers.io())
                .subscribe {

                }
    }

}