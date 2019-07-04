package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent_
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData_
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

    var currentDate = Date()

    override fun initHealthCarEvents() {
        val startCalendar = Calendar.getInstance()
        startCalendar.time = currentDate
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)

        val endCalendar = Calendar.getInstance()
        endCalendar.time = startCalendar.time
        endCalendar.add(Calendar.DAY_OF_MONTH, 1)

        val query = healthCareEventBox.query().apply {
            link(HealthCareEvent_.sensorEventData)
                    .between(SensorEventData_.timestamp, startCalendar.timeInMillis, endCalendar.timeInMillis)
        }.build()

        var disposable: Disposable? = null
        disposable = RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthCareEvents.postValue(it)

                    disposable?.dispose()
                }
        disposables.add(disposable)
    }

    fun refreshView(sensorType: Int, date: Date = Date()) {
        currentDate = date

        initHealthCarEvents()
        initHistoryLiveData(sensorType)
    }

    private fun initHistoryLiveData(sensorType: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val startTimestamp = calendar.time.time
        val stopTimestamp = startTimestamp + 24 * 60 * 60 * 1000

        val box = boxStore.boxFor(SensorEventData::class.java)
        val query = box.query().build()

        var min = Float.MAX_VALUE
        var max = Float.MIN_VALUE

        var sum = 0f
        var count = 0

        var disposable: Disposable? = null
        disposable = RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .map {
                    it.filter { it.type == sensorType && it.timestamp!! in startTimestamp..stopTimestamp }
                }
                .map {
                    val result = mutableListOf<Entry>()

                    it.forEach {
                        SafeCall.safeLet(it.timestamp, it.values) { timestamp, values ->
                            val value = values[0]

                            if (value < min) {
                                min = value
                            }
                            if (value > max) {
                                max = value
                            }
                            sum += value
                            count++

                            val timestampFromMidnight: Int = (timestamp - startTimestamp).toInt()
                            result.add(Entry(timestampFromMidnight.toFloat(), value, it))
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

    override fun onCleared() {
        disposables.clear()
        subscriptions.cancel()
        super.onCleared()
    }
}