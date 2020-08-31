package com.sebastiansokolowski.healthguard.model

import android.annotation.SuppressLint
import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.healthguard.model.healthGuard.engine.*
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class HealthGuardModel(private val sensorDataModel: SensorDataModel, private val wearableClient: WearableClient) {
    private val TAG = javaClass.canonicalName

    private val healthEnginesRegistered = mutableSetOf<HealthGuardEngineBase>()

    private val healthEngines = mutableListOf<HealthGuardEngineBase>()
    private val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()

    init {
        subscribeToNotifyObservable()
        registerHealthEngines()
    }

    private fun registerHealthEngines() {
        healthEnginesRegistered.add(ConvulsionsEngine())
//        healthEnginesRegistered.add(FallEngine())
        healthEnginesRegistered.add(FallEngineAdvanced())
        healthEnginesRegistered.add(HeartRateAnomalyEngine())
//        healthEnginesRegistered.add(AllSensorsEngine())
//        healthEnginesRegistered.add(NotificationTestEngine())
    }

    fun getHealthEngines(healthEvents: Set<HealthEventType>): Set<HealthGuardEngineBase> {
        return healthEnginesRegistered.filter { healthEvents.contains(it.getHealthEventType()) }.toSet()
    }

    fun getSupportedHealthEvents(sensors: List<Sensor>): Set<HealthEventType> {
        val sensorTypes = sensors.map { it.type }
        val supportedHealthEvents = mutableSetOf<HealthEventType>()

        healthEnginesRegistered.forEach {
            if (sensorTypes.containsAll(it.requiredSensors())) {
                supportedHealthEvents.add(it.getHealthEventType())
            }
        }
        return supportedHealthEvents
    }

    fun startEngines(measurementSettings: MeasurementSettings) {
        val healthEngines = getHealthEngines(measurementSettings.healthEvents)

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

    fun setBatterySaveMode(batterySaveMode: Boolean) {
        healthEngines.forEach {
            if (it.requiredSensors().contains(Sensor.TYPE_LINEAR_ACCELERATION)) {
                if (batterySaveMode) {
                    it.stopEngine()
                } else {
                    it.startEngine()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun subscribeToNotifyObservable() {
        notifyObservable
                .groupBy { it.healthEventType }
                .subscribe {
                    it.debounce(5, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .subscribe {
                                notifyAlert(it)
                            }
                }
    }

    private fun notifyAlert(healthEvent: HealthEvent) {
        sensorDataModel.syncSensorData(true)
        wearableClient.sendSensorEvents(healthEvent.sensorEventsSample, urgent = true, highFrequencyData = true)
        wearableClient.sendHealthEvent(healthEvent)
    }

}