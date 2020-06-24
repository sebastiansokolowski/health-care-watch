package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
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
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
class FallEngineAdvanced : HealthGuardEngineBase() {
    val TAG = this::class.java.simpleName

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val stepDetector = StepDetector(10 * 1000)

    override fun setupEngine(sensorsObservable: PublishSubject<SensorEvent>, notifyObservable: PublishSubject<HealthEvent>, measurementSettings: MeasurementSettings) {
        super.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        stepDetector.setupDetector(sensorsObservable)
    }

    data class AcceDataModel(val sensorEvent: SensorEvent, val acceCurrent: Double)

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
                .buffer(7000, 100, TimeUnit.MILLISECONDS)
                .subscribe { events ->
                    val firstEvent = events.first()
                    val firstPeek = events.find { it.acceCurrent >= measurementSettings.fallSettings.threshold }
                    if (firstEvent == null || firstPeek == null) {
                        return@subscribe
                    }

                    // Step 1
                    val timeToFirstPeek = firstPeek.sensorEvent.timestamp - firstEvent.sensorEvent.timestamp
                    if (timeToFirstPeek !in 2001..2100) {
                        return@subscribe
                    }
                    // Step 2
                    val fallCounter = events.count { it.acceCurrent >= measurementSettings.fallSettings.threshold }
                    if (fallCounter !in 4..50) {
                        return@subscribe
                    }
                    // Step 3
                    if (measurementSettings.fallSettings.inactivityDetector && !existPostFallInactivity(events)) {
                        return@subscribe
                    }

                    Timber.d( "fall detected fallCounter=$fallCounter")
                    val max = events.maxBy { it.acceCurrent }!!
                    notifyHealthEvent(max.sensorEvent, max.acceCurrent.toFloat(), "fallCounter=$fallCounter")
                }
                .let {
                    compositeDisposable.add(it)
                }
    }

    fun existPostFallInactivity(events: MutableList<AcceDataModel>): Boolean {
        val lastPeek = events.findLast { it.acceCurrent >= measurementSettings.fallSettings.threshold }
        val lastPeekIndex = events.indexOf(lastPeek)
        val postFallEvents = events.subList(lastPeekIndex, events.lastIndex)
        val size = TimeUnit.SECONDS.toMillis(measurementSettings.fallSettings.inactivityDetectorTimeoutS.toLong()).toInt() / measurementSettings.samplingMs
        postFallEvents.windowed(size, 1).forEach {
            val activity = it.find { it.acceCurrent >= measurementSettings.fallSettings.inactivityDetectorThreshold }
            if (activity == null) {
                return true
            }
        }
        return false
    }

    override fun stopEngine() {
        stepDetector.stopDetector()

        compositeDisposable.clear()
    }

    override fun getHealthEventType(): HealthEventType {
        return HealthEventType.FALL_ADVANCED
    }

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_STEP_DETECTOR)
    }
}