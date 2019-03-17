package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.*
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import io.reactivex.BackpressureStrategy
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HomeViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel) : ViewModel() {

    val measurementState: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val heartRate: LiveData<String> by lazy {
        initLiveData()
    }

    private fun initLiveData(): LiveData<String> {
        val sensorDataModelFlowable = sensorDataModel.heartRateObserver.toFlowable(BackpressureStrategy.LATEST)
        val sensorDataModelLiveData = LiveDataReactiveStreams.fromPublisher(sensorDataModelFlowable)
        return Transformations.map(sensorDataModelLiveData) {
            var result = ""
            it.values?.let {
                result = it[0].toString()
            }
            return@map result
        }
    }

    fun toggleMeasurementState() {
        val lastState = measurementState.value ?: false

        measurementState.value = !lastState


    }
}