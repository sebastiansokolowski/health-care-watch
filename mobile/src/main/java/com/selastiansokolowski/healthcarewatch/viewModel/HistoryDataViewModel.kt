package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.ui.sensorData.SensorAdapterItem
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HistoryDataViewModel
@Inject constructor() : ViewModel() {

    val currentDateLiveData: MutableLiveData<Date> = MutableLiveData()
    val viewPagerToShow: MutableLiveData<Int> = MutableLiveData()
    val healthCareEventToShow: MutableLiveData<HealthCareEvent> = MutableLiveData()

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

    fun showHealthCareEvent(healthCareEvent: HealthCareEvent) {
        val sensorAdapterItem = SensorAdapterItem.values()
                .find { it.sensorId == healthCareEvent.sensorEventData.target?.type }

        sensorAdapterItem?.let {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = healthCareEvent.sensorEventData.target.timestamp

            currentDateLiveData.postValue(calendar.time)
            viewPagerToShow.postValue(sensorAdapterItem.ordinal)
            healthCareEventToShow.postValue(healthCareEvent)
        }
    }
}