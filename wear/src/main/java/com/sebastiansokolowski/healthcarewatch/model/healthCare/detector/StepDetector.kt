package com.sebastiansokolowski.healthcarewatch.model.healthCare.detector

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.sebastiansokolowski.healthcarewatch.model.healthCare.DetectorBase
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*


/**
 * Created by Sebastian Sokołowski on 21.09.19.
 */
class StepDetector(private var timeout: Long) : DetectorBase() {
    val TAG = this::class.java.simpleName

    private var disposable: Disposable? = null
    private var lastStepDetectorEvent: SensorEvent? = null

    override fun startDetector() {
        disposable = sensorsObservable
                .subscribeOn(Schedulers.io())
                .filter { it.sensor.type == Sensor.TYPE_STEP_DETECTOR }
                .subscribe {
                    Log.d(TAG, "isStepDetected=$it")
                    lastStepDetectorEvent = it
                }
    }

    override fun stopDetector() {
        disposable?.dispose()
    }

    fun timeout(timestamp: Long): Boolean {
        val currentTimestamp = Date().time
        val diffTimestamp = currentTimestamp - timestamp

        return diffTimestamp < timeout
    }

    fun isStepDetected(): Boolean {
        lastStepDetectorEvent?.let {
            return timeout(it.timestamp)
        }
        return false
    }
}