package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.selastiansokolowski.shared.db.entity.HealthCareEvent
import com.selastiansokolowski.shared.db.entity.HealthCareEventType
import com.selastiansokolowski.shared.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.ui.adapter.HealthCareEventAdapter
import com.selastiansokolowski.healthcarewatch.util.SingleEvent
import io.objectbox.Box
import io.objectbox.BoxStore
import java.util.*

/**
 * Created by Sebastian Sokołowski on 28.06.19.
 */
abstract class HealthCareEventViewModel(val boxStore: BoxStore) : ViewModel(), HealthCareEventAdapter.HealthCareEventAdapterItemListener {
    private val TAG = javaClass.canonicalName

    val healthCareEventBox: Box<HealthCareEvent> = boxStore.boxFor(HealthCareEvent::class.java)
    val healthCareEventSelected: MutableLiveData<HealthCareEvent> = MutableLiveData()

    init {
//        addHealthCareEvents()
    }

    val healthCareEventToRestore: MutableLiveData<SingleEvent<HealthCareEvent>> = MutableLiveData()
    val healthCareEvents: MutableLiveData<List<HealthCareEvent>> = MutableLiveData()

    abstract fun initHealthCarEvents()

    fun addHealthCareEvents() {
        val healthCareEvents = mutableListOf<HealthCareEvent>()

        val box = boxStore.boxFor(SensorEventData::class.java)
        val sensorEventDataList = box.all

        for (i in 1..10) {
            val random = Random()
            val index = random.nextInt(sensorEventDataList.size)
            val healthCareEvent = HealthCareEvent().apply {
                careEvent = HealthCareEventType.HEARTH_RATE_ANOMALY
                sensorEventData.target = sensorEventDataList[index]
            }

            healthCareEvents.add(healthCareEvent)
        }

        healthCareEventBox.put(healthCareEvents)
    }

    fun restoreDeletedEvent(healthCareEvent: HealthCareEvent) {
        healthCareEventBox.put(healthCareEvent)
    }

    //HealthCareEventAdapterItemListener

    override fun onClickItem(healthCareEvent: HealthCareEvent) {
        healthCareEventSelected.postValue(healthCareEvent)
    }

    override fun onDeleteItem(healthCareEvent: HealthCareEvent) {
        healthCareEventBox.remove(healthCareEvent)
        healthCareEventToRestore.postValue(SingleEvent(healthCareEvent))
    }
}