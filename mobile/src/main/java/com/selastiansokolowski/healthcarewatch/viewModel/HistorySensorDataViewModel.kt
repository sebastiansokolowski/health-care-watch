package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
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
    val entryHighlighted: MutableLiveData<Entry> = MutableLiveData()

    var currentDate = Date()
    var sensorType: Int? = null

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
            val sensorQuery = link(HealthCareEvent_.sensorEventData)
                    .between(SensorEventData_.timestamp, startCalendar.timeInMillis, endCalendar.timeInMillis)
            sensorType?.let {
                sensorQuery.equal(SensorEventData_.type, it.toLong())
            }
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

    fun refreshView() {
        initHealthCarEvents()
        initHistoryLiveData()
    }

    private fun initHistoryLiveData() {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val startTimestamp = calendar.time.time
        val stopTimestamp = startTimestamp + 24 * 60 * 60 * 1000

        val box = boxStore.boxFor(SensorEventData::class.java)
        val query = box.query().apply {
            sensorType?.let {
                equal(SensorEventData_.type, it.toLong())
            }
            between(SensorEventData_.timestamp, startTimestamp, stopTimestamp)
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

    fun showHealthCareEvent(healthCareEvent: HealthCareEvent) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val timestampFromMidnight: Int = (healthCareEvent.sensorEventData.target.timestamp!! - calendar.timeInMillis).toInt()

        entryHighlighted.postValue(Entry(timestampFromMidnight.toFloat(), healthCareEvent.sensorEventData.target!!.values!![0]))
    }

    override fun onCleared() {
        disposables.clear()
        subscriptions.cancel()
        super.onCleared()
    }
}