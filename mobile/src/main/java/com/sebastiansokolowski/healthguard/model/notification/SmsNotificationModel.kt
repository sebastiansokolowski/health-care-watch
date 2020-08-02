package com.sebastiansokolowski.healthguard.model.notification

import android.telephony.SmsManager
import com.sebastiansokolowski.healthguard.model.SettingsModel

/**
 * Created by Sebastian Sokołowski on 10.05.19.
 */
class SmsNotificationModel(private val settingsModel: SettingsModel) {
    fun sendSms(message: String) {
        val smsManager = SmsManager.getDefault()
        val messageDivided = smsManager.divideMessage(message)
        val numbersToNotify = settingsModel.getPhoneNumbers()

        numbersToNotify?.let {
            it.forEach { number ->
                smsManager.sendMultipartTextMessage(number, null, messageDivided, null, null)
            }
        }
    }
}