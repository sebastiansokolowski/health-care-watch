package com.sebastiansokolowski.healthguard.util

import android.content.Context
import android.location.Location
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.util.Utils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Sebastian Sokołowski on 26.06.19.
 */
class HealthEventHelper(val context: Context) {

    private val dtf = SimpleDateFormat("HH:mm:ss yyy-MM-dd", Locale.getDefault())

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
            HealthEventType.EPILEPSY -> value + " " + context.getString(R.string.unit_percentage)
            HealthEventType.HEARTH_RATE_ANOMALY -> value + " " + context.getString(R.string.unit_hearth_rate)
            HealthEventType.FALL -> value + " " + context.getString(R.string.unit_linear_acceleration)
            HealthEventType.FALL_TORDU -> value + " " + context.getString(R.string.unit_linear_acceleration)
            else -> "null"
        }
    }

    fun getNotificationMessage(healthEventEntity: HealthEventEntity): String {
        return getTitle(healthEventEntity) + "\n" + getEventInfo(healthEventEntity) + "\n" + getMessage(healthEventEntity)
    }

    fun getLocationMessage(location: Location?): String {
        var message = context.getString(R.string.sms_message_user_location)
        if (location == null) {
            message += " " + context.getString(R.string.sms_message_unknow_location)
        } else {
            message += " " + context.getString(R.string.sms_message_google_map_url, getFormattedLocation(location))
            if (location.hasAccuracy()) {
                message += ", " + context.getString(R.string.sms_message_location_accuracy, location.accuracy.toInt())
            }
        }

        return message
    }

    private fun getFormattedLocation(location: Location): String {
        return location.latitude.toString().replace(",", ".") + "," + location.longitude.toString().replace(",", ".")
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
