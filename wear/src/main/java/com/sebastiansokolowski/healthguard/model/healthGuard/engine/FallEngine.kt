package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
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
import timber.log.Timber
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
        stepDetector = StepDetector(TimeUnit.SECONDS.toMillis(measurementSettings.fallSettings.stepDetectorTimeoutS.toLong()))
        stepDetector.setupDetector(sensorsObservable)
    }

    private data class AcceDataModel(val sensorEvent: SensorEvent, val acceCurrent: Double)

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

                        Timber.d("fallThreshold=${measurementSettings.fallSettings.threshold} " +
                                "isStepDetected=${stepDetector.isStepDetected()} isFall=$isFall diff=$diff")

                        if (!isFall || diff < measurementSettings.fallSettings.threshold) {
                            return@subscribe
                        }

                        Timber.d("min=$min max=$max isFall=$isFall diff=$diff")

                        if (measurementSettings.fallSettings.stepDetector && !stepDetector.isStepDetected()) {
                            return@subscribe
                        }
                        if (measurementSettings.fallSettings.inactivityDetector) {
                            checkPostFallActivity(max.sensorEvent, diff.toFloat(), Gson().toJson("$min $max"))
                        } else {
                            Timber.d("fall detected!!")
                            notifyHealthEvent(max.sensorEvent, diff.toFloat(), it.map { it.sensorEvent }, Gson().toJson("$min $max"))
                        }
                    }
                }
                .let {
                    compositeDisposable.add(it)
                }
    }

    private fun createActivityDetector(): ActivityDetector {
        val activityDetector = ActivityDetector(measurementSettings.fallSettings.inactivityDetectorThreshold,
                TimeUnit.SECONDS.toMillis(measurementSettings.fallSettings.inactivityDetectorTimeoutS.toLong()))
        activityDetector.setupDetector(sensorEventObservable)

        return activityDetector
    }

    fun checkPostFallActivity(sensorEvent: SensorEvent, value: Float, sensorEventsToSync: List<SensorEvent>, details: String) {
        Timber.d("checkPostFallActivity")
        var postFallStateDetected = false
        var activityDetected = false
        val activityDetector = createActivityDetector()
        activityDetector.activityDetectedObservable
                .take(measurementSettings.fallSettings.inactivityDetectorTimeoutS.toLong(), TimeUnit.SECONDS)
                .doOnComplete {
                    Timber.d("checkPostFallActivity doOnComplete")
                    if (postFallStateDetected && !activityDetected) {
                        Timber.d("checkPostFallActivity fall detected!!")
                        notifyHealthEvent(sensorEvent, value, sensorEventsToSync, details)
                    }
                }
                .subscribe { activity ->
                    if (!activity && !postFallStateDetected) {
                        Timber.d("checkPostFallActivity postFallStateDetected")
                        postFallStateDetected = true
                    }
                    if (activity && postFallStateDetected && !activityDetected) {
                        Timber.d("checkPostFallActivity activityDetected")
                        activityDetected = true
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