package com.selastiansokolowski.healthcarewatch.model.healthCare.engine

import android.annotation.SuppressLint
import com.selastiansokolowski.shared.db.entity.HealthCareEventType
import com.selastiansokolowski.shared.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class EpilepsyCareEngine : HealthCareEngineBase() {
    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.EPILEPSY
    }

    @SuppressLint("CheckResult")
    override fun setSensorObservable(sensorObservable: PublishSubject<SensorEventData>) {
        sensorObservable
                .subscribeOn(Schedulers.io())
                .subscribe {

                }
    }

}