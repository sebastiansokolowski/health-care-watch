package com.sebastiansokolowski.healthguard.model

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Sebastian Sokołowski on 18.06.19.
 */
class SensorDataModel(private val sensorManager: SensorManager, private val wearableClient: WearableClient) : SensorEventListener {
    private val TAG = javaClass.canonicalName

    private var measurementSettings: MeasurementSettings = MeasurementSettings()
    private var liveData = AtomicBoolean(false)
    private var sensorsRegistered = AtomicBoolean(false)
    private val sensorHandler = initSensorHandler()

    private val sensorDataToSend = mutableListOf<SensorEvent>()

    private var sensorDataParserDisposables = CompositeDisposable()
    private var sensorDataSyncDisposable: Disposable? = null

    val sensorsObservable = SensorsObservable()

    private fun initSensorHandler(): Handler {
        val handlerThread = HandlerThread("sensorThread")
        handlerThread.start()
        return Handler(handlerThread.looper)
    }

    fun registerSensors(measurementSettings: MeasurementSettings, sensors: Set<Int>, samplingPeriodUs: Int) {
        Timber.d("registerSensors measurementSettings=$measurementSettings, sensors=$sensors, samplingPeriodUs=$samplingPeriodUs")
        this.measurementSettings = measurementSettings

        startSensorDataParser()
        startSensorDataSync(liveData.get())

        for (sensorId: Int in sensors) {
            registerSensor(sensorId, samplingPeriodUs)
        }
        sensorsRegistered.set(true)
    }

    fun registerSensor(sensorId: Int, samplingPeriodUs: Int) {
        Timber.d("registerSensor sensorEvent=$sensorId samplingPeriodUs=$samplingPeriodUs")

        val sensor = sensorManager.getDefaultSensor(sensorId)

        if (sensor == null) {
            Timber.e("Sensor is null")
        }

        val registered = sensorManager.registerListener(this, sensor, samplingPeriodUs, sensorHandler)
        if (!registered) {
            Timber.e("error register sensorEvent=$sensorId")
        } else {
            Timber.d("registered sensorEvent=$sensorId")
        }
    }

    fun unregisterSensor(sensorId: Int) {
        Timber.d("unregisterSensor sensorEvent=$sensorId")

        val sensor = sensorManager.getDefaultSensor(sensorId)

        sensorManager.unregisterListener(this, sensor)
    }

    fun unregisterSensors() {
        Timber.d("unregisterSensors")
        sensorManager.unregisterListener(this)
        sensorsRegistered.set(false)
        sensorDataParserDisposables.clear()
        sensorDataSyncDisposable?.dispose()
        syncSensorData(liveData.get())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Timber.d("onAccuracyChanged sensorEvent=$sensor accuracy=$accuracy")
    }

    override fun onSensorChanged(event: android.hardware.SensorEvent?) {
        val timestamp = Date().time
        event!!.apply {
            if (sensor == null || values == null || values.isEmpty()) {
                return
            }
            if (sensor.type == Sensor.TYPE_HEART_RATE && values[0] <= 1f) {
                return
            }

            val value = when (sensor.type) {
                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    sqrt(
                            values[0].toDouble().pow(2.0) +
                                    values[1].toDouble().pow(2.0) +
                                    values[2].toDouble().pow(2.0)
                    ).toFloat()
                }
                else -> {
                    values[0]
                }
            }
            val sensorEventWrapper = SensorEvent(
                    sensor.type,
                    value,
                    accuracy,
                    measurementSettings.measurementId,
                    timestamp
            )
            if (BuildConfig.EXTRA_LOGGING) {
                Timber.d("onSensorChanged sensorEvent=$sensorEventWrapper")
            }

            when (sensor.type) {
                Sensor.TYPE_HEART_RATE -> {
                    sensorsObservable.heartRateObservable.onNext(sensorEventWrapper)
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    sensorsObservable.stepDetectorObservable.onNext(sensorEventWrapper)
                }
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    sensorsObservable.linearAccelerationObservable.onNext(sensorEventWrapper)
                }
            }
        }
    }

    fun changeLiveDataState(liveData: Boolean) {
        Timber.d("changeLiveDataState liveData=$liveData")
        this.liveData.set(liveData)

        if (sensorsRegistered.get()) {
            sensorDataSyncDisposable?.dispose()
            startSensorDataSync(liveData)
        }
    }

    private fun startSensorDataSync(liveData: Boolean) {
        Timber.d("startSensorDataSync liveData=$liveData")
        val period: Long = if (liveData) {
            1
        } else {
            300
        }

        Observable.interval(period, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    syncSensorData(liveData)
                }.let {
                    sensorDataSyncDisposable = it
                }
    }

    fun syncSensorData(liveData: Boolean) {
        val dataToSend = mutableListOf<SensorEvent>()
        synchronized(sensorDataToSend) {
            dataToSend.addAll(sensorDataToSend)
            sensorDataToSend.clear()
        }
        Timber.d("syncSensorData dataToSend.size=${dataToSend.size}")
        wearableClient.sendSensorEvents(dataToSend, liveData)
    }

    private fun startSensorDataParser() {
        Timber.d("startSensorDataParser")
        sensorsObservable.heartRateObservable
                .subscribeOn(Schedulers.io())
                .subscribe { event ->
                    Timber.d("sensorDataParser send event=$event")
                    synchronized(sensorDataToSend) {
                        sensorDataToSend.add(event)
                    }
                }.let {
                    sensorDataParserDisposables.add(it)
                }
        if (measurementSettings.testMode) {
            sensorsObservable.linearAccelerationObservable
                    .subscribeOn(Schedulers.io())
                    .subscribe { event ->
                        if (BuildConfig.EXTRA_LOGGING) {
                            Timber.d("sensorDataParser send event=$event")
                        }
                        synchronized(sensorDataToSend) {
                            sensorDataToSend.add(event)
                        }
                    }.let {
                        sensorDataParserDisposables.add(it)
                    }
        } else {
            sensorsObservable.linearAccelerationObservable
                    .subscribeOn(Schedulers.io())
                    .buffer(1, TimeUnit.SECONDS)
                    .subscribe { events ->
                        if (events.isNullOrEmpty()) {
                            return@subscribe
                        }
                        if (BuildConfig.EXTRA_LOGGING) {
                            events.forEach {
                                Timber.d("sensorDataParser event=$it")
                            }
                        }
                        var event = events.first()
                        if (events.size > 1) {
                            var avgValue = 0f
                            var avgAccuracy = 0
                            events.forEach { event ->
                                avgValue += event.value
                                avgAccuracy += event.accuracy
                            }
                            avgValue /= events.size
                            avgAccuracy /= events.size

                            event = SensorEvent(
                                    event.type,
                                    avgValue,
                                    avgAccuracy,
                                    measurementSettings.measurementId,
                                    event.timestamp)
                        }
                        Timber.d("sensorDataParser send event=$event")
                        synchronized(sensorDataToSend) {
                            sensorDataToSend.add(event)
                        }
                    }.let {
                        sensorDataParserDisposables.add(it)
                    }
        }
    }
}