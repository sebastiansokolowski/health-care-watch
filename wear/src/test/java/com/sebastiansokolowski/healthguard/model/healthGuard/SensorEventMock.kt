package com.sebastiansokolowski.healthguard.model.healthGuard

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.sebastiansokolowski.shared.dataModel.SensorEvent

/**
 * Created by Sebastian Sokołowski on 23.09.19.
 */
class SensorEventMock {
    companion object {
        fun getMockedSensorEventWrapper(type: Int, timestamp: Long = 0, value: Float = 0f): SensorEvent {
            val sensorEvent = mock<SensorEvent>()

            whenever(sensorEvent.type).doReturn(type)
            whenever(sensorEvent.value).doReturn(value)
            whenever(sensorEvent.timestamp).doReturn(timestamp)

            return sensorEvent
        }
    }
}