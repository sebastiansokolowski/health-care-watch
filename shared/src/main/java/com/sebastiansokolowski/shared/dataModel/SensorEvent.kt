package com.sebastiansokolowski.shared.dataModel

import java.util.*

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class SensorEvent(val type: Int, val values: FloatArray, val accuracy: Int, val measurementId: Long, val timestamp: Long = Date().time)