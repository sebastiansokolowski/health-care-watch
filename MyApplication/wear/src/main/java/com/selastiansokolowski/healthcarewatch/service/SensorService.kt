package com.selastiansokolowski.healthcarewatch.service

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import dagger.android.DaggerService
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
class SensorService : DaggerService(), SensorEventListener {

    @Inject
    lateinit var sensorManager: SensorManager

    private val mBinder = LocalBinder()

    var measurementRunning = false

    var hearthRateChangeListener: HearthRateChangeListener? = null

    inner class LocalBinder : Binder() {
        internal val service: SensorService
            get() = this@SensorService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    lateinit var heartRateSensor: Sensor

    override fun onCreate() {
        super.onCreate()

        sensorManager.apply {
            heartRateSensor = getDefaultSensor(Sensor.TYPE_HEART_RATE)
        }
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
        sensorManager.apply {
            registerListener(this@SensorService, heartRateSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    private fun stopMeasurement() {
        sensorManager.apply {
            unregisterListener(this@SensorService)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.apply {
            val sensorValue = values[0]

            when (event.sensor?.type) {
                Sensor.TYPE_HEART_RATE -> {
                    val heartRate = Math.round(sensorValue)
                    hearthRateChangeListener?.hearthRateChangeListener(heartRate.toString())
                }
            }
        }
    }

    interface HearthRateChangeListener {
        fun hearthRateChangeListener(hearthRate: String)
    }

}