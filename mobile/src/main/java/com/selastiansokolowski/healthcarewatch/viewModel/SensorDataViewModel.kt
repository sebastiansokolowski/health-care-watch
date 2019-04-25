package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.hardware.Sensor
import com.github.mikephil.charting.data.Entry
import com.selastiansokolowski.healthcarewatch.db.entity.SensorEventData
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.healthcarewatch.util.SafeCall
import io.objectbox.BoxStore
import io.objectbox.android.ObjectBoxLiveData
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SensorDataViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, private val boxStore: BoxStore) : ViewModel() {

    private val disposables = CompositeDisposable()

    val heartRateLiveData: LiveData<MutableList<Entry>> by lazy {
        initLiveData(Sensor.TYPE_HEART_RATE)
    }

    val stepCounterLiveData: LiveData<MutableList<Entry>> by lazy {
        initLiveData(Sensor.TYPE_STEP_COUNTER)
    }

    val pressureLiveData: LiveData<MutableList<Entry>> by lazy {
        initLiveData(Sensor.TYPE_PRESSURE)
    }

    val gravityLiveData: LiveData<MutableList<Entry>> by lazy {
        initLiveData(Sensor.TYPE_GRAVITY)
    }

    private fun initLiveData(type: Int): LiveData<MutableList<Entry>> {
        val heartRateBox = boxStore.boxFor(SensorEventData::class.java)
        val heartRateQuery = heartRateBox.query().filter {
            it.type == type
        }.build()

        val liveData = ObjectBoxLiveData<SensorEventData>(heartRateQuery)

        return Transformations.map(liveData) {
            val result = mutableListOf<Entry>()
            it.forEach {
                SafeCall.safeLet(it.timestamp, it.values) { timestamp, values ->
                    result.add(Entry(it.id.toFloat(), values[0], it))
                }
            }

            return@map result
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}