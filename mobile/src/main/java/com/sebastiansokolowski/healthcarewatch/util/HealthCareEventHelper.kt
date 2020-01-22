package com.sebastiansokolowski.healthcarewatch.util

import android.content.Context
import com.sebastiansokolowski.healthcarewatch.R
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.shared.dataModel.HealthCareEventType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Sebastian Sokołowski on 26.06.19.
 */
class HealthCareEventHelper(val context: Context) {

    private val dtf = SimpleDateFormat("HH:mm:ss yyy-MM-dd")

    fun getTitle(healthCareEventEntity: HealthCareEventEntity): String {
        return getTitle(healthCareEventEntity.careEvent)
    }

    fun getTitle(healthCareEventType: HealthCareEventType): String {
        return when (healthCareEventType) {
            HealthCareEventType.EPILEPSY -> context.getString(R.string.health_care_event_epilepsy_title)
            HealthCareEventType.HEARTH_RATE_ANOMALY -> context.getString(R.string.health_care_event_hearth_rate_anomaly_title)
            HealthCareEventType.FALL-> context.getString(R.string.health_care_event_fall_title)
            HealthCareEventType.FALL_TORDU -> context.getString(R.string.health_care_event_fall_tordu_title)
            else -> healthCareEventType.name
        }
    }

    fun getDate(healthCareEventEntity: HealthCareEventEntity): String {
        val timestamp = healthCareEventEntity.sensorEventEntity.target?.timestamp ?: return "null"

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return dtf.format(calendar.time)
    }

    fun getEventInfo(healthCareEventEntity: HealthCareEventEntity): String {
        val value = healthCareEventEntity.sensorEventEntity.target?.values?.get(0) ?: return "null"
        return when (healthCareEventEntity.careEvent) {
            HealthCareEventType.EPILEPSY -> value.toString() + context.getString(R.string.unit_gravity)
            HealthCareEventType.HEARTH_RATE_ANOMALY -> value.toString() + context.getString(R.string.unit_hearth_rate)
            HealthCareEventType.FALL, HealthCareEventType.FALL_TORDU -> value.toString() + context.getString(R.string.unit_gravity)
            else -> "null"
        }
    }

    fun getMessage(healthCareEventEntity: HealthCareEventEntity): String {
        return when (healthCareEventEntity.careEvent) {
            HealthCareEventType.EPILEPSY -> context.getString(R.string.health_care_event_epilepsy_message)
            HealthCareEventType.HEARTH_RATE_ANOMALY -> context.getString(R.string.health_care_event_hearth_rate_anomaly_message)
            HealthCareEventType.FALL, HealthCareEventType.FALL_TORDU -> context.getString(R.string.health_care_event_fall_message)
            else -> "null"
        }
    }
}
