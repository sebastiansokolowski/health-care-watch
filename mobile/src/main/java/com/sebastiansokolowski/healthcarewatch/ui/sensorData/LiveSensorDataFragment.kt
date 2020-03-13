package com.sebastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.sebastiansokolowski.healthcarewatch.viewModel.sensorData.LiveSensorDataViewModel
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class LiveSensorDataFragment : SensorDataFragment() {

    companion object {

        fun newInstance(sensorTYPE: SensorAdapterItem): LiveSensorDataFragment {
            val fragment = LiveSensorDataFragment()

            val bundle = Bundle()
            bundle.putInt(SENSOR_TYPE_KEY, sensorTYPE.ordinal)
            fragment.arguments = bundle

            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var liveSensorDataViewModel: LiveSensorDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        liveSensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(LiveSensorDataViewModel::class.java)
        sensorEventViewModel = liveSensorDataViewModel
        liveSensorDataViewModel.sensorType = sensorType.sensorId
        liveSensorDataViewModel.refreshView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChart(sensorType)
    }

    private fun initChart(sensorAdapterItem: SensorAdapterItem) {
        liveSensorDataViewModel.chartLiveData.observe(this, Observer {
            fillChart(sensorAdapterItem, it)
        })
    }

}