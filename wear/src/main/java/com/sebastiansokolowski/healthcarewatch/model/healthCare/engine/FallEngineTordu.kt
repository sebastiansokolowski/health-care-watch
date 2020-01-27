package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import android.util.Log
import com.sebastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.sebastiansokolowski.healthcarewatch.model.healthCare.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthCareEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import com.sebastiansokolowski.shared.dataModel.HealthCareEventType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
class FallEngineTordu : HealthCareEngineBase() {
    val TAG = this::class.java.simpleName

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val stepDetector = StepDetector(10 * 1000)

    override fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthCareEvent>, measurementSettings: MeasurementSettings) {
        super.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        stepDetector.setupDetector(sensorsObservable)
    }

    private data class AcceDataModel(val sensorEvent: SensorEvent, val acceCurrent: Double)

    override fun startEngine() {
        stepDetector.startDetector()
        sensorEventSubject
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
                .buffer(6, 1)
                .subscribe {
                    var counter = 0
                    var lastDataUpperThreshold = false
                    it.forEachIndexed { index, acceDataModel ->
                        if (index % 2 == 0) {
                            if (acceDataModel.acceCurrent > measurementSettings.fallSettings.threshold) {
                                lastDataUpperThreshold = true
                            }
                        } else {
                            if (lastDataUpperThreshold && acceDataModel.acceCurrent > 7) {
                                counter++
                            }
                            lastDataUpperThreshold = false
                        }
                    }

                    if (counter in 1..13) {
                        Log.d(TAG, "fall detected")
                        it.forEach {
                            Log.d(TAG, "acceCurrent=${it.acceCurrent}")
                        }
                        Log.d(TAG, "counter=$counter")
                        Log.d(TAG, "isStepDetected=${stepDetector.isStepDetected()}")

                        if (stepDetector.isStepDetected()) {
                            notifyHealthCareEvent(it.last().sensorEvent)
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
        return HealthCareEventType.FALL_TORDU
    }

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_STEP_DETECTOR)
    }
}