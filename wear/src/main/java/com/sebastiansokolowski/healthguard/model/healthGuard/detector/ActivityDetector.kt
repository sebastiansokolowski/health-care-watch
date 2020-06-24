package com.sebastiansokolowski.healthguard.model.healthGuard.detector

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.model.healthGuard.DetectorBase
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
class ActivityDetector(private var activityThreshold: Int, private val timeout: Long) : DetectorBase() {
    val TAG = this::class.java.simpleName

    private var disposable: Disposable? = null
    private val lastActivityTimestamp: AtomicLong = AtomicLong(0)

    val activityDetectedObservable: PublishSubject<Boolean> = PublishSubject.create()

    override fun startDetector() {
        disposable = sensorsObservable
                .subscribeOn(Schedulers.computation())
                .filter { it.type == Sensor.TYPE_LINEAR_ACCELERATION }
                .map {
                    sqrt(
                            it.values[0].toDouble().pow(2.0) +
                                    it.values[1].toDouble().pow(2.0) +
                                    it.values[2].toDouble().pow(2.0)
                    )
                }.subscribe {
                    val activityDetected = it >= activityThreshold
                    if (BuildConfig.EXTRA_LOGGING) {
                        Timber.d("activityValue=$it activityDetected=$activityDetected")
                    }
                    notifyActivityState(activityDetected)
                    if (activityDetected) {
                        lastActivityTimestamp.set(getCurrentTimestamp())
                    }
                }
    }

    override fun stopDetector() {
        disposable?.dispose()
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    private fun isLastEventValid(timestamp: Long): Boolean {
        val timestampDiff = getCurrentTimestamp() - timestamp

        return timestampDiff < timeout
    }

    fun notifyActivityState(activity: Boolean) {
        activityDetectedObservable.onNext(activity)
    }

    fun isActivityDetected(): Boolean {
        lastActivityTimestamp.get().let {
            return isLastEventValid(it)
        }
    }
}