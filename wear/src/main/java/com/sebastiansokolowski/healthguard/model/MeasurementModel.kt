package com.sebastiansokolowski.healthguard.model

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.PowerManager
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.shared.DataClientPaths
import com.sebastiansokolowski.shared.dataModel.SupportedHealthEventTypes
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Sebastian Sokołowski on 06.07.19.
 */
class MeasurementModel(private val sensorDataModel: SensorDataModel, private val healthGuardModel: HealthGuardModel, private val sensorManager: SensorManager, powerManager: PowerManager, private val wearableClient: WearableClient) {
    private val TAG = javaClass.canonicalName

    private var measurementRunning = AtomicBoolean(false)
    private var wakeLock: PowerManager.WakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HealthGuard::MeasurementWakeLock")

    val measurementStateObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()
    var measurementSettings = MeasurementSettings()

    fun notifySupportedHealthEvents() {
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val supportedHealthEvents = healthGuardModel.getSupportedHealthEvents(sensors)
        wearableClient.sendSupportedHealthEvents(SupportedHealthEventTypes(supportedHealthEvents))
    }

    private fun changeMeasurementState(state: Boolean) {
        if (measurementRunning.get() == state) {
            return
        }
        measurementRunning.set(state)
        if (state) {
            sensorDataModel.sensorsObservable.heartRateObservable = ReplaySubject.createWithSize(10)
        } else {
            sensorDataModel.sensorsObservable.heartRateObservable.onComplete()
        }
        measurementStateObservable.onNext(measurementRunning.get())
        notifyMeasurementState()
    }

    fun notifyMeasurementState() {
        wearableClient.sendMeasurementEvent(measurementRunning.get())
    }

    fun toggleMeasurementState() {
        if (measurementRunning.get()) {
            stopMeasurement()
        } else {
            wearableClient.requestStartMeasurement()
        }
    }

    @SuppressLint("WakelockTimeout")
    fun startMeasurement(measurementSettings: MeasurementSettings) {
        Timber.d("startMeasurement")
        this.measurementSettings = measurementSettings
        if (measurementRunning.get()) {
            return
        }

        val healthEngines = healthGuardModel.getHealthEngines(measurementSettings.healthEvents)
        val sensors = healthEngines.flatMap { it.requiredSensors() }.toSet()
        val samplingPeriodUs = TimeUnit.MILLISECONDS.toMicros(measurementSettings.samplingMs.toLong()).toInt()

        changeMeasurementState(true)
        healthGuardModel.startEngines(measurementSettings)
        sensorDataModel.registerSensors(measurementSettings.measurementId, sensors, samplingPeriodUs)
        wakeLock.acquire()
//        startBatterySaveEnabler()
    }


    fun stopMeasurement() {
        Timber.d("stopMeasurement")
        if (!measurementRunning.get()) {
            return
        }
        changeMeasurementState(false)
//        startBatterySaveEnablerDisposable?.dispose()
//        startBatterySaveDisablerDisposable?.dispose()
        healthGuardModel.stopEngines()
        sensorDataModel.unregisterSensors()
        wakeLock.release()
    }

//    var startBatterySaveEnablerDisposable: Disposable? = null
//    var startBatterySaveDisablerDisposable: Disposable? = null

//    private fun startBatterySaveEnabler() {
//        sensorDataModel.stepDetectorObservable
//                .subscribeOn(Schedulers.io())
//                .timeout(1, TimeUnit.MINUTES)
//                .subscribe({
//                }, {
//                    sensorDataModel.unregisterSensor(Sensor.TYPE_LINEAR_ACCELERATION)
//                    healthGuardModel.setBatterySaveMode(true)
//                    startBatterySaveDisabler()
//                })
//                .let {
//                    startBatterySaveEnablerDisposable = it
//                }
//    }
//
//    private fun startBatterySaveDisabler() {
//        sensorDataModel.stepDetectorObservable
//                .subscribeOn(Schedulers.io())
//                .take(1)
//                .subscribe {
//                    sensorDataModel.registerSensor(Sensor.TYPE_LINEAR_ACCELERATION, TimeUnit.MILLISECONDS.toMicros(measurementSettings.samplingMs.toLong()).toInt())
//                    healthGuardModel.setBatterySaveMode(false)
//
//                    startBatterySaveEnabler()
//                }.let {
//                    startBatterySaveDisablerDisposable = it
//                }
//    }

    fun isAvailableSafeBatteryMode(sensors: Set<Int>): Boolean {
        sensors.forEach {
            when (it) {
                Sensor.TYPE_STEP_DETECTOR,
                Sensor.TYPE_HEART_RATE -> {
                    val sensorList = sensorManager.getSensorList(it)
                    val wakeUpSensor = sensorList.find { it.isWakeUpSensor }
                    if (wakeUpSensor == null) {
                        return false
                    }
                }
            }
        }

        return true
    }

    fun onDataChanged(dataItem: DataItem) {
        when ("/" + dataItem.uri.pathSegments.getOrNull(0)?.removePrefix("/")) {
            DataClientPaths.MEASUREMENT_START_DATA_PATH -> {
                DataMapItem.fromDataItem(dataItem).dataMap.apply {
                    val json = getString(DataClientPaths.MEASUREMENT_START_DATA_JSON)
                    val measurementSettings = Gson().fromJson(json, MeasurementSettings::class.java)

                    Timber.d("measurementSettings=$measurementSettings")

                    if (measurementRunning.get()) {
                        stopMeasurement()
                    }
                    startMeasurement(measurementSettings)
                }
            }
        }
    }
}