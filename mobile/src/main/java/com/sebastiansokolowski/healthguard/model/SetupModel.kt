package com.sebastiansokolowski.healthguard.model

import android.annotation.SuppressLint
import com.sebastiansokolowski.healthguard.client.WearableClient
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 08.07.19.
 */
class SetupModel(private val wearableClient: WearableClient, private val measurementModel: MeasurementModel, private val settingsModel: SettingsModel) {

    val setupStatus: BehaviorSubject<SetupStatus> = BehaviorSubject.createDefault(SetupStatus.CONNECTING)

    enum class SetupStatus {
        CONNECTING,
        SYNC_DATA,
        COMPLETED
    }

    init {
        getSupportedHealthEvents()
    }

    @SuppressLint("CheckResult")
    private fun getSupportedHealthEvents() {
        measurementModel.supportedHealthEventsObservable
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS) {
                    getSupportedHealthEvents()
                }
                .take(1)
                .subscribe {
                    settingsModel.saveSupportedHealthEvents(it)
                    setDefaultHealthCateEvents()
                    getMeasurementState()
                }

        setupStatus.onNext(SetupStatus.CONNECTING)
        wearableClient.getSupportedHealthEvents()
    }

    @SuppressLint("CheckResult")
    private fun getMeasurementState() {
        measurementModel.measurementStateObservable
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS) {
                    getMeasurementState()
                }
                .take(1)
                .subscribe {
                    setupStatus.onNext(SetupStatus.COMPLETED)
                }

        setupStatus.onNext(SetupStatus.SYNC_DATA)
        wearableClient.getMeasurementState()
    }

    private fun setDefaultHealthCateEvents() {
        if (!settingsModel.isFirstSetupCompleted()) {
            val supportedHealthEvents = settingsModel.getSupportedHealthEventTypes()
            settingsModel.saveHealthEvents(supportedHealthEvents)
            settingsModel.setFirstSetupCompleted()
        }
    }
}