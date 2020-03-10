package com.sebastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sebastiansokolowski.healthcarewatch.R
import com.sebastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.sebastiansokolowski.healthcarewatch.viewModel.LiveSensorDataViewModel
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class LiveSensorDataFragment : SensorDataFragmentBase() {

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
        liveSensorDataViewModel.initLiveData(sensorType.sensorId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sensor_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        liveSensorDataViewModel.showStatisticsContainer.observe(this, Observer {
            val visibility = it ?: false

            if (visibility) {
                statistics_container.visibility = View.VISIBLE
            } else {
                statistics_container.visibility = View.INVISIBLE
            }
        })
        initChart(sensorType)
    }

    private fun initChart(sensorAdapterItem: SensorAdapterItem) {
        context?.let {
            val marker = CustomMarkerView(it, R.layout.custom_marker_view)
            marker.chartView = chart_lc
            chart_lc.marker = marker
        }
        chart_lc.setTouchEnabled(true)

        liveSensorDataViewModel.chartLiveData.observe(this, Observer {
            fillChart(sensorAdapterItem, it)
        })
    }

}