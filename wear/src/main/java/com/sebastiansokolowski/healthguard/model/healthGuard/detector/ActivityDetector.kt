package com.sebastiansokolowski.healthguard.model.healthGuard.detector

import android.hardware.Sensor
import android.util.Log
import com.sebastiansokolowski.healthguard.model.healthGuard.DetectorBase
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
class ActivityDetector(private var activityThreshold: Int, private var bufferTime: Long) : DetectorBase() {
    val TAG = this::class.java.simpleName

    private var disposable: Disposable? = null

    val activityStateObservable: PublishSubject<Boolean> = PublishSubject.create()

    override fun startDetector() {
        disposable = sensorsObservable
                .subscribeOn(Schedulers.computation())
                .filter { it.type == Sensor.TYPE_LINEAR_ACCELERATION }
                .buffer(bufferTime, TimeUnit.SECONDS)
                .take(1)
                .map {
                    var sum = 0.0
                    var count = 0
                    it.forEach {
                        sum +=
                                sqrt(
                                        it.values[0].toDouble().pow(2.0) +
                                                it.values[1].toDouble().pow(2.0) +
                                                it.values[2].toDouble().pow(2.0)
                                )
                        count++
                    }
                    sum / count
                }.subscribe {
                    Log.d(TAG, "avg=$it")
                    notifyActivityState(it >= activityThreshold)
                }
    }

    fun notifyActivityState(activity: Boolean) {
        Log.d(TAG, "activity=$activity")
        activityStateObservable.onNext(activity)
    }

    override fun stopDetector() {
        disposable?.dispose()
    }

}