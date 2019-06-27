package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.*
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEventType
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.healthcarewatch.ui.adapter.HealthCareEventAdapter
import io.objectbox.BoxStore
import io.objectbox.android.ObjectBoxLiveData
import io.reactivex.BackpressureStrategy
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HomeViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, private val boxStore: BoxStore) : ViewModel(), HealthCareEventAdapter.HealthCareEventAdapterItemListener {

    private val healthCareEventBox = boxStore.boxFor(HealthCareEvent::class.java)

    init {
//        addHealthCareEvents()
    }

    val measurementState: LiveData<Boolean> by lazy {
        initMeasurementStateLiveData()
    }

    val heartRate: LiveData<String> by lazy {
        initLiveData()
    }

    val healthCareEventToRestore: MutableLiveData<HealthCareEvent> = MutableLiveData()

    val healthCareEvents: ObjectBoxLiveData<HealthCareEvent> by lazy {
        initHealthCarEvents()
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

    private fun initHealthCarEvents(): ObjectBoxLiveData<HealthCareEvent> {
        val query = healthCareEventBox.query().build()

        return ObjectBoxLiveData<HealthCareEvent>(query)
    }

    private fun initMeasurementStateLiveData(): LiveData<Boolean> {
        val measurementStateFlowable = sensorDataModel.measurementStateObservable.toFlowable(BackpressureStrategy.LATEST)
        return LiveDataReactiveStreams.fromPublisher(measurementStateFlowable)
    }

    fun toggleMeasurementState() {
        sensorDataModel.toggleMeasurementState()
    }

    fun addHealthCareEvents() {
        val healthCareEvents = mutableListOf<HealthCareEvent>()

        val box = boxStore.boxFor(SensorEventData::class.java)
        val sensorEventDataList = box.all

        for (i in 1..5) {
            val healthCareEvent = HealthCareEvent().apply {
                careEvent = HealthCareEventType.HEARTH_RATE_ANOMALY
                sensorEventData.target = sensorEventDataList[i]
            }

            healthCareEvents.add(healthCareEvent)
        }

        healthCareEventBox.put(healthCareEvents)
    }

    override fun onDeleteItem(healthCareEvent: HealthCareEvent) {
        healthCareEventBox.remove(healthCareEvent)
        healthCareEventToRestore.postValue(healthCareEvent)
    }

    fun restoreDeletedEvent(healthCareEvent: HealthCareEvent) {
        healthCareEventBox.put(healthCareEvent)
    }
}