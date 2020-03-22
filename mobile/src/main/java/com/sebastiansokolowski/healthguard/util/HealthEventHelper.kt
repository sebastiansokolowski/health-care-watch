package com.sebastiansokolowski.healthguard.util

import android.content.Context
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Sebastian Sokołowski on 26.06.19.
 */
class HealthEventHelper(val context: Context) {

    private val dtf = SimpleDateFormat("HH:mm:ss yyy-MM-dd")

    fun getTitle(healthEventEntity: HealthEventEntity): String {
        return getTitle(healthEventEntity.event)
    }

    fun getTitle(healthEventType: HealthEventType): String {
        return when (healthEventType) {
            HealthEventType.EPILEPSY -> context.getString(R.string.health_event_epilepsy_title)
            HealthEventType.HEARTH_RATE_ANOMALY -> context.getString(R.string.health_event_hearth_rate_anomaly_title)
            HealthEventType.FALL -> context.getString(R.string.health_event_fall_title)
            HealthEventType.FALL_TORDU -> context.getString(R.string.health_event_fall_tordu_title)
            else -> healthEventType.name
        }
    }

    fun getDate(healthEventEntity: HealthEventEntity): String {
        val timestamp = healthEventEntity.sensorEventEntity.target?.timestamp ?: return "null"

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return dtf.format(calendar.time)
    }

    fun getEventInfo(healthEventEntity: HealthEventEntity): String {
        val value = Utils.format(healthEventEntity.value, 2)
        return when (healthEventEntity.event) {
            HealthEventType.EPILEPSY -> value + " " + context.getString(R.string.unit_epilepsy)
            HealthEventType.HEARTH_RATE_ANOMALY -> value + " " + context.getString(R.string.unit_hearth_rate)
            HealthEventType.FALL -> value + " " + context.getString(R.string.unit_fall)
            HealthEventType.FALL_TORDU -> value
            else -> "null"
        }
    }

    fun getMessage(healthEventEntity: HealthEventEntity): String {
        return when (healthEventEntity.event) {
            HealthEventType.EPILEPSY -> context.getString(R.string.health_event_epilepsy_message)
            HealthEventType.HEARTH_RATE_ANOMALY -> context.getString(R.string.health_event_hearth_rate_anomaly_message)
            HealthEventType.FALL, HealthEventType.FALL_TORDU -> context.getString(R.string.health_event_fall_message)
            else -> "null"
        }
    }
}
