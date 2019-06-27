package com.selastiansokolowski.healthcarewatch.model

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.selastiansokolowski.healthcarewatch.model.healthCare.engine.EpilepsyCareEngine
import com.selastiansokolowski.healthcarewatch.model.healthCare.engine.HeartRateAnomalyEngine
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class HealthCareModel(private val sensorDataModel: SensorDataModel, private val notificationModel: NotificationModel, val pref: SharedPreferences) {

    private val healthCareEngines = mutableListOf<HealthCareEngineBase>()
    private val notifyObservable: PublishSubject<HealthCareEvent> = PublishSubject.create()

    init {
        createEngines()
        setupEngines()

        subscribeToNotifyObservable()
    }

    private fun createEngines() {
        healthCareEngines.add(EpilepsyCareEngine())
        healthCareEngines.add(HeartRateAnomalyEngine())
    }

    private fun setupEngines() {
        healthCareEngines.forEach {
            it.setSensorObservable(sensorDataModel.sensorsObservable)
            it.setNotifyObservable(notifyObservable)
        }
    }

    @SuppressLint("CheckResult")
    private fun subscribeToNotifyObservable() {
        notifyObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    notifyAlert(it)
                }
    }

    private fun notifyAlert(healthCareEvent: HealthCareEvent) {
        notificationModel.notifyHealthCareEvent(healthCareEvent)
    }

}