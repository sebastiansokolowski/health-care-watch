package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
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
    private val stepDetector = StepDetector(10 * 1000)

    override fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthCareEvent>) {
        super.setupEngine(sensorsObservable, notifyObservable)
        stepDetector.setupDetector(sensorsObservable)
    }

    data class AcceDataModel(val sensorEvent: SensorEvent, val acceCurrent: Double)

    override fun startEngine() {
        sensorEventSubject
                .subscribeOn(Schedulers.io())
                .filter { it.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION }
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
                        val isFall = it.indexOf(min) < it.indexOf(max)
                        val diff = abs(max.acceCurrent - min.acceCurrent)

                        if (isFall && diff > 17 && stepDetector.isStepDetected()) {
                            Log.d(TAG, "fall detected")
                            it.forEach {
                                Log.d(TAG, "acceCurrent=${it.acceCurrent}")
                            }
                            Log.d(TAG, "min=$min max=$max isFall=$isFall diff=$diff")

                            notifyHealthCareEvent(max.sensorEvent)
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