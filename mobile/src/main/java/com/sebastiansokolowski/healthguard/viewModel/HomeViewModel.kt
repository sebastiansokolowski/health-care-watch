package com.sebastiansokolowski.healthguard.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import android.net.Uri
import com.sebastiansokolowski.healthguard.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthguard.db.entity.HealthCareEventEntity_
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import com.sebastiansokolowski.healthguard.model.SetupModel
import com.sebastiansokolowski.healthguard.model.ShareDataModel
import com.sebastiansokolowski.healthguard.util.SingleEvent
import com.sebastiansokolowski.healthguard.viewModel.sensorData.SensorEventViewModel
import io.objectbox.BoxStore
import io.objectbox.rx.RxQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HomeViewModel
@Inject constructor(private val setupModel: SetupModel, private val sensorDataModel: SensorDataModel, private val shareDataModel: ShareDataModel, boxStore: BoxStore) : SensorEventViewModel(boxStore) {

    init {
        refreshView()
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

    override fun getHealthCareEventsObservable(): Observable<MutableList<HealthCareEventEntity>> {
        val query = healthCareEventEntityBox.query()
                .orderDesc(HealthCareEventEntity_.__ID_PROPERTY)
                .build()

        return RxQuery.observable(query)
    }

    override fun getSensorEventsObservable(): Observable<MutableList<SensorEventEntity>>? {
        return null
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
        shareDataModel.clear()
        super.onCleared()
    }
}