package com.selastiansokolowski.healthcarewatch.model

import android.annotation.SuppressLint
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 08.07.19.
 */
class SetupModel(private val wearableDataClient: WearableDataClient, private val sensorDataModel: SensorDataModel, private val settingsModel: SettingsModel) {

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
                    settingsModel.saveSupportedHealthCareEvents(it)
                    setDefaultHealthCateEvents()
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

    private fun setDefaultHealthCateEvents() {
        if (!settingsModel.isFirstSetupCompleted()) {
            val supportedHealthCareEvents = settingsModel.getSupportedHealthCareEventTypes()
            settingsModel.saveHealthCareEvents(supportedHealthCareEvents)
            settingsModel.setFirstSetupCompleted()
        }
    }
}