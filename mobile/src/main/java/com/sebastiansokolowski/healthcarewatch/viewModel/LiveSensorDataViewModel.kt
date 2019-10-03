package com.sebastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.sebastiansokolowski.healthcarewatch.dataModel.ChartData
import com.sebastiansokolowski.healthcarewatch.dataModel.StatisticData
import com.sebastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.sebastiansokolowski.healthcarewatch.model.SensorDataModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
class LiveSensorDataViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel) : ViewModel() {

    private val disposables = CompositeDisposable()

    val showStatisticsContainer: MutableLiveData<Boolean> = MutableLiveData()

    val chartLiveData: MutableLiveData<ChartData> = MutableLiveData()

    fun initLiveData(sensorType: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startDayTimestamp = calendar.time.time

        val disposable = sensorDataModel
                .sensorsObservable
                .subscribeOn(Schedulers.io())
                .filter { it.type == sensorType }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { sensorEventData ->
                    val chartData = chartLiveData.value ?: ChartData()

                    when (sensorType) {
                        Sensor.TYPE_GRAVITY,
                        Sensor.TYPE_LINEAR_ACCELERATION -> {
                            parseData(startDayTimestamp, chartData.xData, chartData.xStatisticData, sensorEventData, 0)
                            parseData(startDayTimestamp, chartData.yData, chartData.yStatisticData, sensorEventData, 1)
                            parseData(startDayTimestamp, chartData.zData, chartData.zStatisticData, sensorEventData, 2)
                        }
                        else -> {
                            parseData(startDayTimestamp, chartData.xData, chartData.xStatisticData, sensorEventData, 0)
                        }
                    }


                    showStatisticsContainer.postValue(true)
                    chartLiveData.postValue(chartData)
                }
        disposables.add(disposable)
    }

    private fun parseData(startDayTimestamp: Long, chartData: MutableList<Entry>, statisticData: StatisticData, sensorEventData: SensorEventData, index: Int) {
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

        statisticData.average = statisticData.sum / statisticData.count
    }

    private fun createEntry(sensorEventData: SensorEventData, lastMidnightTimestamp: Long, index: Int): Entry? {
        var entry: Entry? = null

        if (sensorEventData.values.isNotEmpty()) {
            val timestampFromMidnight: Int = (sensorEventData.timestamp - lastMidnightTimestamp).toInt()

            entry = Entry(timestampFromMidnight.toFloat(), sensorEventData.values[index], sensorEventData.values)
        }
        return entry
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}