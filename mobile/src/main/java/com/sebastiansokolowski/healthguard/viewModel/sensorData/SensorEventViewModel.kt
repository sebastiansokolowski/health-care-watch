package com.sebastiansokolowski.healthguard.viewModel.sensorData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.sebastiansokolowski.healthguard.dataModel.ChartData
import com.sebastiansokolowski.healthguard.dataModel.StatisticData
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.ui.adapter.HealthEventAdapter
import com.sebastiansokolowski.healthguard.util.SingleEvent
import io.objectbox.Box
import io.objectbox.BoxStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by Sebastian Sokołowski on 28.06.19.
 */
abstract class SensorEventViewModel(val boxStore: BoxStore) : ViewModel(), HealthEventAdapter.HealthEventAdapterItemListener {
    private val TAG = javaClass.canonicalName

    private val disposables = CompositeDisposable()

    val healthEventEntityBox: Box<HealthEventEntity> = boxStore.boxFor(HealthEventEntity::class.java)
    val sensorEventEntityBox: Box<SensorEventEntity> = boxStore.boxFor(SensorEventEntity::class.java)

    val healthEvents: MutableLiveData<List<HealthEventEntity>> = MutableLiveData()
    val healthEventSelected: MutableLiveData<HealthEventEntity> = MutableLiveData()
    val healthEventDetails: MutableLiveData<SingleEvent<HealthEventEntity>> = MutableLiveData()
    val healthEventToRestore: MutableLiveData<SingleEvent<HealthEventEntity>> = MutableLiveData()

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
        refreshHealthEvents()
        if (getSensorEventsObservable() != null) {
            refreshSensorEvents()
        }
    }

    //HealthEventAdapterItemListener

    override fun onClickItem(healthEventEntity: HealthEventEntity) {
        healthEventSelected.postValue(healthEventEntity)
    }

    override fun onLongClickItem(healthEventEntity: HealthEventEntity) {
        healthEventDetails.postValue(SingleEvent(healthEventEntity))
    }

    override fun onDeleteItem(healthEventEntity: HealthEventEntity) {
        healthEventEntityBox.remove(healthEventEntity)
        healthEventToRestore.postValue(SingleEvent(healthEventEntity))
        refreshHealthEvents()
    }

    //

    abstract fun getHealthEventsObservable(): Observable<MutableList<HealthEventEntity>>

    abstract fun getSensorEventsObservable(): Observable<MutableList<SensorEventEntity>>?

    private fun refreshHealthEvents() {
        val disposable = getHealthEventsObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthEvents.postValue(it)
                }
        disposables.add(disposable)
    }

    private fun refreshSensorEvents() {
        val startDayTimestamp = getStartDayTimestamp(currentDate.time)

        val disposable = getSensorEventsObservable()!!
                .subscribeOn(Schedulers.io())
                .map {
                    it.sortBy { it.timestamp }

                    val chartData = ChartData()
                    when (sensorType) {
                        Sensor.TYPE_GRAVITY,
                        Sensor.TYPE_ACCELEROMETER,
                        Sensor.TYPE_LINEAR_ACCELERATION -> {
                            parseData(it, startDayTimestamp, chartData.xData, chartData.xStatisticData, 0)
                            parseData(it, startDayTimestamp, chartData.yData, chartData.yStatisticData, 1)
                            parseData(it, startDayTimestamp, chartData.zData, chartData.zStatisticData, 2)
                        }
                        else -> {
                            parseData(it, startDayTimestamp, chartData.xData, chartData.xStatisticData, 0)
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

    private fun parseData(dataToParse: MutableList<SensorEventEntity>, startDayTimestamp: Long, chartData: MutableList<LineDataSet>, statisticData: StatisticData, index: Int) {
        val measurementDataSet = HashMap<Long, LineDataSet>()

        dataToParse.forEach { sensorEventData ->
            val lineDataSet = measurementDataSet.getOrPut(sensorEventData.measurementEventEntity.target.id) {
                LineDataSet(mutableListOf(), "")
            }
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

                lineDataSet.addEntry(entry)
            }
        }

        chartData.addAll(measurementDataSet.values)

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

    fun restoreDeletedEvent(healthEventEntity: HealthEventEntity) {
        healthEventEntityBox.put(healthEventEntity)
        refreshHealthEvents()
    }

    fun showHealthEvent(healthEventEntity: HealthEventEntity) {
        healthEventEntity.sensorEventEntity.target?.let { sensorEventData ->
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