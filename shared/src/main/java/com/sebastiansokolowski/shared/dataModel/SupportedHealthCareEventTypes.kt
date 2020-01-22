package com.sebastiansokolowski.shared.dataModel

import java.util.*

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class SupportedHealthCareEventTypes(val supportedTypes: Set<HealthCareEventType>, val timestamp: Long = Date().time)