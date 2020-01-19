package com.sebastiansokolowski.healthcarewatch.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.sebastiansokolowski.healthcarewatch.client.WearableDataClient
import com.sebastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthSensorEvent
import com.sebastiansokolowski.healthcarewatch.utils.HealthCareEnginesUtils
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToInt

/**
 * Created by Sebastian Sokołowski on 18.06.19.
 */
class SensorDataModel(measurementModel: MeasurementModel, private val wearableDataClient: WearableDataClient, private val sensorManager: SensorManager, private val healthCareModel: HealthCareModel) : SensorEventListener {
    private val TAG = javaClass.canonicalName

    init {
        measurementModel.sensorDataModel = this
        healthCareModel.sensorDataModel = this
    }

    val sensorsObservable: PublishSubject<HealthSensorEvent> = PublishSubject.create()
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

    private fun notifySensorsObservable(healthSensorEvent: HealthSensorEvent) {
        sensorsObservable.onNext(healthSensorEvent)
    }

    fun toggleMeasurementState() {
        if (measurementRunning) {
            stopMeasurement()
        } else {
            wearableDataClient.requestStartMeasurement()
        }
    }

    fun notifySupportedHealthCareEvents() {
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val supportedHealthCareEngines = HealthCareEnginesUtils.getSupportedHealthCareEngines(sensors)
        val supportedHealthCareEvents = supportedHealthCareEngines.map { it.getHealthCareEventType() }
        wearableDataClient.sendSupportedHealthCareEvents(supportedHealthCareEvents)
    }

    fun startMeasurement(measurementSettings: MeasurementSettings) {
        if (measurementRunning) {
            return
        }
        changeMeasurementState(true)

        healthCareModel.startEngines(measurementSettings)

        for (sensorId: Int in measurementSettings.sensors) {
            val sensor = sensorManager.getDefaultSensor(sensorId)

            val registered = sensorManager.registerListener(this, sensor, measurementSettings.samplingUs)
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

        healthCareModel.stopEngines()

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

            val sensorEventWrapper = HealthSensorEvent(
                    sensor.name ?: "empty",
                    sensor.type,
                    accuracy,
                    values.copyOf()
            )

            notifySensorsObservable(sensorEventWrapper)

            wearableDataClient.sendSensorEvent(sensorEventWrapper)
        }
    }
}