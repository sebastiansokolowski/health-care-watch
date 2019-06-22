package com.selastiansokolowski.healthcarewatch.model

import android.content.Context
import android.content.SharedPreferences
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.model.notification.AndroidNotification
import com.selastiansokolowski.healthcarewatch.model.notification.SmsNotification

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
        return when (healthCareEvent.healthCareEventType) {
            HealthCareEvent.HealthCareEventType.EPILEPSY -> {
                ""
            }
            HealthCareEvent.HealthCareEventType.HEARTH_RATE_ANOMALY -> {
                ""
            }
        }
    }
}