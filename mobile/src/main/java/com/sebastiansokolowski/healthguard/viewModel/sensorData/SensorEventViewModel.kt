package com.sebastiansokolowski.healthguard.viewModel.sensorData

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.sebastiansokolowski.healthguard.dataModel.ChartData
import com.sebastiansokolowski.healthguard.dataModel.StatisticData
import com.sebastiansokolowski.healthguard.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.ui.adapter.HealthCareEventAdapter
import com.sebastiansokolowski.healthguard.util.SingleEvent
import io.objectbox.Box
import io.objectbox.BoxStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Sebastian Sokołowski on 28.06.19.
 */
abstract class SensorEventViewModel(val boxStore: BoxStore) : ViewModel(), HealthCareEventAdapter.HealthCareEventAdapterItemListener {
    private val TAG = javaClass.canonicalName

    private val disposables = CompositeDisposable()

    val healthCareEventEntityBox: Box<HealthCareEventEntity> = boxStore.boxFor(HealthCareEventEntity::class.java)
    val sensorEventEntityBox: Box<SensorEventEntity> = boxStore.boxFor(SensorEventEntity::class.java)

    val healthCareEvents: MutableLiveData<List<HealthCareEventEntity>> = MutableLiveData()
    val healthCareEventSelected: MutableLiveData<HealthCareEventEntity> = MutableLiveData()
    val healthCareEventDetails: MutableLiveData<SingleEvent<HealthCareEventEntity>> = MutableLiveData()
    val healthCareEventToRestore: MutableLiveData<SingleEvent<HealthCareEventEntity>> = MutableLiveData()

    val chartLiveData: MutableLiveData<ChartData> = MutableLiveData()
    val showLoadingProgressBar: MutableLiveData<Boolean> = MutableLiveData()
    val showStatisticsContainer: MutableLiveData<Boolean> = MutableLiveData()
    val entryHighlighted: MutableLiveData<Entry> = MutableLiveData()

    var currentDate = Date()
    var sensorType: Int = 0

    fun changeCurrentDate(date: Date) {
        currentDate = date
        entryHighlighted.postValue(null)
        refreshView()
    }

    fun refreshView() {
        initHealthCarEvents()
        if (getSensorEventsObservable() != null) {
            initSensorEvents()
        }
    }

    //HealthCareEventAdapterItemListener

    override fun onClickItem(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventSelected.postValue(healthCareEventEntity)
    }

    override fun onLongClickItem(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventDetails.postValue(SingleEvent(healthCareEventEntity))
    }

    override fun onDeleteItem(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntityBox.remove(healthCareEventEntity)
        healthCareEventToRestore.postValue(SingleEvent(healthCareEventEntity))
    }

    //

    abstract fun getHealthCareEventsObservable(): Observable<MutableList<HealthCareEventEntity>>

    abstract fun getSensorEventsObservable(): Observable<MutableList<SensorEventEntity>>?

    private fun initHealthCarEvents() {
        val disposable = getHealthCareEventsObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthCareEvents.postValue(it)
                }
        disposables.add(disposable)
    }

    private fun initSensorEvents() {
        val startDayTimestamp = getStartDayTimestamp(currentDate.time)

        val disposable = getSensorEventsObservable()!!
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

    private fun createEntry(sensorEventEntity: SensorEventEntity, lastMidnightTimestamp: Long, index: Int): Entry? {
        var entry: Entry? = null

        if (sensorEventEntity.values.isNotEmpty()) {
            val timestampFromMidnight: Int = (sensorEventEntity.timestamp - lastMidnightTimestamp).toInt()

            entry = Entry(timestampFromMidnight.toFloat(), sensorEventEntity.values[index], sensorEventEntity.values)
        }
        return entry
    }

    //

    fun getStartDayTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    fun getEndDayTimestamp(startDayTimestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDayTimestamp
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return calendar.timeInMillis
    }

    fun restoreDeletedEvent(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntityBox.put(healthCareEventEntity)
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
        super.onCleared()
    }
}