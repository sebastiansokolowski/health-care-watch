package com.sebastiansokolowski.healthcarewatch.model.healthCare

import android.hardware.Sensor
import android.hardware.SensorEvent
import io.mockk.every
import io.mockk.mockk

/**
 * Created by Sebastian Sokołowski on 23.09.19.
 */
class SensorEventMock {
    companion object {
        fun getMockedSensorEvent(type: Int, timestamp: Long = 0, values: FloatArray? = null): SensorEvent {
            val sensorEvent = mockk<SensorEvent>()

            val sensor = mockk<Sensor>()
            every { sensor.type } returns type

            val sensorField = sensorEvent.javaClass.getField("sensor")
            sensorField.isAccessible = true
            sensorField.set(sensorEvent, sensor)

            val timestampField = sensorEvent.javaClass.getField("timestamp")
            timestampField.isAccessible = true
            timestampField.set(sensorEvent, timestamp)

            val valuesField = sensorEvent.javaClass.getField("values")
            valuesField.isAccessible = true
            valuesField.set(sensorEvent, values)

            return sensorEvent
        }
    }
}