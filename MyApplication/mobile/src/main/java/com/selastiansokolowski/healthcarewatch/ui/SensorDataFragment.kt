package com.selastiansokolowski.healthcarewatch.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.selastiansokolowski.healthcarewatch.view.DateValueFormatter
import com.selastiansokolowski.healthcarewatch.viewModel.SensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SensorDataFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var sensorDataViewModel: SensorDataViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sensor_data_fragment, container, false)
    }

    private fun initChart(view: LineChart, data: LiveData<MutableList<Entry>>) {
        context?.let {
            val marker = CustomMarkerView(it, R.layout.custom_marker_view)
            marker.chartView = view
            view.marker = marker
        }
        view.setTouchEnabled(true)

        data.observe(this, Observer {
            if (it == null || it.isEmpty()) {
                return@Observer
            }

            val lineDataSet = LineDataSet(it, "Heart rate")
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4.5f

            view.xAxis.valueFormatter = DateValueFormatter(it)
            view.data = LineData(lineDataSet)
            view.notifyDataSetChanged()
            view.invalidate()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SensorDataViewModel::class.java)

        initChart(heart_rate_chart, sensorDataViewModel.heartRateLiveData)
        initChart(step_counter_chart, sensorDataViewModel.stepCounterLiveData)
        initChart(pressure_chart, sensorDataViewModel.pressureLiveData)
        initChart(gravity_chart, sensorDataViewModel.gravityLiveData)
    }

}