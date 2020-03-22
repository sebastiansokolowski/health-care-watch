package com.sebastiansokolowski.healthguard.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.sebastiansokolowski.healthguard.client.WearableDataClient
import com.sebastiansokolowski.healthguard.utils.HealthEnginesUtils
import com.sebastiansokolowski.shared.dataModel.SupportedHealthEventTypes
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Created by Sebastian Sokołowski on 18.06.19.
 */
class SensorDataModel(measurementModel: MeasurementModel, private val wearableDataClient: WearableDataClient, private val sensorManager: SensorManager, private val healthGuardModel: HealthGuardModel) : SensorEventListener {
    private val TAG = javaClass.canonicalName

    init {
        measurementModel.sensorDataModel = this
        healthGuardModel.sensorDataModel = this
    }

    val sensorsObservable: PublishSubject<com.sebastiansokolowski.shared.dataModel.SensorEvent> = PublishSubject.create()
    val heartRateObservable: PublishSubject<Int> = PublishSubject.create()
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()

    var measurementRunning = false

    private fun changeMeasurementState(state: Boolean) {
        if (measurementRunning == state) {
            return
        }
        measurementRunning = state
        measurementStateObservable.onNext(measurementRunning)
        notifyMeasurementState()
    }

    fun notifyMeasurementState() {
        wearableDataClient.sendMeasurementEvent(measurementRunning)
    }

    private fun notifySensorsObservable(sensorEvent: com.sebastiansokolowski.shared.dataModel.SensorEvent) {
        sensorsObservable.onNext(sensorEvent)
    }

    fun toggleMeasurementState() {
        if (measurementRunning) {
            stopMeasurement()
        } else {
            wearableDataClient.requestStartMeasurement()
        }
    }

    fun notifySupportedHealthEvents() {
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val supportedHealthEngines = HealthEnginesUtils.getSupportedHealthEngines(sensors)
        val supportedHealthEvents = supportedHealthEngines.map { it.getHealthEventType() }.toSet()
        wearableDataClient.sendSupportedHealthEvents(SupportedHealthEventTypes(supportedHealthEvents))
    }

    fun startMeasurement(measurementSettings: MeasurementSettings) {
        if (measurementRunning) {
            return
        }
        val healthEngines = HealthEnginesUtils.getHealthEngines(measurementSettings.healthEvents)
        val sensors = healthEngines.flatMap { it.requiredSensors() }.toSet()

        changeMeasurementState(true)

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
        event?.apply {
            if (sensor == null) {
                return@apply
            }

            when (sensor.type) {
                Sensor.TYPE_HEART_RATE -> {
                    val sensorValue = values[0]

                    val heartRate = sensorValue.roundToInt()
                    heartRateObservable.onNext(heartRate)
                }
            }

            val sensorEventWrapper = com.sebastiansokolowski.shared.dataModel.SensorEvent(
                    sensor.type,
                    values.copyOf(),
                    accuracy
            )

            notifySensorsObservable(sensorEventWrapper)

            wearableDataClient.sendSensorEvent(sensorEventWrapper)
        }
    }
}