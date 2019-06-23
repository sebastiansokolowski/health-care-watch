package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HistoryDataViewModel
@Inject constructor() : ViewModel() {

    val currentDateLiveData: MutableLiveData<Date> = MutableLiveData()

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
}