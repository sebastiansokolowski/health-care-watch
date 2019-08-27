package com.selastiansokolowski.healthcarewatch.model

import android.annotation.SuppressLint
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class HealthCareModel(private val wearableDataClient: WearableDataClient) {
    private val TAG = javaClass.canonicalName

    private val healthCareEngines = mutableListOf<HealthCareEngineBase>()
    private val notifyObservable: PublishSubject<HealthCareEvent> = PublishSubject.create()

    lateinit var sensorDataModel: SensorDataModel

    init {
        subscribeToNotifyObservable()
    }

    fun startEngines(measurementSettings: MeasurementSettings) {
        measurementSettings.healthCareEngines.forEach {
            healthCareEngines.add(it)
        }

        setupEngines()
    }

    fun stopEngines() {
        healthCareEngines.forEach {
            it.compositeDisposable.clear()
        }

        healthCareEngines.clear()
    }

    private fun setupEngines() {
        healthCareEngines.forEach {
            it.setSensorEventObservable(sensorDataModel.sensorsObservable)
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
        wearableDataClient.sendHealthCareEvent(healthCareEvent)
    }

}