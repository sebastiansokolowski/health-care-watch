package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.healthcarewatch.util.SafeCall
import io.objectbox.BoxStore
import io.objectbox.android.AndroidScheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SensorDataViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, private val boxStore: BoxStore) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val supportedSensors: List<Int> = listOf(
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_GRAVITY)

    val currentDateLiveData: MutableLiveData<Date> = MutableLiveData()
    val showLoadingProgressBar: MutableLiveData<Boolean> = MutableLiveData()

    val heartRateLiveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()
    val stepCounterLiveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()
    val pressureLiveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()
    val gravityLiveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()


    init {
        val currentDate = Date()
        currentDateLiveData.postValue(currentDate)
    }

    fun initLiveData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTimestamp = calendar.time.time

        val disposable = sensorDataModel
                .sensorsObservable
                .subscribeOn(Schedulers.io())
                .subscribe {
                    SafeCall.safeLet(it.type, it.timestamp, it.values) { type, timestamp, values ->
                        if (values.isEmpty()) {
                            return@safeLet
                        }
                        if (!supportedSensors.contains(type)) {
                            return@safeLet
                        }

                        val timestampFromMidnight: Int = (timestamp - startTimestamp).toInt()
                        val entry = Entry(timestampFromMidnight.toFloat(), values[0], it)
                        when (type) {
                            Sensor.TYPE_HEART_RATE -> postValueToLiveData(heartRateLiveData, entry)
                            Sensor.TYPE_STEP_COUNTER -> postValueToLiveData(stepCounterLiveData, entry)
                            Sensor.TYPE_PRESSURE -> postValueToLiveData(pressureLiveData, entry)
                            Sensor.TYPE_GRAVITY -> postValueToLiveData(gravityLiveData, entry)
                        }
                    }
                }
        disposables.add(disposable)
    }

    private fun postValueToLiveData(liveData: MutableLiveData<MutableList<Entry>>, entry: Entry) {
        val value: MutableList<Entry> = liveData.value ?: mutableListOf()
        value.add(entry)
        liveData.postValue(value)
    }

    fun initHistoryData(date: Date) {
        initHistoryLiveData(heartRateLiveData, Sensor.TYPE_HEART_RATE, date)
        initHistoryLiveData(stepCounterLiveData, Sensor.TYPE_STEP_COUNTER, date)
        initHistoryLiveData(pressureLiveData, Sensor.TYPE_PRESSURE, date)
        initHistoryLiveData(gravityLiveData, Sensor.TYPE_GRAVITY, date)
    }

    private fun initHistoryLiveData(liveData: MutableLiveData<MutableList<Entry>>, type: Int, date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val startTimestamp = calendar.time.time
        val stopTimestamp = startTimestamp + 24 * 60 * 60 * 1000

        val box = boxStore.boxFor(SensorEventData::class.java)
        val query = box.query().filter {
            it.type == type && it.timestamp!! in startTimestamp..stopTimestamp
        }.build()

        if (query.count() > 0) {
            showLoadingProgressBar.value = true
        }

        query.subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .transform {
                    val result = mutableListOf<Entry>()
                    it.forEach {
                        SafeCall.safeLet(it.timestamp, it.values) { timestamp, values ->
                            val timestampFromMidnight: Int = (timestamp - startTimestamp).toInt()
                            result.add(Entry(timestampFromMidnight.toFloat(), values[0], it))
                        }
                    }
                    return@transform result
                }
                .observer {
                    liveData.postValue(it)
                    showLoadingProgressBar.value = false
                }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}