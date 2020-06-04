package com.sebastiansokolowski.healthguard.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.shared.dataModel.SupportedHealthEventTypes
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 18.06.19.
 */
class SensorDataModel(measurementModel: MeasurementModel, private val wearableClient: WearableClient, private val sensorManager: SensorManager, private val healthGuardModel: HealthGuardModel) : SensorEventListener {
    private val TAG = javaClass.canonicalName

    init {
        measurementModel.sensorDataModel = this
        healthGuardModel.sensorDataModel = this
    }

    var heartRateObservable: ReplaySubject<com.sebastiansokolowski.shared.dataModel.SensorEvent> = ReplaySubject.createWithSize(10)
    val sensorsObservable: PublishSubject<com.sebastiansokolowski.shared.dataModel.SensorEvent> = PublishSubject.create()
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()

    var measurementRunning = false
    var measurementId = -1L

    private fun changeMeasurementState(state: Boolean) {
        if (measurementRunning == state) {
            return
        }
        measurementRunning = state
        if (state) {
            heartRateObservable = ReplaySubject.createWithSize(10)
        } else {
            heartRateObservable.onComplete()
        }
        measurementStateObservable.onNext(measurementRunning)
        notifyMeasurementState()
    }

    fun notifyMeasurementState() {
        wearableClient.sendMeasurementEvent(measurementRunning)
    }

    private fun notifySensorsObservable(sensorEvent: com.sebastiansokolowski.shared.dataModel.SensorEvent) {
        sensorsObservable.onNext(sensorEvent)
    }

    fun toggleMeasurementState() {
        if (measurementRunning) {
            stopMeasurement()
        } else {
            wearableClient.requestStartMeasurement()
        }
    }

    fun notifySupportedHealthEvents() {
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val supportedHealthEvents = healthGuardModel.getSupportedHealthEvents(sensors)
        wearableClient.sendSupportedHealthEvents(SupportedHealthEventTypes(supportedHealthEvents))
    }

    fun startMeasurement(measurementSettings: MeasurementSettings) {
        if (measurementRunning) {
            return
        }

        val healthEngines = healthGuardModel.getHealthEngines(measurementSettings.healthEvents)
        val sensors = healthEngines.flatMap { it.requiredSensors() }.toSet()

        changeMeasurementState(true)

        this.measurementId = measurementSettings.measurementId
        healthGuardModel.startEngines(measurementSettings)

        for (sensorId: Int in sensors) {
            val sensor = sensorManager.getDefaultSensor(sensorId)

            val registered = sensorManager.registerListener(this, sensor,
                    TimeUnit.MILLISECONDS.toMicros(measurementSettings.samplingMs.toLong()).toInt())
            if (!registered) {
                Log.e(TAG, "error register sensorEvent: $sensorId")
            } else {
                Log.d(TAG, "registered sensorEvent: $sensorId")
            }
        }
    }

    fun stopMeasurement() {
        if (!measurementRunning) {
            return
        }
        changeMeasurementState(false)

        healthGuardModel.stopEngines()

        sensorManager.unregisterListener(this)
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

                notifySensorsObservable(sensorEventWrapper)
                wearableClient.sendSensorEvent(sensorEventWrapper)
            }
        }
    }
}