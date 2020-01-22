package com.sebastiansokolowski.shared.dataModel

import java.util.*

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class HealthCareEvent(val healthCareEventType: HealthCareEventType, val sensorEvent: SensorEvent, val timestamp: Long = Date().time)