package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.ActivityDetector
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

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var anomalyState = false
    lateinit var activityDetector: ActivityDetector

    override fun requiredSensors(): Set<Int> {
        return setOf(
                Sensor.TYPE_HEART_RATE,
                Sensor.TYPE_LINEAR_ACCELERATION
        )
    }

    override fun getHealthEventType(): HealthEventType {
        return HealthEventType.HEART_RATE_ANOMALY
    }

    override fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthEvent>, measurementSettings: MeasurementSettings) {
        super.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        activityDetector = ActivityDetector(measurementSettings.heartRateAnomalySettings.activityDetectorThreshold,
                TimeUnit.MINUTES.toMillis(measurementSettings.heartRateAnomalySettings.activityDetectorTimeoutMin.toLong()))
        activityDetector.setupDetector(sensorsObservable)
    }

    override fun startEngine() {
        activityDetector.startDetector()
        sensorEventObservable
                .subscribeOn(Schedulers.computation())
                .skip(1, TimeUnit.MINUTES)
                .filter { it.type == Sensor.TYPE_HEART_RATE }
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
        activityDetector.stopDetector()

        compositeDisposable.clear()
    }


    private fun getMaxHeartRate(heartRateAnomalySettings: HeartRateAnomalySettings): Int {
        return if (activityDetector.isActivityDetected()) {
            heartRateAnomalySettings.maxThresholdDuringActivity
        } else {
            heartRateAnomalySettings.maxThresholdDuringInactivity
        }
    }

}