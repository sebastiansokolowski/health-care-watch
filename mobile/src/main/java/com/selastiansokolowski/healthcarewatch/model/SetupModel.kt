package com.selastiansokolowski.healthcarewatch.model

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.shared.SettingsSharedPreferences
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 08.07.19.
 */
class SetupModel(private val prefs: SharedPreferences, private val wearableDataClient: WearableDataClient, private val sensorDataModel: SensorDataModel) {

    val setupComplete: BehaviorSubject<SETUP_STEP> = BehaviorSubject.createDefault(SETUP_STEP.CONNECTING)

    enum class SETUP_STEP {
        CONNECTING,
        SYNC_HEALTH_CARE_EVENTS,
        COMPLETED
    }

    init {
        getMeasurementState()
    }

    private fun isHealthCareEventsSynced(): Boolean {
        val healthCareEvents = prefs.getStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, emptySet())
                ?: emptySet()

        return healthCareEvents.isNotEmpty()
    }

    @SuppressLint("CheckResult")
    private fun getSupportedHealthCareEvents() {
        var disposable: Disposable? = null
        disposable = sensorDataModel.supportedHealthCareEventsObservable
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS) {
                    getSupportedHealthCareEvents()
                }
                .subscribe {
                    saveSupportedHealthCareEvents(it)
                    setupComplete.onNext(SETUP_STEP.COMPLETED)
                    disposable?.dispose()
                }

        wearableDataClient.getSupportedHealthCareEvents()
        setupComplete.onNext(SETUP_STEP.SYNC_HEALTH_CARE_EVENTS)
    }

    @SuppressLint("CheckResult")
    private fun getMeasurementState() {
        var disposable: Disposable? = null
        disposable = sensorDataModel.measurementStateObservable
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS) {
                    getMeasurementState()
                }
                .subscribe {
                    if (!isHealthCareEventsSynced()) {
                        getSupportedHealthCareEvents()
                    } else {
                        setupComplete.onNext(SETUP_STEP.COMPLETED)
                    }
                    disposable?.dispose()
                }

        wearableDataClient.getMeasurementState()
        setupComplete.onNext(SETUP_STEP.CONNECTING)
    }

    private fun saveSupportedHealthCareEvents(healthCareEvents: List<HealthCareEventType>) {
        prefs.edit()?.apply {
            val values = healthCareEvents.map { sensor -> sensor.name }.toSet()
            putStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, values)
            apply()
        }
    }

}