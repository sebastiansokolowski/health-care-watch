package com.selastiansokolowski.healthcarewatch.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToInt

/**
 * Created by Sebastian Sokołowski on 18.06.19.
 */
class SensorDataModel(private val settingsModel: SettingsModel, private val wearableDataClient: WearableDataClient, private val sensorManager: SensorManager) : SensorEventListener {
    private val TAG = javaClass.canonicalName

    init {
        settingsModel.sensorDataModel = this
    }

    val heartRateObservable: PublishSubject<Int> = PublishSubject.create()
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var measurementRunning = false

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

    fun toggleMeasurementState() {
        if (measurementRunning) {
            stopMeasurement()
        } else {
            startMeasurement()
        }
    }

    private fun getAllSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }

    fun startMeasurement() {
        if (measurementRunning) {
            return
        }
        changeMeasurementState(true)

        val samplingUs = settingsModel.getSamplingUs()
        val sensors = settingsModel.getSensors()

        for (sensorId: Int in sensors) {
            val sensor = sensorManager.getDefaultSensor(sensorId)

            val registered = sensorManager.registerListener(this, sensor, samplingUs)
            if (!registered) {
                Log.d(TAG, "error register sensor: $sensorId")
            } else {
                Log.d(TAG, "error register sensor: $sensorId")
            }
        }
    }

    fun refreshSettings() {
        if (!measurementRunning) {
            return
        }
        stopMeasurement()
        startMeasurement()
    }

    fun stopMeasurement() {
        if (!measurementRunning) {
            return
        }
        changeMeasurementState(false)
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        sensor?.apply {
            wearableDataClient.sendSensorAccuracy(sensor, accuracy)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.apply {
            when (event.sensor?.type) {
                Sensor.TYPE_HEART_RATE -> {
                    val sensorValue = values[0]

                    val heartRate = sensorValue.roundToInt()
                    heartRateObservable.onNext(heartRate)
                }
            }

            wearableDataClient.sendSensorEvent(this)
        }
    }
}