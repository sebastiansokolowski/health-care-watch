package com.sebastiansokolowski.healthcarewatch.model

import android.content.Context
import android.content.SharedPreferences
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.model.notification.AndroidNotification
import com.sebastiansokolowski.healthcarewatch.model.notification.SmsNotification
import com.sebastiansokolowski.shared.healthCare.HealthCareEventType

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class NotificationModel(context: Context, private val prefs: SharedPreferences) {
    private val androidNotificationModel = AndroidNotification(context)
    private val smsNotificationModel = SmsNotification(prefs)

    private fun isAndroidNotificationEnabled(): Boolean {
        return prefs.getBoolean("android_notification_enabled", false)
    }

    private fun isSmsNotificationEnabled(): Boolean {
        return prefs.getBoolean("sms_notification_enabled", false)
    }

    fun notifyHealthCareEvent(healthCareEvent: HealthCareEvent) {
        val message = createMessage(healthCareEvent) ?: return

        if (isAndroidNotificationEnabled()) {
            androidNotificationModel.showAlertNotification(message)
        }
        if (isSmsNotificationEnabled()) {
            smsNotificationModel.sendSms(message)
        }
    }

    private fun createMessage(healthCareEvent: HealthCareEvent): String? {
        return when (healthCareEvent.careEvent) {
            HealthCareEventType.EPILEPSY -> {
                ""
            }
            HealthCareEventType.HEARTH_RATE_ANOMALY -> {
                ""
            }
            else -> null
        }
    }
}