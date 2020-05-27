package com.sebastiansokolowski.healthguard.model

import android.content.Context
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.model.notification.AndroidNotification
import com.sebastiansokolowski.healthguard.model.notification.SmsNotification
import com.sebastiansokolowski.healthguard.util.HealthEventHelper

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class NotificationModel(val context: Context, private val settingsModel: SettingsModel) {
    val androidNotificationModel = AndroidNotification(context)
    val smsNotificationModel = SmsNotification(settingsModel)

    private val healthEventHelper = HealthEventHelper(context)

    fun notifyHealthEvent(healthEventEntity: HealthEventEntity) {
        val message = createMessage(healthEventEntity) ?: return

        if (settingsModel.isAndroidNotificationEnabled()) {
            androidNotificationModel.showAlertNotification(message, settingsModel.isSmsNotificationEnabled()) {
                smsNotificationModel.sendSms(message)
            }
        } else if (settingsModel.isSmsNotificationEnabled()) {
            smsNotificationModel.sendSms(message)
        }
    }

    private fun createMessage(healthEventEntity: HealthEventEntity): String? {
        return healthEventHelper.getNotificationMessage(healthEventEntity)
    }

    fun dismissAlertNotification(notificationId: Int) {
        androidNotificationModel.dismissAlertNotification(notificationId)
    }
}