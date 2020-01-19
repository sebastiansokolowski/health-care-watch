package com.sebastiansokolowski.healthcarewatch.dataModel

/**
 * Created by Sebastian Sokołowski on 19.01.20.
 */
data class HealthSensorEvent(val name: String, val type: Int, val accuracy: Int, val values: FloatArray, val timestamp: Long = System.currentTimeMillis())