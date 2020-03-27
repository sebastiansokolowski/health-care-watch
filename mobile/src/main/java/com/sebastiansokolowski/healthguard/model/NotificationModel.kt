package com.sebastiansokolowski.healthguard.model

import android.content.Context
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.model.notification.AndroidNotification
import com.sebastiansokolowski.healthguard.model.notification.SmsNotification
import com.sebastiansokolowski.shared.dataModel.HealthEventType

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class NotificationModel(context: Context, private val settingsModel: SettingsModel) {
    private val androidNotificationModel = AndroidNotification(context)
    private val smsNotificationModel = SmsNotification(settingsModel)

    fun notifyHealthEvent(healthEventEntity: HealthEventEntity) {
        val message = createMessage(healthEventEntity) ?: return

        if (settingsModel.isAndroidNotificationEnabled()) {
            androidNotificationModel.showAlertNotification(message)
        }
        if (settingsModel.isSmsNotificationEnabled()) {
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