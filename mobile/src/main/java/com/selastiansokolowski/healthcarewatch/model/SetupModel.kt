package com.selastiansokolowski.healthcarewatch.model

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.shared.SettingsSharedPreferences
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 08.07.19.
 */
class SetupModel(private val prefs: SharedPreferences, private val wearableDataClient: WearableDataClient, private val sensorDataModel: SensorDataModel) {

    val setupComplete: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    init {
        if (!isHealthCareEventsSynced()) {
            getSupportedHealthCareEvents()
        } else {
            setupComplete.onNext(true)
        }
    }

    private fun isHealthCareEventsSynced(): Boolean {
        val healthCareEvents = prefs.getStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, emptySet())
                ?: emptySet()

        return healthCareEvents.isNotEmpty()
    }

    @SuppressLint("CheckResult")
    private fun getSupportedHealthCareEvents() {
        sensorDataModel.supportedHealthCareEventsObservable
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS) {
                    getSupportedHealthCareEvents()
                }
                .subscribe {
                    saveSupportedHealthCareEvents(it)
                    setupComplete.onNext(true)
                }

        wearableDataClient.getSupportedHealthCareEvents()
    }

    private fun saveSupportedHealthCareEvents(healthCareEvents: List<HealthCareEventType>) {
        prefs.edit()?.apply {
            val values = healthCareEvents.map { sensor -> sensor.name }.toSet()
            putStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, values)
            apply()
        }
    }

}