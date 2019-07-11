package com.selastiansokolowski.healthcarewatch.model.healthCare.engine

import android.annotation.SuppressLint
import android.hardware.Sensor
import com.selastiansokolowski.shared.db.entity.HealthCareEventType
import com.selastiansokolowski.shared.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class HeartRateAnomalyEngine : HealthCareEngineBase() {

    private var anomalyState = false

    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.HEARTH_RATE_ANOMALY
    }

    @SuppressLint("CheckResult")
    override fun setSensorObservable(sensorObservable: PublishSubject<SensorEventData>) {
        sensorObservable
                .subscribeOn(Schedulers.io())
                .filter { it.type == Sensor.TYPE_HEART_RATE }
                .subscribe { sensorEventData ->
                    sensorEventData.values?.let { values ->
                        val heartRate = values[0].toInt()
                        if (heartRate > 90) {
                            if (!anomalyState) {
                                notifyHealthCareEvent(sensorEventData)
                                anomalyState = true
                            }
                        } else {
                            anomalyState = false
                        }
                    }
                }
    }

}