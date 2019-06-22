package com.selastiansokolowski.healthcarewatch.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 18.06.19.
 */
class SensorDataModel(private val wearableDataClient: WearableDataClient, private val sensorManager: SensorManager) : SensorEventListener {
    private val TAG = javaClass.canonicalName

    private val sensors = listOf(
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION
    )

    val heartRateObservable: PublishSubject<Int> = PublishSubject.create()
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var measurementRunning = false

    private fun changeMeasurementState(state: Boolean){
        if(measurementRunning == state){
            return
        }
        measurementRunning = state
        measurementStateObservable.onNext(measurementRunning)
        Thread(Runnable {
            wearableDataClient.sendMeasurementEvent(state)
        }).start()
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
        for (sensorId: Int in sensors) {
            val sensor = sensorManager.getDefaultSensor(sensorId)
            if (sensor == null) {
                Log.d(TAG, "unavailable sensorId: $sensorId")
                wearableDataClient.sendSensorSupportedInfo(sensorId, false)
                continue
            }

            val registered = sensorManager.registerListener(this, sensor, TimeUnit.SECONDS.toMicros(1).toInt())
            if (!registered) {
                Log.d(TAG, "error register sensor: $sensorId")
            }
            wearableDataClient.sendSensorSupportedInfo(sensorId, registered)
        }
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

                    val heartRate = Math.round(sensorValue)
                    heartRateObservable.onNext(heartRate)
                }
            }

            wearableDataClient.sendSensorEvent(this)
        }
    }
}