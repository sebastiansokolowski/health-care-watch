package com.sebastiansokolowski.healthcarewatch.model.healthCare

import com.sebastiansokolowski.healthcarewatch.dataModel.HealthSensorEvent
import io.mockk.every
import io.mockk.mockk

/**
 * Created by Sebastian Sokołowski on 23.09.19.
 */
class SensorEventMock {
    companion object {
        fun getMockedSensorEventWrapper(type: Int, timestamp: Long = 0, values: FloatArray = FloatArray(0)): HealthSensorEvent {
            val sensorEvent = mockk<HealthSensorEvent>()

            every { sensorEvent.type } returns type
            every { sensorEvent.values } returns values
            every { sensorEvent.timestamp } returns timestamp

            return sensorEvent
        }
    }
}