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

    override fun startEngine() {
        stepDetector.startDetector()
        sensorEventObservable
                .subscribeOn(Schedulers.computation())
                .filter { it.type == Sensor.TYPE_LINEAR_ACCELERATION }
                .buffer(7000, 100, TimeUnit.MILLISECONDS)
                .subscribe { events ->
                    val firstEvent = events.first()
                    val firstPeek = events.find { it.value >= measurementSettings.fallSettings.threshold }
                    if (firstEvent == null || firstPeek == null) {
                        return@subscribe
                    }

                    // Step 1
                    val timeToFirstPeek = firstPeek.timestamp - firstEvent.timestamp
                    if (timeToFirstPeek !in 2001..2100) {
                        return@subscribe
                    }
                    // Step 2
                    val fallCounter = events.count { it.value >= measurementSettings.fallSettings.threshold }
                    if (fallCounter !in 4..50) {
                        return@subscribe
                    }
                    // Step 3
                    if (measurementSettings.fallSettings.inactivityDetector && !existPostFallInactivity(events)) {
                        return@subscribe
                    }

                    Timber.d("fall detected fallCounter=$fallCounter")
                    val max = events.maxBy { it.value }!!
                    notifyHealthEvent(max, max.value, events, "fallCounter=$fallCounter")
                }
                .let {
                    compositeDisposable.add(it)
                }
    }

    fun existPostFallInactivity(events: MutableList<SensorEvent>): Boolean {
        val lastPeek = events.findLast { it.value >= measurementSettings.fallSettings.threshold }
        val lastPeekIndex = events.indexOf(lastPeek)
        val postFallEvents = events.subList(lastPeekIndex, events.lastIndex)
        val size = TimeUnit.SECONDS.toMillis(measurementSettings.fallSettings.inactivityDetectorTimeoutS.toLong()).toInt() / measurementSettings.samplingMs
        postFallEvents.windowed(size, 1).forEach {
            val activity = it.find { it.value >= measurementSettings.fallSettings.inactivityDetectorThreshold }
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