package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.HeartRateAnomalySettings
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

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
    private lateinit var stepDetector: StepDetector

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
        stepDetector = StepDetector(TimeUnit.MINUTES.toMillis(measurementSettings.heartRateAnomalySettings.stepDetectorTimeoutInMin.toLong()))
        stepDetector.setupDetector(sensorsObservable)
    }

    override fun startEngine() {
        stepDetector.startDetector()
        sensorEventObservable
                .subscribeOn(Schedulers.computation())
                .filter { it.type == Sensor.TYPE_HEART_RATE }
                .subscribe { sensorEventData ->
                    val heartRate = sensorEventData.values[0].toInt()

                    if (heartRate > getMaxHeartRate(measurementSettings.heartRateAnomalySettings) ||
                            heartRate < measurementSettings.heartRateAnomalySettings.minThreshold) {
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
        stepDetector?.stopDetector()

        compositeDisposable.clear()
    }


    private fun getMaxHeartRate(heartRateAnomalySettings: HeartRateAnomalySettings): Int {
        return if (stepDetector.isStepDetected()) {
            heartRateAnomalySettings.maxThresholdDuringActivity
        } else {
            heartRateAnomalySettings.maxThresholdDuringInactivity
        }
    }

}