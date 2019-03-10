package com.selastiansokolowski.healthcarewatch.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.view.DateValueFormatter
import com.selastiansokolowski.healthcarewatch.viewModel.SensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import java.time.format.DateTimeFormatter
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SensorDataViewModel::class.java)

        heart_rate_chart.xAxis.valueFormatter = DateValueFormatter()
        step_counter_chart.xAxis.valueFormatter = DateValueFormatter()
        pressure_chart.xAxis.valueFormatter = DateValueFormatter()
        gravity_chart.xAxis.valueFormatter = DateValueFormatter()

        sensorDataViewModel.heartRateEntries.observe(this, Observer {
            val values = mutableListOf<Entry>()

            it?.let {
                values.addAll(it)
            }

            val lineDataSet = LineDataSet(values, "Heart rate")
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4.5f

            heart_rate_chart.data = LineData(lineDataSet)
            heart_rate_chart.notifyDataSetChanged()
            heart_rate_chart.invalidate()
        })
        sensorDataViewModel.stepCounterEntries.observe(this, Observer {
            val values = mutableListOf<Entry>()

            it?.let {
                values.addAll(it)
            }

            val lineDataSet = LineDataSet(values, "Step counter")
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4.5f

            step_counter_chart.data = LineData(lineDataSet)
            step_counter_chart.notifyDataSetChanged()
            step_counter_chart.invalidate()
        })
        sensorDataViewModel.pressureEntries.observe(this, Observer {
            val values = mutableListOf<Entry>()

            it?.let {
                values.addAll(it)
            }

            val lineDataSet = LineDataSet(values, "Pressure")
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4.5f

            pressure_chart.data = LineData(lineDataSet)
            pressure_chart.notifyDataSetChanged()
            pressure_chart.invalidate()
        })
        sensorDataViewModel.gravityEntries.observe(this, Observer {
            val values = mutableListOf<Entry>()

            it?.let {
                values.addAll(it)
            }

            val lineDataSet = LineDataSet(values, "Gravity")
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4.5f

            gravity_chart.data = LineData(lineDataSet)
            gravity_chart.notifyDataSetChanged()
            gravity_chart.invalidate()
        })
    }
}