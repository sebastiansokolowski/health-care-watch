package com.selastiansokolowski.healthcarewatch.ui

import android.app.DatePickerDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
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
import kotlinx.android.synthetic.main.sensor_data_fragment.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SensorDataFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var sensorDataViewModel: SensorDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sensor_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SensorDataViewModel::class.java)

        initChart(heart_rate_chart, sensorDataViewModel.heartRateLiveData)
        initChart(step_counter_chart, sensorDataViewModel.stepCounterLiveData)
        initChart(pressure_chart, sensorDataViewModel.pressureLiveData)
        initChart(gravity_chart, sensorDataViewModel.gravityLiveData)

        sensorDataViewModel.currentDateLiveData.observe(this, Observer {
            val dateTimeFormatter = SimpleDateFormat("yyyy/MM/dd")
            view.current_date_tv.text = dateTimeFormatter.format(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.sensor_data_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.date -> {
                showSelectDateDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
                view.clear()
                return@Observer
            }

            val lineDataSet = LineDataSet(it, "Heart rate")
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4.5f

            view.xAxis.valueFormatter = DateValueFormatter()
            view.data = LineData(lineDataSet)
            view.notifyDataSetChanged()
            view.invalidate()
        })
    }

    private fun showSelectDateDialog() {
        val listener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            sensorDataViewModel.currentDateLiveData.postValue(calendar.time)
            sensorDataViewModel.refreshCharts(calendar.time)
        }

        context?.let {
            val calendar = Calendar.getInstance()
            calendar.time = sensorDataViewModel.currentDateLiveData.value
            val dialog = DatePickerDialog(it, listener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }
    }
}