package com.sebastiansokolowski.healthguard.model

import android.content.Context
import android.content.SharedPreferences
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.model.notification.AndroidNotification
import com.sebastiansokolowski.healthguard.model.notification.SmsNotification
import com.sebastiansokolowski.shared.dataModel.HealthEventType

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

    fun notifyHealthEvent(healthEventEntity: HealthEventEntity) {
        val message = createMessage(healthEventEntity) ?: return

        if (isAndroidNotificationEnabled()) {
            androidNotificationModel.showAlertNotification(message)
        }
        if (isSmsNotificationEnabled()) {
            smsNotificationModel.sendSms(message)
        }
    }

    private fun createMessage(healthEventEntity: HealthEventEntity): String? {
        return when (healthEventEntity.event) {
            HealthEventType.EPILEPSY -> {
                ""
            }
            HealthEventType.HEARTH_RATE_ANOMALY -> {
                ""
            }
            else -> null
        }
    }
}