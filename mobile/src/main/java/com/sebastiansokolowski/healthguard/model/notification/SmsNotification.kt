package com.sebastiansokolowski.healthguard.model.notification

import android.telephony.SmsManager
import com.sebastiansokolowski.healthguard.model.SettingsModel

/**
 * Created by Sebastian Sokołowski on 10.05.19.
 */
class SmsNotification(private val settingsModel: SettingsModel) {
    fun sendSms(message: String) {
        val numbersToNotify = settingsModel.getPhoneNumbers()

        numbersToNotify?.let {
            val smsManager = SmsManager.getDefault()
            it.forEach {
                smsManager.sendTextMessage(it, null, message, null, null)
            }
        }
    }
}