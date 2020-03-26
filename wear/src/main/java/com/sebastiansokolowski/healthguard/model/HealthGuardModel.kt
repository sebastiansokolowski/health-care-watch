package com.sebastiansokolowski.healthguard.model

import android.annotation.SuppressLint
import com.sebastiansokolowski.healthguard.client.WearableDataClient
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.healthguard.utils.HealthEnginesUtils
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class HealthGuardModel(private val wearableDataClient: WearableDataClient) {
    private val TAG = javaClass.canonicalName

    private val healthEngines = mutableListOf<HealthGuardEngineBase>()
    private val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()

    lateinit var sensorDataModel: SensorDataModel

    init {
        subscribeToNotifyObservable()
    }

    fun startEngines(measurementSettings: MeasurementSettings) {
        val healthEngines = HealthEnginesUtils.getHealthEngines(measurementSettings.healthEvents)

        healthEngines.forEach {
            it.setupEngine(sensorDataModel.sensorsObservable, notifyObservable, measurementSettings)

            it.startEngine()

            this.healthEngines.add(it)
        }
    }

    fun stopEngines() {
        healthEngines.forEach {
            it.stopEngine()
        }

        healthEngines.clear()
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

    private fun notifyAlert(healthEvent: HealthEvent) {
        wearableDataClient.syncSensorData(true)
        wearableDataClient.sendHealthEvent(healthEvent)
    }

}