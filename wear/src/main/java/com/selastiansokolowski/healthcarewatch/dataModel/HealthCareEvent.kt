package com.selastiansokolowski.healthcarewatch.dataModel

import android.hardware.SensorEvent
import com.selastiansokolowski.shared.healthCare.HealthCareEventType

/**
 * Created by Sebastian Sokołowski on 04.08.19.
 */
data class HealthCareEvent(val sensorEvent: SensorEvent, val healthCareEventType: HealthCareEventType)