package com.selastiansokolowski.healthcarewatch.service

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import dagger.android.DaggerService
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
class SensorService : DaggerService(), SensorEventListener {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var wearableDataClient: WearableDataClient

    @Inject
    lateinit var sensorManager: SensorManager

    private val mBinder = LocalBinder()

    var measurementRunning = false

    var hearthRateChangeListener: HearthRateChangeListener? = null

    private val sensors = listOf(
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION
    )

    inner class LocalBinder : Binder() {
        internal val service: SensorService
            get() = this@SensorService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    fun toggleMeasurementState() {
        measurementRunning = !measurementRunning
        if (measurementRunning) {
            startMeasurement()
        } else {
            stopMeasurement()
        }
    }

    fun getAllSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }

    private fun startMeasurement() {
        for (sensorId: Int in sensors) {
            val sensor = sensorManager.getDefaultSensor(sensorId)
            if (sensor == null) {
                Log.d(TAG, "unavailable sensorId: $sensorId")
                wearableDataClient.sendSensorSupportedInfo(sensorId, false)
                continue
            }
            val registered = sensorManager.registerListener(this@SensorService, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            if (!registered) {
                Log.d(TAG, "error register sensor: $sensorId")
            }
            wearableDataClient.sendSensorSupportedInfo(sensorId, registered)
        }
    }

    private fun stopMeasurement() {
        sensorManager.unregisterListener(this@SensorService)
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
                    hearthRateChangeListener?.hearthRateChangeListener(heartRate.toString())
                }
            }

            wearableDataClient.sendSensorEvent(this)
        }
    }

    interface HearthRateChangeListener {
        fun hearthRateChangeListener(hearthRate: String)
    }

}