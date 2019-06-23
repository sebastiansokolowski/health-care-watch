package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
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
@Inject constructor(private val boxStore: BoxStore) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val subscriptions = DataSubscriptionList()

    val showLoadingProgressBar: MutableLiveData<Boolean> = MutableLiveData()

    val showStatisticsContainer: MutableLiveData<Boolean> = MutableLiveData()
    val statisticMinValue: MutableLiveData<Float> = MutableLiveData()
    val statisticMaxValue: MutableLiveData<Float> = MutableLiveData()
    val statisticAverageValue: MutableLiveData<Float> = MutableLiveData()

    val liveData: MutableLiveData<MutableList<Entry>> = MutableLiveData()

    fun initHistoryLiveData(sensorType: Int, date: Date = Date()) {
        val calendar = Calendar.getInstance()
        calendar.time = date
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
        super.onCleared()
        disposables.clear()
        subscriptions.cancel()
    }
}