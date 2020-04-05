package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class HeartRateAnomalyEngine : HealthGuardEngineBase() {

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

    override fun getHealthEventType(): HealthEventType {
        return HealthEventType.HEARTH_RATE_ANOMALY
    }

    override fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthEvent>, measurementSettings: MeasurementSettings) {
        super.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        stepDetector.setupDetector(sensorsObservable)
    }

    override fun startEngine() {
        stepDetector.startDetector()

        sensorEventObservable
                .subscribeOn(Schedulers.io())
                .filter { it.type == Sensor.TYPE_HEART_RATE }
                .subscribe { sensorEventData ->
                    val heartRate = sensorEventData.values[0].toInt()

                    if (heartRate > getMaxHeartRate() || heartRate < getMinHeartRate()) {
                        if (!anomalyState) {
                            notifyHealthEvent(sensorEventData, heartRate.toFloat(), Gson().toJson(sensorEventData))
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