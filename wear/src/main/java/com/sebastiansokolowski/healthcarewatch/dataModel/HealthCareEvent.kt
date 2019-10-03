package com.sebastiansokolowski.healthcarewatch.dataModel

import android.hardware.SensorEvent
import com.sebastiansokolowski.shared.healthCare.HealthCareEventType

/**
 * Created by Sebastian Sokołowski on 04.08.19.
 */
data class HealthCareEvent(val sensorEvent: SensorEvent, val healthCareEventType: HealthCareEventType)