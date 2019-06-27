package com.selastiansokolowski.healthcarewatch.view

import android.content.Context
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEventType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Sebastian Sokołowski on 26.06.19.
 */
class HealthCareEventHelper(val context: Context) {

    private val dtf = SimpleDateFormat("HH:mm yyy-MM-dd")

    fun getTitle(healthCareEvent: HealthCareEvent): String {
        return when (healthCareEvent.careEvent) {
            HealthCareEventType.EPILEPSY -> "Epilepsy"
            HealthCareEventType.HEARTH_RATE_ANOMALY -> "Hearth rate anomaly"
            else -> "null"
        }
    }

    fun getDate(healthCareEvent: HealthCareEvent): String {
        val timestamp = healthCareEvent.sensorEventData.target?.timestamp ?: return "null"

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return dtf.format(calendar.time)
    }

    fun getEventInfo(healthCareEvent: HealthCareEvent): String {
        val value = healthCareEvent.sensorEventData.target?.values?.get(0) ?: return "null"
        return when (healthCareEvent.careEvent) {
            HealthCareEventType.EPILEPSY -> "$value gravity"
            HealthCareEventType.HEARTH_RATE_ANOMALY -> "$value bpm"
            else -> "null"
        }
    }

    fun getMessage(healthCareEvent: HealthCareEvent): String {
        return when (healthCareEvent.careEvent) {
            HealthCareEventType.EPILEPSY -> "Detected motion was very high."
            HealthCareEventType.HEARTH_RATE_ANOMALY -> "Detected hearth rate didn't looks normal."
            else -> "null"
        }
    }
}
