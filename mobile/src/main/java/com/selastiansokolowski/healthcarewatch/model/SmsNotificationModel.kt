package com.selastiansokolowski.healthcarewatch.model

import android.content.SharedPreferences
import android.telephony.SmsManager

/**
 * Created by Sebastian Sokołowski on 10.05.19.
 */
class SmsNotificationModel(val prefs: SharedPreferences) {
    fun sendSms(message: String) {
        val numbersToNotify = getPhoneNumbers()

        numbersToNotify?.let {
            val smsManager = SmsManager.getDefault()
            it.forEach {
                smsManager.sendTextMessage(it, null, message, null, null)
            }
        }
    }

    private fun getPhoneNumbers(): MutableSet<String>? {
        return prefs.getStringSet("contacts", mutableSetOf())
    }
}