package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthSensorEvent
import com.sebastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.sebastiansokolowski.healthcarewatch.model.healthCare.detector.StepDetector
import com.sebastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class HeartRateAnomalyEngine : HealthCareEngineBase() {

    /*
        running mode is off
        heart rate >120 || <50 ~10 sec

     */
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var anomalyState = false
    var stepDetector = StepDetector(5 * 1000)

    override fun requiredSensors(): Set<Int> {
        return setOf(
                Sensor.TYPE_HEART_RATE,
                Sensor.TYPE_STEP_DETECTOR
        )
    }

    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.HEARTH_RATE_ANOMALY
    }

    override fun setupEngine(sensorsObservable: PublishSubject<HealthSensorEvent>, notifyObservable: PublishSubject<HealthCareEvent>, measurementSettings: MeasurementSettings) {
        super.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        stepDetector.setupDetector(sensorsObservable)
    }

    override fun startEngine() {
        stepDetector.startDetector()

        healthSensorEventSubject
                .subscribeOn(Schedulers.io())
                .filter { it.type == Sensor.TYPE_HEART_RATE }
                .subscribe { sensorEventData ->
                    val heartRate = sensorEventData.values[0].toInt()

                    if (heartRate > getMaxHeartRate() || heartRate < getMinHeartRate()) {
                        if (!anomalyState) {
                            notifyHealthCareEvent(sensorEventData)
                            anomalyState = true
                        }
                    } else {
                        anomalyState = false
                    }
                }.let {
                    compositeDisposable.add(it)
                }
    }

    override fun stopEngine() {
        stepDetector.stopDetector()

        compositeDisposable.clear()
    }


    private fun getMaxHeartRate(): Int {
        return if (stepDetector.isStepDetected()) {
            150
        } else {
            120
        }
    }

    private fun getMinHeartRate(): Int {
        return 40
    }
}