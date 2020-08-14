package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.lang.Math.abs
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class EpilepsyEngine : HealthGuardEngineBase() {
    val TAG = this::class.java.simpleName

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val lastDetected = AtomicBoolean(false)

    override fun startEngine() {
        sensorsObservable.linearAccelerationObservable
                .subscribeOn(scheduler)
                .buffer(measurementSettings.epilepsySettings.samplingTimeS.toLong(),
                        1,
                        TimeUnit.SECONDS,
                        scheduler)
                .subscribe { events ->
                    if (events.isNullOrEmpty()) {
                        return@subscribe
                    }
                    var motions = 0
                    var lastEvent = events.first()
                    var decreasing = false
                    var diff = 0f
                    events.forEach { event ->
                        if (event.value > lastEvent.value && decreasing ||
                                event.value < lastEvent.value && !decreasing) {
                            decreasing = !decreasing
                            if (diff >= measurementSettings.epilepsySettings.threshold) {
                                motions++
                            }
                            diff = 0f
                        }

                        diff += abs(lastEvent.value - event.value)
                        lastEvent = event
                    }
                    if (motions >= measurementSettings.epilepsySettings.motionsToDetect && !lastDetected.get()) {
                        Timber.d("epilepsy detected!!")
                        lastDetected.set(true)
                        notifyHealthEvent(events.last(), motions.toFloat(), events)
                    } else if (motions < measurementSettings.epilepsySettings.motionsToCancel) {
                        lastDetected.set(false)
                    }
                }
                .let {
                    compositeDisposable.add(it)
                }
    }

    override fun stopEngine() {
        compositeDisposable.clear()
    }

    override fun getHealthEventType(): HealthEventType {
        return HealthEventType.EPILEPSY
    }

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_LINEAR_ACCELERATION)
    }
}