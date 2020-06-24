package com.sebastiansokolowski.healthguard.model

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
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

    private var measurementId = -1L
    private var liveData = AtomicBoolean(false)
    private var sensorsRegistered = AtomicBoolean(false)
    private val sensorHandler = initSensorHandler()

    var heartRateObservable: ReplaySubject<SensorEvent> = ReplaySubject.createWithSize(10)
    val sensorsObservable: PublishSubject<SensorEvent> = PublishSubject.create()
    private val sensorDataToSend = mutableListOf<SensorEvent>()

    private var sensorDataParserDisposables = CompositeDisposable()
    private var sensorDataSyncDisposable: Disposable? = null

    private fun initSensorHandler(): Handler {
        val handlerThread = HandlerThread("sensorThread")
        handlerThread.start()
        return Handler(handlerThread.looper)
    }

    fun registerSensors(measurementId: Long, sensors: Set<Int>, samplingPeriodUs: Int) {
        Timber.d("registerSensors measurementId=$measurementId, sensors=$sensors, samplingPeriodUs=$samplingPeriodUs")
        this.measurementId = measurementId

        startSensorDataParser()
        startSensorDataSync(liveData.get())

        for (sensorId: Int in sensors) {
            val sensor = sensorManager.getDefaultSensor(sensorId)

            val registered = sensorManager.registerListener(this, sensor, samplingPeriodUs, sensorHandler)
            if (!registered) {
                Log.e(TAG, "error register sensorEvent: $sensorId")
            } else {
                Timber.d("registered sensorEvent: $sensorId")
            }
        }
        sensorsRegistered.set(true)
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
            if (sensor.type == Sensor.TYPE_HEART_RATE && values[0] <= 0f) {
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
                    measurementId,
                    timestamp
            )
            if (BuildConfig.EXTRA_LOGGING) {
                Timber.d("onSensorChanged sensorEvent=$sensorEventWrapper")
            }

            when (sensor.type) {
                Sensor.TYPE_HEART_RATE -> {
                    heartRateObservable.onNext(sensorEventWrapper)
                }
            }
            sensorsObservable.onNext(sensorEventWrapper)
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
        sensorsObservable
                .subscribeOn(Schedulers.io())
                .groupBy { it.type }
                .subscribe {
                    it.buffer(1, TimeUnit.SECONDS)
                            .subscribe eventsSubscribe@{ events ->
                                if (events.isNullOrEmpty()) {
                                    return@eventsSubscribe
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
                                            measurementId,
                                            event.timestamp)
                                }
                                Timber.d("sensorDataParser send event=$event")
                                sensorDataToSend.add(event)
                            }.let {
                                sensorDataParserDisposables.add(it)
                            }
                }.let {
                    sensorDataParserDisposables.add(it)
                }
    }
}