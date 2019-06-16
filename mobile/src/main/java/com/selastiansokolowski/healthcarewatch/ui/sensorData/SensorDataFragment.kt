package com.selastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.MutableLiveData
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
import com.selastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.selastiansokolowski.healthcarewatch.view.DateValueFormatter
import com.selastiansokolowski.healthcarewatch.viewModel.SensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class SensorDataFragment : DaggerFragment() {

    companion object {
        private const val SENSOR_TYPE_KEY = "SENSOR_TYPE_KEY"

        fun newInstance(sensorTYPE: SensorDataPageAdapter.SENSOR_TYPE): SensorDataFragment {
            val fragment = SensorDataFragment()

            val bundle = Bundle()
            bundle.putInt(SENSOR_TYPE_KEY, sensorTYPE.ordinal)
            fragment.arguments = bundle

            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var sensorDataViewModel: SensorDataViewModel
    private var sensorType: SensorDataPageAdapter.SENSOR_TYPE = SensorDataPageAdapter.SENSOR_TYPE.HEART_RATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val sensorTypeIndex = it.getInt(SENSOR_TYPE_KEY)
            sensorType = SensorDataPageAdapter.SENSOR_TYPE.values()[sensorTypeIndex]
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sensor_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sensorDataViewModel = ViewModelProviders.of(parentFragment!!, viewModelFactory)
                .get(SensorDataViewModel::class.java)

        sensorDataViewModel.showLoadingProgressBar.observe(this, Observer {
            val visibility = it ?: false

            if (visibility) {
                loading_pb.visibility = View.VISIBLE
                chart_lc.visibility = View.INVISIBLE
            } else {
                loading_pb.visibility = View.GONE
                chart_lc.visibility = View.VISIBLE
            }
        })
        initChart(sensorType)
    }


    private fun initChart(sensorType: SensorDataPageAdapter.SENSOR_TYPE) {
        val chartLiveData: MutableLiveData<MutableList<Entry>> = when (sensorType) {
            SensorDataPageAdapter.SENSOR_TYPE.HEART_RATE -> sensorDataViewModel.heartRateLiveData
            SensorDataPageAdapter.SENSOR_TYPE.STEP_COUNTER -> sensorDataViewModel.stepCounterLiveData
            SensorDataPageAdapter.SENSOR_TYPE.PRESSURE -> sensorDataViewModel.pressureLiveData
            SensorDataPageAdapter.SENSOR_TYPE.GRAVITY -> sensorDataViewModel.gravityLiveData
        }

        context?.let {
            val marker = CustomMarkerView(it, R.layout.custom_marker_view)
            marker.chartView = chart_lc
            chart_lc.marker = marker
        }
        chart_lc.setTouchEnabled(true)

        chartLiveData.observe(this, Observer {
            if (it == null || it.isEmpty()) {
                chart_lc.clear()
                return@Observer
            }

            val lineDataSet = LineDataSet(it, sensorType.title)

            chart_lc.xAxis.valueFormatter = DateValueFormatter()
            chart_lc.data = LineData(lineDataSet)
            chart_lc.setVisibleXRangeMaximum(60 * 60 * 5 * 60f)
            chart_lc.notifyDataSetChanged()
            chart_lc.invalidate()
        })
    }

}