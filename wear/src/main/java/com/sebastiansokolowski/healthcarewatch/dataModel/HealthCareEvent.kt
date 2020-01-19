package com.sebastiansokolowski.healthcarewatch.dataModel

import com.sebastiansokolowski.shared.healthCare.HealthCareEventType

/**
 * Created by Sebastian Sokołowski on 04.08.19.
 */
data class HealthCareEvent(val healthSensorEvent: HealthSensorEvent, val healthCareEventType: HealthCareEventType)