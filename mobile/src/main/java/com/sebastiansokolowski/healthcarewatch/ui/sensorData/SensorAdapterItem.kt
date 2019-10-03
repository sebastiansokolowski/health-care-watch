package com.sebastiansokolowski.healthcarewatch.ui.sensorData

import android.hardware.Sensor

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
enum class SensorAdapterItem(val sensorId: Int) {
    HEART_RATE(Sensor.TYPE_HEART_RATE),
    STEP_COUNTER(Sensor.TYPE_STEP_COUNTER),
    GRAVITY(Sensor.TYPE_GRAVITY),
    LINEAR_ACCELERATION(Sensor.TYPE_LINEAR_ACCELERATION)
}