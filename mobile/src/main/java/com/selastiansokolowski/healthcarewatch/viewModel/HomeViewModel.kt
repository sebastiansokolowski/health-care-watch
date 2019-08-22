package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.healthcarewatch.model.SetupModel
import io.objectbox.BoxStore
import io.objectbox.rx.RxQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HomeViewModel
@Inject constructor(private val setupModel: SetupModel, private val sensorDataModel: SensorDataModel, boxStore: BoxStore) : HealthCareEventViewModel(boxStore) {

    private val disposables = CompositeDisposable()

    init {
        initHealthCarEvents()
    }

    val setupState: LiveData<SetupModel.SETUP_STEP> by lazy {
        initSetupState()
    }

    val measurementState: LiveData<Boolean> by lazy {
        initMeasurementStateLiveData()
    }
    val heartRate: LiveData<String> by lazy {
        initLiveData()
    }

    override fun initHealthCarEvents() {
        val query = healthCareEventBox.query()
                .orderDesc(HealthCareEvent_.__ID_PROPERTY)
                .build()

        val disposable = RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthCareEvents.postValue(it)
                }
        disposables.add(disposable)
    }

    private fun initLiveData(): LiveData<String> {
        val sensorDataModelFlowable = sensorDataModel.heartRateObservable.toFlowable(BackpressureStrategy.LATEST)
        val sensorDataModelLiveData = LiveDataReactiveStreams.fromPublisher(sensorDataModelFlowable)
        return Transformations.map(sensorDataModelLiveData) { sensorEventData ->
            var result = ""
            if (sensorEventData.values.isNotEmpty()) {
                result = sensorEventData.values[0].toString()
            }
            return@map result
        }
    }

    private fun initSetupState(): LiveData<SetupModel.SETUP_STEP> {
        val setupStateFlowable = setupModel.setupComplete.toFlowable(BackpressureStrategy.LATEST)
        return LiveDataReactiveStreams.fromPublisher(setupStateFlowable)
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