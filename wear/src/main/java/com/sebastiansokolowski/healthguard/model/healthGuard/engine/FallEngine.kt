package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import android.util.Log
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.ActivityDetector
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
class FallEngine : HealthGuardEngineBase() {
    val TAG = this::class.java.simpleName

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    lateinit var stepDetector: StepDetector

    override fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthEvent>, measurementSettings: MeasurementSettings) {
        super.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        stepDetector = StepDetector(TimeUnit.SECONDS.toMillis(measurementSettings.fallSettings.stepDetectorTimeoutInS.toLong()))
        stepDetector.setupDetector(sensorsObservable)
    }

    private data class AcceDataModel(val sensorEvent: SensorEvent, val acceCurrent: Double)

    private fun createActivityDetector(): ActivityDetector {
        val activityDetector = ActivityDetector(measurementSettings.fallSettings.activityThreshold, measurementSettings.fallSettings.timeOfInactivity.toLong())
        activityDetector.setupDetector(sensorEventObservable)

        return activityDetector
    }

    override fun startEngine() {
        stepDetector.startDetector()
        sensorEventObservable
                .subscribeOn(Schedulers.computation())
                .filter { it.type == Sensor.TYPE_LINEAR_ACCELERATION }
                .map {
                    AcceDataModel(
                            it,
                            sqrt(
                                    it.values[0].toDouble().pow(2.0) +
                                            it.values[1].toDouble().pow(2.0) +
                                            it.values[2].toDouble().pow(2.0)
                            )
                    )
                }
                .buffer(measurementSettings.fallSettings.sampleCount, 1)
                .subscribe {
                    val min = it.minBy { it.acceCurrent }
                    val max = it.maxBy { it.acceCurrent }

                    if (min != null && max != null) {
                        val isFall = it.indexOf(max) > it.indexOf(min)
                        val diff = abs(max.acceCurrent - min.acceCurrent)

                        Log.d(TAG, "fallThreshold=${measurementSettings.fallSettings.threshold} " +
                                "isStepDetected=${stepDetector.isStepDetected()} isFall=$isFall diff=$diff")
                        if (isFall &&
                                diff > measurementSettings.fallSettings.threshold &&
                                (!measurementSettings.fallSettings.stepDetector || stepDetector.isStepDetected())) {
                            it.forEach {
                                Log.d(TAG, "x=${it.sensorEvent.values[0]} y=${it.sensorEvent.values[1]} z=${it.sensorEvent.values[2]}")
                                Log.d(TAG, "acceCurrent=${it.acceCurrent}")
                            }
                            Log.d(TAG, "min=$min max=$max isFall=$isFall diff=$diff")

                            if (measurementSettings.fallSettings.timeOfInactivity > 0) {
                                checkPostFallActivity(max.sensorEvent, diff.toFloat(), Gson().toJson(it))
                            } else {
                                Log.d(TAG, "fall detected!!")
                                notifyHealthEvent(max.sensorEvent, diff.toFloat(), Gson().toJson(it))
                            }
                        }
                    }
                }
                .let {
                    compositeDisposable.add(it)
                }
    }

    fun checkPostFallActivity(sensorEvent: SensorEvent, value: Float, details: String) {
        Log.d(TAG, "checkPostFallActivity")
        val activityDetector = createActivityDetector()
        activityDetector.activityStateObservable.subscribe { activity ->
            if (!activity) {
                Log.d(TAG, "fall detected!!")
                notifyHealthEvent(sensorEvent, value, details)
            }
        }.let {
            compositeDisposable.add(it)
        }
        activityDetector.startDetector()
    }

    override fun stopEngine() {
        stepDetector.stopDetector()

        compositeDisposable.clear()
    }

    override fun getHealthEventType(): HealthEventType {
        return HealthEventType.FALL
    }

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_STEP_DETECTOR)
    }
}