package com.sebastiansokolowski.shared.dataModel

import java.util.*

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class SupportedHealthEventTypes(val supportedTypes: Set<HealthEventType>, val timestamp: Long = Date().time)