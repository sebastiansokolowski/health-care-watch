package com.sebastiansokolowski.shared.dataModel

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class HealthEvent(val healthEventType: HealthEventType, val sensorEvent: SensorEvent, val value: Float, @Transient val sensorEventsSample: List<SensorEvent>, val details: String, val measurementId: Long)