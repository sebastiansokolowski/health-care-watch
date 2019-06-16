package com.selastiansokolowski.healthcarewatch.model.healthCare

import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
data class HealthCareEvent(val healthCareEventType: HealthCareEventType, val sensorEventData: SensorEventData?) {
    enum class HealthCareEventType {
        EPILEPSY,
        HEARTH_RATE_ANOMALY
    }
}
