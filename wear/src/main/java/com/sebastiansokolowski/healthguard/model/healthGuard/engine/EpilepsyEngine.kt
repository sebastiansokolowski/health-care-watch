package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class EpilepsyEngine : HealthGuardEngineBase() {
    val TAG = this::class.java.simpleName

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun startEngine() {
        sensorEventObservable
                .subscribeOn(Schedulers.computation())
                .filter { it.type == Sensor.TYPE_LINEAR_ACCELERATION }
                .buffer(measurementSettings.epilepsySettings.timeS.toLong(),
                        1,
                        TimeUnit.SECONDS)
                .subscribe { events ->
                    var positiveEvents = 0
                    events.forEach { event ->
                        if (event.value >= measurementSettings.epilepsySettings.threshold) {
                            positiveEvents++
                        }
                    }
                    val percentOfPositiveSignals = positiveEvents / events.size.toDouble()
                    val targetPercentOfPositiveSignals = measurementSettings.epilepsySettings.percentOfPositiveSignals / 100.toDouble()

                    if (percentOfPositiveSignals >= targetPercentOfPositiveSignals) {
                        Timber.d("epilepsy detected!!")
                        notifyHealthEvent(events.last(), percentOfPositiveSignals.toFloat())
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