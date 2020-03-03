package com.sebastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthcarewatch.ui.adapter.HealthCareEventAdapter
import com.sebastiansokolowski.healthcarewatch.util.SingleEvent
import io.objectbox.Box
import io.objectbox.BoxStore

/**
 * Created by Sebastian Sokołowski on 28.06.19.
 */
abstract class HealthCareEventViewModel(val boxStore: BoxStore) : ViewModel(), HealthCareEventAdapter.HealthCareEventAdapterItemListener {
    private val TAG = javaClass.canonicalName

    val healthCareEventEntityBox: Box<HealthCareEventEntity> = boxStore.boxFor(HealthCareEventEntity::class.java)
    val healthCareEventEntitySelected: MutableLiveData<HealthCareEventEntity> = MutableLiveData()

    val healthCareEventEntityDetails: MutableLiveData<SingleEvent<HealthCareEventEntity>> = MutableLiveData()
    val healthCareEventEntityToRestore: MutableLiveData<SingleEvent<HealthCareEventEntity>> = MutableLiveData()
    val healthCareEventsEntity: MutableLiveData<List<HealthCareEventEntity>> = MutableLiveData()

    abstract fun initHealthCarEvents()

    fun restoreDeletedEvent(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntityBox.put(healthCareEventEntity)
    }

    //HealthCareEventAdapterItemListener

    override fun onClickItem(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntitySelected.postValue(healthCareEventEntity)
    }

    override fun onLongClickItem(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntityDetails.postValue(SingleEvent(healthCareEventEntity))
    }

    override fun onDeleteItem(healthCareEventEntity: HealthCareEventEntity) {
        healthCareEventEntityBox.remove(healthCareEventEntity)
        healthCareEventEntityToRestore.postValue(SingleEvent(healthCareEventEntity))
    }
}