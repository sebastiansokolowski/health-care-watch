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
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SensorDataViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, private val boxStore: BoxStore) : ViewModel() {

    private val disposables = CompositeDisposable()

    val currentDateLiveData: MutableLiveData<Date> = MutableLiveData()

    val heartRateLiveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()
    val stepCounterLiveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()
    val pressureLiveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()
    val gravityLiveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()

    init {
        val currentDate = Date()
        currentDateLiveData.postValue(currentDate)

        refreshCharts(currentDate)
    }

    fun refreshCharts(date: Date) {
        initChart(heartRateLiveData, Sensor.TYPE_HEART_RATE, date)
        initChart(stepCounterLiveData, Sensor.TYPE_STEP_COUNTER, date)
        initChart(pressureLiveData, Sensor.TYPE_PRESSURE, date)
        initChart(gravityLiveData, Sensor.TYPE_GRAVITY, date)
    }

    private fun initChart(liveData: MutableLiveData<MutableList<Entry>>, type: Int, date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val startTimestamp = calendar.time.time
        val stopTimestamp = startTimestamp + 24 * 60 * 60 * 1000

        val heartRateBox = boxStore.boxFor(SensorEventData::class.java)
        val heartRateQuery = heartRateBox.query().filter {
            it.type == type && it.timestamp!! in startTimestamp..stopTimestamp
        }.build()

        heartRateQuery.subscribe()
                .on(AndroidScheduler.mainThread())
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
                }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}