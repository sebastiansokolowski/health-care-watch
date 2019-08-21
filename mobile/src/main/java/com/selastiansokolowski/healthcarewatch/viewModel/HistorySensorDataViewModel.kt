package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent_
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData_
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
class HistorySensorDataViewModel
@Inject constructor(boxStore: BoxStore) : HealthCareEventViewModel(boxStore) {

    private val disposables = CompositeDisposable()
    private val subscriptions = DataSubscriptionList()

    val showLoadingProgressBar: MutableLiveData<Boolean> = MutableLiveData()

    val showStatisticsContainer: MutableLiveData<Boolean> = MutableLiveData()
    val statisticMinValue: MutableLiveData<Float> = MutableLiveData()
    val statisticMaxValue: MutableLiveData<Float> = MutableLiveData()
    val statisticAverageValue: MutableLiveData<Float> = MutableLiveData()

    val liveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()
    val entryHighlighted: MutableLiveData<Entry> = MutableLiveData()

    private var currentDate = Date()
    private var sensorType: Int = 0

    fun setCurrentDate(date: Date) {
        currentDate = date
        entryHighlighted.postValue(null)
        refreshView()
    }

    fun setSensorType(sensorType: Int) {
        this.sensorType = sensorType
    }

    override fun initHealthCarEvents() {
        val startDayTimestamp = getStartDayTimestamp(currentDate.time)
        val endDayTimestamp = getEndDayTimestamp(startDayTimestamp)

        val query = healthCareEventBox.query().apply {
            link(HealthCareEvent_.sensorEventData)
                    .between(SensorEventData_.timestamp, startDayTimestamp, endDayTimestamp)
                    .equal(SensorEventData_.type, sensorType.toLong())
        }.build()

        val disposable = RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthCareEvents.postValue(it)
                }
        disposables.add(disposable)
    }

    fun refreshView() {
        initHealthCarEvents()
        initHistoryLiveData()
    }

    private fun initHistoryLiveData() {
        val startDayTimestamp = getStartDayTimestamp(currentDate.time)
        val endDayTimestamp = getEndDayTimestamp(startDayTimestamp)

        val box = boxStore.boxFor(SensorEventData::class.java)
        val query = box.query().apply {
            equal(SensorEventData_.type, sensorType.toLong())
            between(SensorEventData_.timestamp, startDayTimestamp, endDayTimestamp)
        }.build()

        var min = Float.MAX_VALUE
        var max = Float.MIN_VALUE

        var sum = 0f
        var count = 0

        var disposable: Disposable? = null
        disposable = RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .map {
                    val result = mutableListOf<Entry>()

                    it.forEach { sensorEventData ->
                        if (sensorEventData.values.isNotEmpty()) {
                            createEntry(sensorEventData, startDayTimestamp)?.let { entry ->
                                val value = sensorEventData.values[0]

                                if (value < min) {
                                    min = value
                                }
                                if (value > max) {
                                    max = value
                                }
                                sum += value
                                count++

                                result.add(entry)
                            }
                        }
                    }
                    return@map result
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    showLoadingProgressBar.postValue(true)
                    showStatisticsContainer.postValue(false)
                }
                .subscribe {
                    liveData.postValue(it)
                    showLoadingProgressBar.postValue(false)

                    showStatisticsContainer.postValue(count > 0)
                    statisticMinValue.postValue(min)
                    statisticMaxValue.postValue(max)
                    statisticAverageValue.postValue(sum / count)

                    disposable?.dispose()
                }
        disposables.add(disposable)
    }

    private fun getStartDayTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    private fun getEndDayTimestamp(startDayTimestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDayTimestamp
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return calendar.timeInMillis
    }

    private fun createEntry(sensorEventData: SensorEventData, lastMidnightTimestamp: Long): Entry? {
        var entry: Entry? = null

        if (sensorEventData.values.isNotEmpty()) {
            val timestampFromMidnight: Int = (sensorEventData.timestamp - lastMidnightTimestamp).toInt()

            entry = Entry(timestampFromMidnight.toFloat(), sensorEventData.values[0], sensorEventData.values)
        }
        return entry
    }

    fun showHealthCareEvent(healthCareEvent: HealthCareEvent) {
        healthCareEvent.sensorEventData.target?.let { sensorEventData ->
            val startDayTimestamp = getStartDayTimestamp(sensorEventData.timestamp)
            createEntry(sensorEventData, startDayTimestamp)?.let { entry ->
                entryHighlighted.postValue(entry)
            }
        }
    }

    override fun onCleared() {
        disposables.clear()
        subscriptions.cancel()
        super.onCleared()
    }
}