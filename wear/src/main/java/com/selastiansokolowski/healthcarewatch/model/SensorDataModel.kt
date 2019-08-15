package com.selastiansokolowski.healthcarewatch.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.utils.HealthCareEnginesUtils
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

    val sensorsObservable: PublishSubject<SensorEvent> = PublishSubject.create()
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

    private fun notifySensorsObservable(sensorEvent: SensorEvent) {
        sensorsObservable.onNext(sensorEvent)
    }

    fun toggleMeasurementState() {
        if (measurementRunning) {
            stopMeasurement()
        } else {
            startMeasurement()
        }
    }

    fun notifySupportedHealthCareEvents() {
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val supportedHealthCareEngines = HealthCareEnginesUtils.getSupportedHealthCareEngines(sensors)
        val supportedHealthCareEvents = supportedHealthCareEngines.map { it.getHealthCareEventType() }
        wearableDataClient.sendSupportedHealthCareEvents(supportedHealthCareEvents)
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
                Log.e(TAG, "error register sensorEvent: $sensorId")
            } else {
                Log.d(TAG, "registered sensorEvent: $sensorId")
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
        Log.d(TAG, "onAccuracyChanged sensorEvent=$sensor accuracy=$accuracy")
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

            notifySensorsObservable(event)

            wearableDataClient.sendSensorEvent(this)
        }
    }
}