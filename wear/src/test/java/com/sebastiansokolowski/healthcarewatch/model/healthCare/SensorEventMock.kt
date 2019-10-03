package com.sebastiansokolowski.healthcarewatch.model.healthCare

import android.hardware.Sensor
import android.hardware.SensorEvent
import org.mockito.Mockito

/**
 * Created by Sebastian Sokołowski on 23.09.19.
 */
class SensorEventMock {
    companion object {
        fun getMockedSensorEvent(type: Int, timestamp: Long = 0): SensorEvent {
            val sensorEvent = Mockito.mock(SensorEvent::class.java)

            val sensor = Mockito.mock(Sensor::class.java)
            Mockito.`when`(sensor.type).thenReturn(type)

            val sensorField = sensorEvent.javaClass.getField("sensor")
            sensorField.isAccessible = true
            sensorField.set(sensorEvent, sensor)

            val timestampField = sensorEvent.javaClass.getField("timestamp")
            timestampField.isAccessible = true
            timestampField.set(sensorEvent, timestamp)

            return sensorEvent
        }
    }
}