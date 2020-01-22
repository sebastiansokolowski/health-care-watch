package com.sebastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.shared.dataModel.HealthCareEventType
import com.sebastiansokolowski.healthcarewatch.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthcarewatch.ui.adapter.HealthCareEventAdapter
import com.sebastiansokolowski.healthcarewatch.util.SingleEvent
import io.objectbox.Box
import io.objectbox.BoxStore
import java.util.*

/**
 * Created by Sebastian Sokołowski on 28.06.19.
 */
abstract class HealthCareEventViewModel(val boxStore: BoxStore) : ViewModel(), HealthCareEventAdapter.HealthCareEventAdapterItemListener {
    private val TAG = javaClass.canonicalName

    val healthCareEventEntityBox: Box<HealthCareEventEntity> = boxStore.boxFor(HealthCareEventEntity::class.java)
    val healthCareEventEntitySelected: MutableLiveData<HealthCareEventEntity> = MutableLiveData()

    init {
//        addHealthCareEvents()
    }

    val healthCareEventEntityToRestore: MutableLiveData<SingleEvent<HealthCareEventEntity>> = MutableLiveData()
    val healthCareEventsEntity: MutableLiveData<List<HealthCareEventEntity>> = MutableLiveData()

    abstract fun initHealthCarEvents()

    fun addHealthCareEvents() {
        val healthCareEvents = mutableListOf<HealthCareEventEntity>()

        val box = boxStore.boxFor(SensorEventEntity::class.java)
        val sensorEventDataList = box.all

        for (i in 1..10) {
            val random = Random()
            val index = random.nextInt(sensorEventDataList.size)
            val healthCareEvent = HealthCareEventEntity().apply {
                careEvent = HealthCareEventType.HEARTH_RATE_ANOMALY
                sensorEventEntity.target = sensorEventDataList[index]
            }

            healthCareEvents.add(healthCareEvent)
        }

        healthCareEventEntityBox.put(healthCareEvents)
    }

    fun restoreDeletedEvent(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntityBox.put(healthCareEventEntity)
    }

    //HealthCareEventAdapterItemListener

    override fun onClickItem(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntitySelected.postValue(healthCareEventEntity)
    }

    override fun onDeleteItem(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntityBox.remove(healthCareEventEntity)
        healthCareEventEntityToRestore.postValue(SingleEvent(healthCareEventEntity))
    }
}