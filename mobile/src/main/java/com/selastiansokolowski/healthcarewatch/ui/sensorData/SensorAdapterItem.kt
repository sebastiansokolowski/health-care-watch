package com.selastiansokolowski.healthcarewatch.ui.sensorData

import android.hardware.Sensor

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
enum class SensorAdapterItem(val title: String, val sensorId: Int) {
    HEART_RATE("HEART RATE", Sensor.TYPE_HEART_RATE),
    STEP_COUNTER("STEP COUNTER", Sensor.TYPE_STEP_COUNTER),
    PRESSURE("PRESSURE", Sensor.TYPE_PRESSURE),
    GRAVITY("GRAVITY", Sensor.TYPE_GRAVITY)
}