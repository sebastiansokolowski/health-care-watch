package com.sebastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import android.net.Uri
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity_
import com.sebastiansokolowski.healthcarewatch.model.SensorDataModel
import com.sebastiansokolowski.healthcarewatch.model.SetupModel
import com.sebastiansokolowski.healthcarewatch.model.ShareDataModel
import com.sebastiansokolowski.healthcarewatch.util.SingleEvent
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
@Inject constructor(private val setupModel: SetupModel, private val sensorDataModel: SensorDataModel, private val shareDataModel: ShareDataModel, boxStore: BoxStore) : HealthCareEventViewModel(boxStore) {

    private val disposables = CompositeDisposable()


    init {
        initHealthCarEvents()
    }

    val setupState: LiveData<SetupModel.SetupStep> by lazy {
        initSetupState()
    }

    val measurementState: LiveData<Boolean> by lazy {
        initMeasurementStateLiveData()
    }
    val heartRate: LiveData<String> by lazy {
        initHeartRateLiveData()
    }

    val fileToShare: LiveData<SingleEvent<Uri>> by lazy {
        initFileToShareLiveData()
    }

    override fun initHealthCarEvents() {
        val query = healthCareEventEntityBox.query()
                .orderDesc(HealthCareEventEntity_.__ID_PROPERTY)
                .build()

        val disposable = RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthCareEventsEntity.postValue(it)
                }
        disposables.add(disposable)
    }

    private fun initHeartRateLiveData(): LiveData<String> {
        val sensorDataModelFlowable = sensorDataModel.heartRateObservable.toFlowable(BackpressureStrategy.LATEST)
        val sensorDataModelLiveData = LiveDataReactiveStreams.fromPublisher(sensorDataModelFlowable)
        return Transformations.map(sensorDataModelLiveData) { sensorEventData ->
            var result = ""
            if (sensorEventData.values.isNotEmpty() && sensorDataModel.measurementRunning) {
                result = sensorEventData.values[0].toString()
            }
            return@map result
        }
    }

    private fun initSetupState(): LiveData<SetupModel.SetupStep> {
        val setupStateFlowable = setupModel.setupComplete.toFlowable(BackpressureStrategy.LATEST)
        return LiveDataReactiveStreams.fromPublisher(setupStateFlowable)
    }

    private fun initMeasurementStateLiveData(): LiveData<Boolean> {
        val measurementStateFlowable = sensorDataModel.measurementStateObservable.toFlowable(BackpressureStrategy.LATEST)
        return LiveDataReactiveStreams.fromPublisher(measurementStateFlowable)
    }

    private fun initFileToShareLiveData(): LiveData<SingleEvent<Uri>> {
        val fileToShareFlowable = shareDataModel.fileToShareObservable.toFlowable(BackpressureStrategy.LATEST)
        return LiveDataReactiveStreams.fromPublisher(fileToShareFlowable)
    }

    fun shareMeasurementData() {
        shareDataModel.shareMeasurementData()
    }

    fun toggleMeasurementState() {
        sensorDataModel.toggleMeasurementState()
    }

    override fun onCleared() {
        disposables.clear()
        shareDataModel.clear()
        super.onCleared()
    }
}