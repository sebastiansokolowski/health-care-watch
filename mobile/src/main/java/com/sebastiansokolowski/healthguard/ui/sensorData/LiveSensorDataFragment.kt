package com.sebastiansokolowski.healthguard.ui.sensorData

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.viewModel.sensorData.LiveSensorDataViewModel
import kotlinx.android.synthetic.main.sensor_data_fragment.*
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

        liveSensorDataViewModel = ViewModelProvider(this, viewModelFactory)
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
        chart_lc.setNoDataText(getString(R.string.sensor_data_chart_live_no_data))
        liveSensorDataViewModel.chartLiveData.observe(viewLifecycleOwner, Observer {
            fillChart(sensorAdapterItem, it)
        })
    }

}