package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import android.util.Log
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthSensorEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.sebastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.sebastiansokolowski.healthcarewatch.model.healthCare.detector.ActivityDetector
import com.sebastiansokolowski.healthcarewatch.model.healthCare.detector.StepDetector
import com.sebastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
class FallEngine : HealthCareEngineBase() {
    val TAG = this::class.java.simpleName

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var stepDetector = StepDetector(10 * 1000)

    override fun setupEngine(sensorsObservable: PublishSubject<HealthSensorEvent>, notifyObservable: PublishSubject<HealthCareEvent>, measurementSettings: MeasurementSettings) {
        super.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        stepDetector.setupDetector(sensorsObservable)
    }

    data class AcceDataModel(val healthSensorEvent: HealthSensorEvent, val acceCurrent: Double)

    private fun createActivityDetector(): ActivityDetector {
        val activityDetector = ActivityDetector(3, 5)
        activityDetector.setupDetector(healthSensorEventSubject)

        return activityDetector
    }

    override fun startEngine() {
        stepDetector.startDetector()
        healthSensorEventSubject
                .subscribeOn(Schedulers.io())
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
                .buffer(10, 1)
                .subscribe {
                    val min = it.minBy { it.acceCurrent }
                    val max = it.maxBy { it.acceCurrent }

                    if (min != null && max != null) {
                        val isFall = it.indexOf(max) > it.indexOf(min)
                        val diff = abs(max.acceCurrent - min.acceCurrent)

                        Log.d(TAG, "fallThreshold=${measurementSettings.fallThreshold} " +
                                "isStepDetected=${stepDetector.isStepDetected()} isFall=$isFall diff=$diff")
                        if (isFall &&
                                diff > measurementSettings.fallThreshold &&
                                (!measurementSettings.fallStepDetector || stepDetector.isStepDetected())) {
                            Log.d(TAG, "fall detected")
                            it.forEach {
                                Log.d(TAG, "x=${it.healthSensorEvent.values[0]} y=${it.healthSensorEvent.values[1]} z=${it.healthSensorEvent.values[2]}")
                                Log.d(TAG, "acceCurrent=${it.acceCurrent}")
                            }
                            Log.d(TAG, "min=$min max=$max isFall=$isFall diff=$diff")


                            val activityDetector = createActivityDetector()
                            activityDetector.activityStateObservable.subscribe { activity ->
                                if (!activity) {
                                    notifyHealthCareEvent(max.healthSensorEvent)
                                }
                            }.let {
                                compositeDisposable.add(it)
                            }
                            activityDetector.startDetector()
                        }
                    }
                }
                .let {
                    compositeDisposable.add(it)
                }
    }

    override fun stopEngine() {
        stepDetector.stopDetector()

        compositeDisposable.clear()
    }

    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.FALL
    }

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_STEP_DETECTOR)
    }
}