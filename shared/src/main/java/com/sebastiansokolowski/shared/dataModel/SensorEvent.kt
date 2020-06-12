package com.sebastiansokolowski.shared.dataModel

import java.util.*

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class SensorEvent(val type: Int, val values: FloatArray, val accuracy: Int, val measurementId: Long, val timestamp: Long = Date().time) {
    override fun toString(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return "SensorEvent(time=${calendar.time}, type=$type, values=${values.contentToString()}, accuracy=$accuracy, measurementId=$measurementId, timestamp=$timestamp)"
    }
}