package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import io.objectbox.BoxStore
import io.objectbox.rx.RxQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HomeViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, boxStore: BoxStore) : HealthCareEventViewModel(boxStore) {

    private val disposables = CompositeDisposable()

    init {
        initHealthCarEvents()
    }

    val measurementState: LiveData<Boolean> by lazy {
        initMeasurementStateLiveData()
    }
    val heartRate: LiveData<String> by lazy {
        initLiveData()
    }

    override fun initHealthCarEvents() {
        val query = healthCareEventBox.query().build()

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

    private fun initLiveData(): LiveData<String> {
        val sensorDataModelFlowable = sensorDataModel.heartRateObservable.toFlowable(BackpressureStrategy.LATEST)
        val sensorDataModelLiveData = LiveDataReactiveStreams.fromPublisher(sensorDataModelFlowable)
        return Transformations.map(sensorDataModelLiveData) {
            var result = ""
            it.values?.let {
                result = it[0].toString()
            }
            return@map result
        }
    }

    private fun initMeasurementStateLiveData(): LiveData<Boolean> {
        val measurementStateFlowable = sensorDataModel.measurementStateObservable.toFlowable(BackpressureStrategy.LATEST)
        return LiveDataReactiveStreams.fromPublisher(measurementStateFlowable)
    }

    fun toggleMeasurementState() {
        sensorDataModel.toggleMeasurementState()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}