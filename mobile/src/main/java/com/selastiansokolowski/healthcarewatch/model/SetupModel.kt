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

    val setupComplete: BehaviorSubject<SetupStep> = BehaviorSubject.createDefault(SetupStep.CONNECTING)

    enum class SetupStep {
        CONNECTING,
        SYNC_DATA,
        COMPLETED
    }

    init {
        getSupportedHealthCareEvents()
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
                    getMeasurementState()
                    disposable?.dispose()
                }

        setupComplete.onNext(SetupStep.CONNECTING)
        wearableDataClient.getSupportedHealthCareEvents()
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
                    disposable?.dispose()
                    setupComplete.onNext(SetupStep.COMPLETED)
                }

        setupComplete.onNext(SetupStep.SYNC_DATA)
        wearableDataClient.getMeasurementState()
    }

    private fun saveSupportedHealthCareEvents(healthCareEvents: List<HealthCareEventType>) {
        prefs.edit()?.apply {
            val values = healthCareEvents.map { sensor -> sensor.name }.toSet()
            putStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, values)
            apply()
        }
    }

}