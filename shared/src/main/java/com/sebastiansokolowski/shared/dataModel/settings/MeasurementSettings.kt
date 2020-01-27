package com.sebastiansokolowski.shared.dataModel.settings

import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class MeasurementSettings(val samplingMs: Int = 1000, val healthCareEvents: Set<String> = emptySet(), val fallSettings: FallSettings = FallSettings(), val epilepsySettings: EpilepsySettings = EpilepsySettings(), val timestamp: Long = Date().time)