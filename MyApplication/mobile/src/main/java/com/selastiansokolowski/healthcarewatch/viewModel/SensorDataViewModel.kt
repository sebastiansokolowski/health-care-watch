package com.selastiansokolowski.healthcarewatch.viewModel

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SensorDataViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel) : ViewModel() {

    private val disposables = CompositeDisposable()

    val heartRateEntries = MutableLiveData<ArrayList<Entry>>()
    val stepCounterEntries = MutableLiveData<ArrayList<Entry>>()
    val pressureEntries = MutableLiveData<ArrayList<Entry>>()
    val gravityEntries = MutableLiveData<ArrayList<Entry>>()

    init {
        processHeartRateData()
    }

    @SuppressLint("CheckResult")
    private fun processHeartRateData() {
        sensorDataModel.observableSensorEventData
                .subscribe {

                    when (it.type) {
                        Sensor.TYPE_HEART_RATE -> {
                            val list = heartRateEntries.value ?: ArrayList()
                            val entry = Entry(it.timestamp.toFloat(), it.values[0].toInt().toFloat())
                            list.add(entry)
                            heartRateEntries.value = list
                        }
                        Sensor.TYPE_STEP_COUNTER -> {
                            val list = stepCounterEntries.value ?: ArrayList()
                            val entry = Entry(it.timestamp.toFloat(), it.values[0])
                            list.add(entry)
                            stepCounterEntries.value = list
                        }
                        Sensor.TYPE_PRESSURE -> {
                            val list = pressureEntries.value ?: ArrayList()
                            val entry = Entry(it.timestamp.toFloat(), it.values[0])
                            list.add(entry)
                            pressureEntries.value = list
                        }
                        Sensor.TYPE_GRAVITY -> {
                            val list = gravityEntries.value ?: ArrayList()
                            val entry = Entry(it.timestamp.toFloat(), it.values[0])
                            list.add(entry)
                            gravityEntries.value = list
                        }
                    }
                }
                .let {
                    disposables.add(it)
                }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}