package com.sebastiansokolowski.shared.dataModel.settings

import com.sebastiansokolowski.shared.dataModel.HealthEventType
import java.util.*

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class MeasurementSettings(val samplingMs: Int = 1000, val healthEvents: Set<HealthEventType> = emptySet(), val fallSettings: FallSettings = FallSettings(), val epilepsySettings: EpilepsySettings = EpilepsySettings(), val timestamp: Long = Date().time)