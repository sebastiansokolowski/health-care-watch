package com.sebastiansokolowski.healthguard.model

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.db.entity.MeasurementEventEntity
import com.sebastiansokolowski.healthguard.service.MeasurementService
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SupportedHealthEventTypes
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.objectbox.BoxStore
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*


/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
class MeasurementModel(val context: Context, private val wearableClient: WearableClient, boxStore: BoxStore, private val settingsModel: SettingsModel, val sensorDataModel: SensorDataModel) {
    private val TAG = javaClass.canonicalName

    var measurementRunning: Boolean = false
    private var measurementEventEntity: MeasurementEventEntity? = null

    //observables
    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val supportedHealthEventsObservable: PublishSubject<Set<HealthEventType>> = PublishSubject.create()

    //boxes
    val measurementEventEntityBox = boxStore.boxFor(MeasurementEventEntity::class.java)

    private fun notifyMeasurementStateObservable(measurementState: Boolean) {
        measurementStateObservable.onNext(measurementState)
    }

    private fun notifySupportedHealthEventsObservable(healthEvents: Set<HealthEventType>) {
        supportedHealthEventsObservable.onNext(healthEvents)
        supportedHealthEventsObservable.onComplete()
    }

    private fun changeMeasurementState(state: Boolean) {
        if (measurementRunning == state) {
            return
        }
        measurementRunning = state
        val serviceIntent = Intent(context, MeasurementService::class.java)
        if (state) {
            context.startService(serviceIntent)
            sensorDataModel.heartRateObservable = BehaviorSubject.create()
        } else {
            context.stopService(serviceIntent)
            sensorDataModel.heartRateObservable.onComplete()
        }
        measurementStateObservable.onNext(measurementRunning)
    }

    fun toggleMeasurementState() {
        if (measurementRunning) {
            wearableClient.sendStopMeasurementEvent()
            stopMeasurement()
        } else {
            requestStartMeasurement()
        }
    }

    fun requestStartMeasurement() {
        val measurementSettings = settingsModel.getMeasurementSettings()

        measurementEventEntity = createMeasurementEventEntity(measurementSettings).apply {
            measurementEventEntityBox.put(this)
            measurementSettings.measurementId = id
        }

        wearableClient.sendStartMeasurementEvent(measurementSettings)
    }

    fun startMeasurement() {
        measurementEventEntity?.apply {
            startTimestamp = Date().time
            measurementEventEntityBox.put(this)
        }

        notifyMeasurementStateObservable(true)
        if (measurementRunning) {
            return
        }
        changeMeasurementState(true)
        sensorDataModel.measurementEventEntity = measurementEventEntity
    }

    fun stopMeasurement() {
        measurementEventEntity?.apply {
            stopTimestamp = Date().time
            measurementEventEntityBox.put(this)
        }

        notifyMeasurementStateObservable(false)
        if (!measurementRunning) {
            return
        }
        changeMeasurementState(false)
    }

    fun onDataChanged(dataItem: DataItem) {
        when (dataItem.uri.path) {
            DataClientPaths.SUPPORTED_HEALTH_EVENTS_MAP_PATH -> {
                DataMapItem.fromDataItem(dataItem).dataMap.apply {
                    val json = getString(DataClientPaths.HEALTH_EVENT_MAP_JSON)
                    val healthEventTypesSupported = Gson().fromJson(json, SupportedHealthEventTypes::class.java)

                    notifySupportedHealthEventsObservable(healthEventTypesSupported.supportedTypes)

                    Log.d(TAG, "supported health events :$healthEventTypesSupported")
                }
            }
        }
    }

    private fun createMeasurementEventEntity(measurementSettings: MeasurementSettings): MeasurementEventEntity {
        return MeasurementEventEntity().apply {
            this.measurementSettings = Gson().toJson(measurementSettings)
        }
    }
}