package com.sebastiansokolowski.healthguard.viewModel.sensorData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by Sebastian Sokołowski on 28.06.19.
 */
abstract class SensorEventViewModel(val boxStore: BoxStore) : ViewModel(), HealthEventAdapter.HealthEventAdapterItemListener {
    private val TAG = javaClass.canonicalName

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

    var sensorEventsDisposable: Disposable? = null
    var healthEventsDisposable: Disposable? = null

    var currentDate = Date()
    var sensorType: Int = 0

    fun changeCurrentDate(date: Date) {
        currentDate = date
        entryHighlighted.postValue(null)
        initEventsView()
    }

    fun initEventsView() {
        initHealthEvents()
        if (getSensorEventsObservable() != null) {
            initSensorEvents()
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
        initHealthEvents()
    }

    //

    abstract fun getHealthEventsObservable(): Observable<MutableList<HealthEventEntity>>

    abstract fun getSensorEventsObservable(): Observable<MutableList<SensorEventEntity>>?

    private fun initHealthEvents() {
        healthEventsDisposable?.dispose()
        healthEventsDisposable = getHealthEventsObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthEvents.postValue(it)
                }
    }

    private fun initSensorEvents() {
        val startDayTimestamp = getStartDayTimestamp(currentDate.time)

        sensorEventsDisposable?.dispose()
        sensorEventsDisposable = getSensorEventsObservable()!!
                .subscribeOn(Schedulers.io())
                .map {
                    it.sortBy { it.timestamp }

                    val chartData = ChartData()
                    parseData(it, startDayTimestamp, chartData.xData, chartData.xStatisticData)
                    return@map chartData
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    showLoadingProgressBar.postValue(true)
                    showStatisticsContainer.postValue(false)
                }
                .doOnComplete {
                    showLoadingProgressBar.postValue(false)
                }
                .subscribe {
                    showLoadingProgressBar.postValue(false)
                    showStatisticsContainer.postValue(it.xData.size > 0)

                    chartLiveData.postValue(it)
                }
    }

    private fun parseData(dataToParse: MutableList<SensorEventEntity>, startDayTimestamp: Long, chartData: MutableList<LineDataSet>, statisticData: StatisticData) {
        val measurementDataSet = HashMap<Long, LineDataSet>()

        for (sensorEventData in dataToParse) {
            if (sensorEventData.measurementEventEntity.targetId == 0L) {
                continue
            }
            val lineDataSet = measurementDataSet.getOrPut(sensorEventData.measurementEventEntity.targetId) {
                LineDataSet(mutableListOf(), "")
            }
            createEntry(sensorEventData, startDayTimestamp)?.let { entry ->
                val value = sensorEventData.value

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

    private fun createEntry(sensorEventEntity: SensorEventEntity, lastMidnightTimestamp: Long): Entry? {
        val timestampFromMidnight: Int = (sensorEventEntity.timestamp - lastMidnightTimestamp).toInt()

        return Entry(timestampFromMidnight.toFloat(), sensorEventEntity.value)
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
        initHealthEvents()
    }

    fun showHealthEvent(healthEventEntity: HealthEventEntity) {
        healthEventEntity.sensorEventEntity.target?.let { sensorEventData ->
            val startDayTimestamp = getStartDayTimestamp(sensorEventData.timestamp)
            createEntry(sensorEventData, startDayTimestamp)?.let { entry ->
                entryHighlighted.postValue(entry)
            }
        }
    }

    override fun onCleared() {
        sensorEventsDisposable?.dispose()
        healthEventsDisposable?.dispose()
        super.onCleared()
    }
}