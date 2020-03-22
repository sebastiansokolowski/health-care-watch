package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import android.util.Log
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class EpilepsyEngine : HealthGuardEngineBase() {
    val TAG = this::class.java.simpleName

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private data class AcceDataModel(val sensorEvent: SensorEvent, val acceCurrent: Double)

    override fun startEngine() {
        sensorEventSubject
                .subscribeOn(Schedulers.io())
                .filter { it.type == Sensor.TYPE_LINEAR_ACCELERATION }
                .buffer(TimeUnit.SECONDS.toMillis(measurementSettings.epilepsySettings.timeS.toLong()),
                        measurementSettings.samplingMs.toLong(),
                        TimeUnit.MILLISECONDS)
                .map {
                    val acceDataList = mutableListOf<AcceDataModel>()
                    it.forEach {
                        val acce =
                                sqrt(
                                        it.values[0].toDouble().pow(2.0) +
                                                it.values[1].toDouble().pow(2.0) +
                                                it.values[2].toDouble().pow(2.0)
                                )
                        acceDataList.add(AcceDataModel(it, acce))
                    }

                    acceDataList
                }
                .subscribe {
                    var positiveEvents = 0
                    it.forEach {
                        if (it.acceCurrent >= measurementSettings.epilepsySettings.threshold) {
                            positiveEvents++
                        }
                    }
                    val percentOfPositiveSignals = positiveEvents / it.size.toDouble()
                    val targetPercentOfPositiveSignals = measurementSettings.epilepsySettings.percentOfPositiveSignals / 100.toDouble()

                    if (percentOfPositiveSignals >= targetPercentOfPositiveSignals) {
                        Log.d(TAG, "epilepsy detected!!")
                        notifyHealthEvent(it.last().sensorEvent, percentOfPositiveSignals.toFloat(), Gson().toJson(it))
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