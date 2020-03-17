package com.sebastiansokolowski.healthguard.model.healthCare

import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.mockk.every
import io.mockk.mockk

/**
 * Created by Sebastian Sokołowski on 23.09.19.
 */
class SensorEventMock {
    companion object {
        fun getMockedSensorEventWrapper(type: Int, timestamp: Long = 0, values: FloatArray = FloatArray(0)): SensorEvent {
            val sensorEvent = mockk<SensorEvent>()

            every { sensorEvent.type } returns type
            every { sensorEvent.values } returns values
            every { sensorEvent.timestamp } returns timestamp

            return sensorEvent
        }
    }
}