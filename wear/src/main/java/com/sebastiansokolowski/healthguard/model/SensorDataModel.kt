package com.sebastiansokolowski.healthguard.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.sebastiansokolowski.healthguard.client.WearableClient
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 18.06.19.
 */
class SensorDataModel(private val sensorManager: SensorManager, private val wearableClient: WearableClient) : SensorEventListener {
    private val TAG = javaClass.canonicalName

    var measurementId = -1L

    var heartRateObservable: ReplaySubject<com.sebastiansokolowski.shared.dataModel.SensorEvent> = ReplaySubject.createWithSize(10)
    val sensorsObservable: PublishSubject<com.sebastiansokolowski.shared.dataModel.SensorEvent> = PublishSubject.create()

    var sensorDataDisposable: Disposable? = null

    fun registerSensors(measurementId: Long, sensors: Set<Int>, samplingPeriodUs: Int) {
        this.measurementId = measurementId

        sensorDataDisposable = startSensorDataSender()

        for (sensorId: Int in sensors) {
            val sensor = sensorManager.getDefaultSensor(sensorId)

            val registered = sensorManager.registerListener(this, sensor, samplingPeriodUs)
            if (!registered) {
                Log.e(TAG, "error register sensorEvent: $sensorId")
            } else {
                Log.d(TAG, "registered sensorEvent: $sensorId")
            }
        }
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
        sensorDataDisposable?.dispose()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged sensorEvent=$sensor accuracy=$accuracy")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Schedulers.single().scheduleDirect {
            event?.apply {
                if (sensor == null || values == null || values.isEmpty()) {
                    return@apply
                }
                if (sensor.type == Sensor.TYPE_HEART_RATE && values[0] <= 0f) {
                    return@apply
                }

                val sensorEventWrapper = com.sebastiansokolowski.shared.dataModel.SensorEvent(
                        sensor.type,
                        values.copyOf(),
                        accuracy,
                        measurementId
                )

                when (sensor.type) {
                    Sensor.TYPE_HEART_RATE -> {
                        heartRateObservable.onNext(sensorEventWrapper)
                    }
                }

                sensorsObservable.onNext(sensorEventWrapper)
            }
        }
    }

    private fun startSensorDataSender(): Disposable {
        return sensorsObservable
                .subscribeOn(Schedulers.single())
                .groupBy { it.type }
                .subscribe {
                    it.buffer(1, TimeUnit.SECONDS)
                            .subscribe { events ->
                                if (events.isNullOrEmpty()) {
                                    return@subscribe
                                }
                                if (events.size <= 2) {
                                    events.forEach {
                                        wearableClient.sendSensorEvent(it)
                                    }
                                    return@subscribe
                                }
                                val firstEvent = events.first()

                                val avgValues = FloatArray(firstEvent.values.size)
                                var avgAccuracy = 0
                                events.forEach { event ->
                                    event.values.forEachIndexed { index, value ->
                                        avgValues[index] += value
                                    }
                                    avgAccuracy += event.accuracy
                                }
                                avgValues.forEachIndexed { index, value ->
                                    avgValues[index] = value / events.size
                                }
                                avgAccuracy /= events.size

                                //send
                                val sensorEventWrapper = com.sebastiansokolowski.shared.dataModel.SensorEvent(
                                        firstEvent.type,
                                        avgValues,
                                        avgAccuracy,
                                        measurementId,
                                        firstEvent.timestamp
                                )
                                wearableClient.sendSensorEvent(sensorEventWrapper)
                            }
                }
    }
}