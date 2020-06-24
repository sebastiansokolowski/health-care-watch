package com.sebastiansokolowski.shared.dataModel

import java.util.*

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class SensorEvent(val type: Int, val value: Float, val accuracy: Int, val measurementId: Long, val timestamp: Long = Date().time) {
    override fun toString(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return "SensorEvent(time=${calendar.time}, type=$type, value=${value}, accuracy=$accuracy, measurementId=$measurementId, timestamp=$timestamp)"
    }
}