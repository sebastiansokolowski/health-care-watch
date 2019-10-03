package com.selastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.selastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.selastiansokolowski.healthcarewatch.model.healthCare.detector.StepDetector
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
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
    private val stepDetector = StepDetector(5 * 1000)

    override fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthCareEvent>) {
        super.setupEngine(sensorsObservable, notifyObservable)
        stepDetector.setupDetector(sensorsObservable)
    }

    data class AcceDataModel(val sensorEvent: SensorEvent, val acceCurrent: Double)

    override fun startEngine() {
        sensorEventSubject
                .subscribeOn(Schedulers.io())
                .filter { it.sensor.type == Sensor.TYPE_ACCELEROMETER }
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
                .buffer(10)
                .subscribe {
                    val min = it.maxBy { it.acceCurrent }
                    val max = it.minBy { it.acceCurrent }

                    if (min != null && max != null) {
                        val isFall = it.indexOf(min) < it.indexOf(max)
                        val diff = abs(min.acceCurrent - max.acceCurrent)

                        Log.d(TAG, "min=$min max=$max isFall=$isFall dif=$diff")
                        if (isFall && diff > 8) {
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
        return setOf(Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_STEP_DETECTOR)
    }
}