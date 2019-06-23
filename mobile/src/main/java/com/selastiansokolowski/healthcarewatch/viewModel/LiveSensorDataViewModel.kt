package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.healthcarewatch.util.SafeCall
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
    val statisticMinValue: MutableLiveData<Float> = MutableLiveData()
    val statisticMaxValue: MutableLiveData<Float> = MutableLiveData()
    val statisticAverageValue: MutableLiveData<Float> = MutableLiveData()

    val liveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()

    fun initLiveData(sensorType: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTimestamp = calendar.time.time

        var min = Float.MAX_VALUE
        var max = Float.MIN_VALUE

        var sum = 0f
        var count = 0

        val disposable = sensorDataModel
                .sensorsObservable
                .subscribeOn(Schedulers.io())
                .filter { it.type == sensorType }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    SafeCall.safeLet(it.type, it.timestamp, it.values) { type, timestamp, values ->
                        if (values.isEmpty()) {
                            return@safeLet
                        }
                        val value = values[0]

                        val timestampFromMidnight: Int = (timestamp - startTimestamp).toInt()
                        val entry = Entry(timestampFromMidnight.toFloat(), value, it)

                        postValueToLiveData(entry)

                        if (value < min) {
                            min = value
                        }
                        if (value > max) {
                            max = value
                        }
                        sum += value
                        count++

                        showStatisticsContainer.postValue(count > 0)
                        statisticMinValue.postValue(min)
                        statisticMaxValue.postValue(max)
                        statisticAverageValue.postValue(sum / count)

                    }
                }
        disposables.add(disposable)
    }

    private fun postValueToLiveData(entry: Entry) {
        val value: MutableList<Entry> = liveData.value ?: mutableListOf()
        value.add(entry)
        liveData.postValue(value)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}