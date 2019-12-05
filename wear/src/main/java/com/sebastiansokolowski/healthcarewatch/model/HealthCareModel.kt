package com.sebastiansokolowski.healthcarewatch.model

import android.annotation.SuppressLint
import com.sebastiansokolowski.healthcarewatch.client.WearableDataClient
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.sebastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

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
            it.setupEngine(sensorDataModel.sensorsObservable, notifyObservable, measurementSettings)

            it.startEngine()

            healthCareEngines.add(it)
        }
    }

    fun stopEngines() {
        healthCareEngines.forEach {
            it.stopEngine()
        }

        healthCareEngines.clear()
    }

    @SuppressLint("CheckResult")
    private fun subscribeToNotifyObservable() {
        notifyObservable
                .debounce(10, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    notifyAlert(it)
                }
    }

    private fun notifyAlert(healthCareEvent: HealthCareEvent) {
        wearableDataClient.sendHealthCareEvent(healthCareEvent)
    }

}