package com.selastiansokolowski.healthcarewatch.model.healthCare.engine

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

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_HEART_RATE)
    }

    override fun setSensorEventObservable(sensorObservable: PublishSubject<SensorEvent>) {
        sensorObservable
                .subscribeOn(Schedulers.io())
                .filter { it.sensor.type == Sensor.TYPE_HEART_RATE }
                .subscribe { sensorEventData ->
                    sensorEventData.values?.let { values ->
                        val heartRate = values[0].toInt()
                        if (heartRate > 120) {
                            if (!anomalyState) {
                                notifyHealthCareEvent(sensorEventData)
                                anomalyState = true
                            }
                        } else {
                            anomalyState = false
                        }
                    }
                }.let {
                    compositeDisposable.add(it)
                }
    }

    private fun isRunningMode(){

    }

    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.HEARTH_RATE_ANOMALY
    }

}