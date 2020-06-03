package com.sebastiansokolowski.healthguard.model.healthGuard.detector

import android.hardware.Sensor
import android.util.Log
import com.sebastiansokolowski.healthguard.model.healthGuard.DetectorBase
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
class StepDetector(private var timeout: Long) : DetectorBase() {
    val TAG = this::class.java.simpleName

    private var disposable: Disposable? = null
    private var lastEventTimestamp: Long? = null

    override fun startDetector() {
        disposable = sensorsObservable
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .filter { it.type == Sensor.TYPE_STEP_DETECTOR }
                .subscribe {
                    Log.d(TAG, "isStepDetected=$it")
                    lastEventTimestamp = getCurrentTimestamp()
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

    fun isStepDetected(): Boolean {
        lastEventTimestamp?.let {
            return isLastEventValid(it)
        }
        return false
    }
}