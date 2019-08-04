package com.selastiansokolowski.healthcarewatch.model.healthCare.engine

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class HeartRateAnomalyEngine : HealthCareEngineBase() {

    private var anomalyState = false

    @SuppressLint("CheckResult")
    override fun setSensorEventObservable(sensorObservable: PublishSubject<SensorEvent>) {
                sensorObservable
                .subscribeOn(Schedulers.io())
                .filter { it.sensor.type == Sensor.TYPE_HEART_RATE }
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

    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.HEARTH_RATE_ANOMALY
    }

}