package com.sebastiansokolowski.healthguard.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sebastiansokolowski.healthguard.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthguard.ui.sensorData.SensorAdapterItem
import com.sebastiansokolowski.healthguard.util.SingleEvent
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HistoryDataViewModel
@Inject constructor() : ViewModel() {

    val currentDateLiveData: MutableLiveData<Date> = MutableLiveData()
    val viewPagerToShow: MutableLiveData<SingleEvent<Int>> = MutableLiveData()
    val healthCareEventEntityToShow: MutableLiveData<HealthCareEventEntity> = MutableLiveData()

    init {
        val currentDate = Date()
        currentDateLiveData.postValue(currentDate)
    }

    private fun changeCurrentDateDay(amount: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDateLiveData.value
        calendar.add(Calendar.DAY_OF_MONTH, amount)

        currentDateLiveData.postValue(calendar.time)
    }

    fun decreaseCurrentDate() {
        changeCurrentDateDay(-1)
    }

    fun increaseCurrentDate() {
        changeCurrentDateDay(1)
    }

    fun showHealthCareEvent(healthCareEventEntity: HealthCareEventEntity) {
        val sensorAdapterItem = SensorAdapterItem.values()
                .find { it.sensorId == healthCareEventEntity.sensorEventEntity.target?.type }

        sensorAdapterItem?.let {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = healthCareEventEntity.sensorEventEntity.target.timestamp

            currentDateLiveData.postValue(calendar.time)
            viewPagerToShow.postValue(SingleEvent(sensorAdapterItem.ordinal))
            healthCareEventEntityToShow.postValue(healthCareEventEntity)
        }
    }
}