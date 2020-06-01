package com.sebastiansokolowski.healthguard.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
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
    val healthEventEntityToShow: MutableLiveData<HealthEventEntity> = MutableLiveData()

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

    fun showHealthEvent(healthEventEntity: HealthEventEntity) {
        val sensorAdapterItem = SensorAdapterItem.values()
                .find { it.sensorId == healthEventEntity.sensorEventEntity.target?.type }

        sensorAdapterItem?.let {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = healthEventEntity.sensorEventEntity.target.timestamp

            currentDateLiveData.postValue(calendar.time)
            viewPagerToShow.postValue(SingleEvent(sensorAdapterItem.ordinal))
            healthEventEntityToShow.postValue(healthEventEntity)
        }
    }
}