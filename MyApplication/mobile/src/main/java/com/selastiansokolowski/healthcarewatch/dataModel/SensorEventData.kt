package com.selastiansokolowski.healthcarewatch.dataModel

/**
 * Created by Sebastian Sokołowski on 17.01.19.
 */
data class SensorEventData(val type: Int, val accuracy: Int, val timestamp: Long, val values: FloatArray)