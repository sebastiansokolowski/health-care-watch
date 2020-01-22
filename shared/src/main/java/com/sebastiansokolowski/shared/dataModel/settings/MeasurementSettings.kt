package com.sebastiansokolowski.shared.dataModel.settings

import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class MeasurementSettings(val samplingUs: Int = 1000, val healthCareEvents: ArrayList<String> = ArrayList(), val fallSettings: FallSettings = FallSettings(), val timestamp: Long = Date().time)