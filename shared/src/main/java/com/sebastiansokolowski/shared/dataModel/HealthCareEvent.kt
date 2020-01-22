package com.sebastiansokolowski.shared.dataModel

import java.util.*

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class HealthCareEvent(val healthCareEventType: HealthCareEventType, val healthSensorEvent: HealthSensorEvent, val timestamp: Long = Date().time)