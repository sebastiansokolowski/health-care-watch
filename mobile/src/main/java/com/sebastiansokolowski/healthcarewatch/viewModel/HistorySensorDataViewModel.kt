package com.sebastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.sebastiansokolowski.healthcarewatch.dataModel.ChartData
import com.sebastiansokolowski.healthcarewatch.dataModel.StatisticData
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity_
import com.sebastiansokolowski.healthcarewatch.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthcarewatch.db.entity.SensorEventEntity_
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

        val query = healthCareEventEntityBox.query()
                .orderDesc(HealthCareEventEntity_.__ID_PROPERTY)
                .apply {
                    link(HealthCareEventEntity_.sensorEventEntity)
                            .between(SensorEventEntity_.timestamp, startDayTimestamp, endDayTimestamp)
                            .equal(SensorEventEntity_.type, sensorType.toLong())
                }.build()

        val disposable = RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthCareEventsEntity.postValue(it)
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

        val box = boxStore.boxFor(SensorEventEntity::class.java)
        val query = box.query().apply {
            equal(SensorEventEntity_.type, sensorType.toLong())
            between(SensorEventEntity_.timestamp, startDayTimestamp, endDayTimestamp)
        }.build()


        val disposable = RxQuery.observable(query)
                .take(1)
                .subscribeOn(Schedulers.io())
                .map {
                    val chartData = ChartData()
                    when (sensorType) {
                        Sensor.TYPE_GRAVITY,
                        Sensor.TYPE_ACCELEROMETER,
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

    private fun parseData(startDayTimestamp: Long, chartData: MutableList<Entry>, statisticData: StatisticData, list: MutableList<SensorEventEntity>, index: Int) {
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

    private fun createEntry(sensorEventEntity: SensorEventEntity, lastMidnightTimestamp: Long, index: Int): Entry? {
        var entry: Entry? = null

        if (sensorEventEntity.values.isNotEmpty()) {
            val timestampFromMidnight: Int = (sensorEventEntity.timestamp - lastMidnightTimestamp).toInt()

            entry = Entry(timestampFromMidnight.toFloat(), sensorEventEntity.values[index], sensorEventEntity.values)
        }
        return entry
    }

    fun showHealthCareEvent(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntity.sensorEventEntity.target?.let { sensorEventData ->
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