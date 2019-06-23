package com.selastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.selastiansokolowski.healthcarewatch.view.DateValueFormatter
import com.selastiansokolowski.healthcarewatch.viewModel.LiveSensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class LiveSensorDataFragment : DaggerFragment() {

    companion object {
        private const val SENSOR_TYPE_KEY = "SENSOR_TYPE_KEY"

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
    private var sensorType: SensorAdapterItem = SensorAdapterItem.HEART_RATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val sensorTypeIndex = it.getInt(SENSOR_TYPE_KEY)
            sensorType = SensorAdapterItem.values()[sensorTypeIndex]
        }

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
        liveSensorDataViewModel.statisticMinValue.observe(this, Observer {
            it?.let {
                statistic_min_tv.text = it.toString()
            }
        })
        liveSensorDataViewModel.statisticMaxValue.observe(this, Observer {
            it?.let {
                statistic_max_tv.text = it.toString()
            }
        })
        liveSensorDataViewModel.statisticAverageValue.observe(this, Observer {
            it?.let {
                statistic_avg_tv.text = it.toString()
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

        liveSensorDataViewModel.liveData.observe(this, Observer {
            if (it == null || it.isEmpty()) {
                chart_lc.clear()
                return@Observer
            }

            val lineDataSet = LineDataSet(it, sensorAdapterItem.title)

            chart_lc.xAxis.valueFormatter = DateValueFormatter()
            chart_lc.data = LineData(lineDataSet)
            chart_lc.setVisibleXRangeMaximum(60 * 60 * 5 * 60f)
            chart_lc.notifyDataSetChanged()
            chart_lc.invalidate()
        })
    }

}