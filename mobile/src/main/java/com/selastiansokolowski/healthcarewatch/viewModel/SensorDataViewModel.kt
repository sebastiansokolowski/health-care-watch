package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.healthcarewatch.util.SafeCall
import io.objectbox.BoxStore
import io.objectbox.reactive.DataSubscriptionList
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SensorDataViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, private val boxStore: BoxStore) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val subscriptions = DataSubscriptionList()

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

    private fun changeCurrentDateDay(amount: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDateLiveData.value
        calendar.add(Calendar.DAY_OF_MONTH, amount)

        currentDateLiveData.postValue(calendar.time)
    }

    fun decreaseCurrentDate() {
        changeCurrentDateDay(-1)
    }

    fun increaseCurrentDate() {
        changeCurrentDateDay(1)
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
        val query = box.query().build()

        var disposable: Disposable? = null
        disposable = RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .map {
                    it.filter { it.type == type && it.timestamp!! in startTimestamp..stopTimestamp }
                }
                .map {
                    val result = mutableListOf<Entry>()
                    it.forEach {
                        SafeCall.safeLet(it.timestamp, it.values) { timestamp, values ->
                            val timestampFromMidnight: Int = (timestamp - startTimestamp).toInt()
                            result.add(Entry(timestampFromMidnight.toFloat(), values[0], it))
                        }
                    }
                    return@map result
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    showLoadingProgressBar.value = true
                }
                .subscribe {
                    liveData.postValue(it)
                    showLoadingProgressBar.value = false
                    disposable?.dispose()
                }
        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        subscriptions.cancel()
    }
}