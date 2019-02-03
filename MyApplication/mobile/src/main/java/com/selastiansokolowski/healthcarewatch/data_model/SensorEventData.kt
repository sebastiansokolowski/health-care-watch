package com.selastiansokolowski.healthcarewatch.data_model

/**
 * Created by Sebastian Sokołowski on 17.01.19.
 */
data class SensorEventData(val sensorName: String, val accuracy: Int, val timestamp: Long, val values: FloatArray)