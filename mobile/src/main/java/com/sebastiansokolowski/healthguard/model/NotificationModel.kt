package com.sebastiansokolowski.healthguard.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.model.notification.AndroidNotification
import com.sebastiansokolowski.healthguard.model.notification.SmsNotification
import com.sebastiansokolowski.healthguard.util.HealthEventHelper
import io.reactivex.Maybe
import io.reactivex.functions.BiFunction

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class NotificationModel(val context: Context, private val settingsModel: SettingsModel, private val locationModel: LocationModel) {
    private val SMS_NOTIFICATION_TIMEOUT = 30
    private val LOCATION_UPDATE_TIMEOUT = 30

    val androidNotificationModel = AndroidNotification(context)
    val smsNotificationModel = SmsNotification(settingsModel)

    private val healthEventHelper = HealthEventHelper(context)

    @SuppressLint("CheckResult")
    fun notifyHealthEvent(healthEventEntity: HealthEventEntity) {
        val message = createMessage(healthEventEntity)

        var sendSMSObservable: Maybe<Boolean> = Maybe.just(true)
        if (settingsModel.isAndroidNotificationEnabled()) {
            if (settingsModel.isSmsNotificationEnabled()) {
                sendSMSObservable = Maybe.create { emmiter ->
                    androidNotificationModel.showAlertNotification(message, true, SMS_NOTIFICATION_TIMEOUT) {
                        emmiter.onSuccess(it)
                    }
                }
            } else {
                androidNotificationModel.showAlertNotification(message)
            }
        }
        if (settingsModel.isSmsNotificationEnabled()) {
            if (settingsModel.isSmsUserLocationEnabled()) {
                Maybe.zip(sendSMSObservable, getLocationObservable(), BiFunction { sendSMS: Boolean, location: Location? ->
                    if (sendSMS) {
                        smsNotificationModel.sendSms(createMessage(healthEventEntity, location))
                    }
                }).subscribe()
            } else {
                sendSMSObservable.subscribe { sendSMS ->
                    if (sendSMS) {
                        smsNotificationModel.sendSms(message)
                    }
                }
            }
        }
    }

    private fun getLocationObservable(): Maybe<Location?> {
        return Maybe.create {
            locationModel.requestLocation(LOCATION_UPDATE_TIMEOUT, object : LocationModel.LocationCallback {
                override fun onLocationResult(location: Location?) {
                    if (location != null) {
                        it.onSuccess(location)
                    } else {
                        it.onComplete()
                    }
                }
            })
        }
    }

    private fun createMessage(healthEventEntity: HealthEventEntity, location: Location? = null): String {
        var message = healthEventHelper.getNotificationMessage(healthEventEntity)
        if (location != null) {
            message += "\n\n" + healthEventHelper.getLocationMessage(location)
        }

        return message
    }

    fun dismissAlertNotification(notificationId: Int) {
        androidNotificationModel.dismissAlertNotification(notificationId)
    }
}