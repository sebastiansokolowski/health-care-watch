package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
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

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var anomalyState = false
    lateinit var stepDetector: StepDetector

    override fun requiredSensors(): Set<Int> {
        return setOf(
                Sensor.TYPE_HEART_RATE,
                Sensor.TYPE_STEP_DETECTOR
        )
    }

    override fun getHealthEventType(): HealthEventType {
        return HealthEventType.HEART_RATE_ANOMALY
    }

    override fun setupEngine(sensorsObservable: SensorsObservable, notifyObservable: PublishSubject<HealthEvent>, measurementSettings: MeasurementSettings) {
        super.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        stepDetector = StepDetector(TimeUnit.MINUTES.toMillis(measurementSettings.heartRateAnomalySettings.activityDetectorTimeoutMin.toLong()))
        stepDetector.setupDetector(sensorsObservable)
    }

    override fun startEngine() {
        stepDetector.startDetector()
        sensorsObservable.heartRateObservable
                .subscribeOn(Schedulers.computation())
                .skip(1, TimeUnit.MINUTES)
                .subscribe { sensorEventData ->
                    val heartRate = sensorEventData.value.toInt()

                    if (heartRate > getMaxHeartRate(measurementSettings.heartRateAnomalySettings) ||
                            heartRate < measurementSettings.heartRateAnomalySettings.minThreshold) {
                        if (!anomalyState) {
                            notifyHealthEvent(sensorEventData, heartRate.toFloat(), details = Gson().toJson(sensorEventData))
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


    private fun getMaxHeartRate(heartRateAnomalySettings: HeartRateAnomalySettings): Int {
        return if (stepDetector.isStepDetected()) {
            heartRateAnomalySettings.maxThresholdDuringActivity
        } else {
            heartRateAnomalySettings.maxThresholdDuringInactivity
        }
    }

}