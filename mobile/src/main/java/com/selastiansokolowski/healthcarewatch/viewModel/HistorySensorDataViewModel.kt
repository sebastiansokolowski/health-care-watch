package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.dataModel.ChartData
import com.selastiansokolowski.healthcarewatch.dataModel.StatisticData
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent_
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData_
import io.objectbox.BoxStore
import io.objectbox.reactive.DataSubscriptionList
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    val chartLiveData: MutableLiveData<ChartData> = MutableLiveData()

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

        val query = healthCareEventBox.query()
                .orderDesc(HealthCareEvent_.__ID_PROPERTY)
                .apply {
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


        val disposable = RxQuery.observable(query)
                .take(1)
                .subscribeOn(Schedulers.io())
                .map {
                    val chartData = ChartData()
                    when (sensorType) {
                        Sensor.TYPE_GRAVITY,
                        Sensor.TYPE_LINEAR_ACCELERATION -> {
                            parseData(startDayTimestamp, chartData.xData, chartData.xStatisticData, it, 0)
                            parseData(startDayTimestamp, chartData.yData, chartData.yStatisticData, it, 1)
                            parseData(startDayTimestamp, chartData.zData, chartData.zStatisticData, it, 2)
                        }
                        else -> {
                            parseData(startDayTimestamp, chartData.xData, chartData.xStatisticData, it, 0)
                        }
                    }
                    return@map chartData
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    showLoadingProgressBar.postValue(true)
                    showStatisticsContainer.postValue(false)
                }
                .subscribe {
                    showLoadingProgressBar.postValue(false)
                    showStatisticsContainer.postValue(it.xData.size > 0)

                    chartLiveData.postValue(it)
                }
        disposables.add(disposable)
    }

    private fun parseData(startDayTimestamp: Long, chartData: MutableList<Entry>, statisticData: StatisticData, list: MutableList<SensorEventData>, index: Int) {
        list.forEach { sensorEventData ->
            createEntry(sensorEventData, startDayTimestamp, index)?.let { entry ->
                val value = sensorEventData.values[index]

                if (value < statisticData.min) {
                    statisticData.min = value
                }
                if (value > statisticData.max) {
                    statisticData.max = value
                }
                statisticData.sum += value
                statisticData.count++

                chartData.add(entry)
            }
        }

        statisticData.average = statisticData.sum / statisticData.count
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

    private fun createEntry(sensorEventData: SensorEventData, lastMidnightTimestamp: Long, index: Int): Entry? {
        var entry: Entry? = null

        if (sensorEventData.values.isNotEmpty()) {
            val timestampFromMidnight: Int = (sensorEventData.timestamp - lastMidnightTimestamp).toInt()

            entry = Entry(timestampFromMidnight.toFloat(), sensorEventData.values[index], sensorEventData.values)
        }
        return entry
    }

    fun showHealthCareEvent(healthCareEvent: HealthCareEvent) {
        healthCareEvent.sensorEventData.target?.let { sensorEventData ->
            val startDayTimestamp = getStartDayTimestamp(sensorEventData.timestamp)
            createEntry(sensorEventData, startDayTimestamp, 0)?.let { entry ->
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