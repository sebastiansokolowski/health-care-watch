package com.sebastiansokolowski.healthguard.model.healthGuard.detector

import com.sebastiansokolowski.healthguard.model.healthGuard.DetectorBase
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong


/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
class StepDetector(private val timeoutMillis: Long) : DetectorBase() {
    val TAG = this::class.java.simpleName

    private var disposable: Disposable? = null
    private var lastEventTimestamp: AtomicLong = AtomicLong(0)

    override fun startDetector() {
        disposable = sensorsObservable.stepDetectorObservable
                .subscribeOn(scheduler)
                .subscribe {
                    Timber.d("isStepDetected=$it")
                    lastEventTimestamp.set(getCurrentTimestamp())
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

        return timestampDiff < timeoutMillis
    }

    fun isStepDetected(): Boolean {
        lastEventTimestamp.get().let {
            return isLastEventValid(it)
        }
    }
}