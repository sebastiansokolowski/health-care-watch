package com.sebastiansokolowski.healthguard.ui.sensorData

import android.hardware.Sensor

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
enum class SensorAdapterItem(val sensorId: Int) {
    HEART_RATE(Sensor.TYPE_HEART_RATE),
    STEP_COUNTER(Sensor.TYPE_STEP_COUNTER),
    ACCELERATION_VECTOR(Sensor.TYPE_LINEAR_ACCELERATION)
}