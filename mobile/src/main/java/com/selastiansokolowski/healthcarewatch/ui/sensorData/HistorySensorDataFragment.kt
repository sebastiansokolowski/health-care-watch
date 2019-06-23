package com.selastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.selastiansokolowski.healthcarewatch.view.DateValueFormatter
import com.selastiansokolowski.healthcarewatch.viewModel.HistoryDataViewModel
import com.selastiansokolowski.healthcarewatch.viewModel.HistorySensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class HistorySensorDataFragment : DaggerFragment() {

    companion object {
        private const val SENSOR_TYPE_KEY = "SENSOR_TYPE_KEY"

        fun newInstance(sensorAdapterItem: SensorAdapterItem): HistorySensorDataFragment {
            val fragment = HistorySensorDataFragment()

            val bundle = Bundle()
            bundle.putInt(SENSOR_TYPE_KEY, sensorAdapterItem.ordinal)
            fragment.arguments = bundle

            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var historySensorDataViewModel: HistorySensorDataViewModel
    private lateinit var historyDataViewModel: HistoryDataViewModel
    private var sensorAdapterItem: SensorAdapterItem = SensorAdapterItem.HEART_RATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val sensorTypeIndex = it.getInt(SENSOR_TYPE_KEY)
            sensorAdapterItem = SensorAdapterItem.values()[sensorTypeIndex]
        }
        historySensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HistorySensorDataViewModel::class.java)
        historyDataViewModel = ViewModelProviders.of(parentFragment!!, viewModelFactory)
                .get(HistoryDataViewModel::class.java)
        historySensorDataViewModel.initHistoryLiveData(sensorAdapterItem.sensorId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sensor_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        historyDataViewModel.currentDateLiveData.observe(this, Observer {
            it?.let {
                historySensorDataViewModel.initHistoryLiveData(sensorAdapterItem.sensorId, it)
            }
        })
        historySensorDataViewModel.showLoadingProgressBar.observe(this, Observer {
            val visibility = it ?: false

            if (visibility) {
                loading_pb.visibility = View.VISIBLE
                chart_lc.visibility = View.INVISIBLE
            } else {
                loading_pb.visibility = View.GONE
                chart_lc.visibility = View.VISIBLE
            }
        })
        historySensorDataViewModel.showStatisticsContainer.observe(this, Observer {
            val visibility = it ?: false

            if (visibility) {
                statistics_container.visibility = View.VISIBLE
            } else {
                statistics_container.visibility = View.INVISIBLE
            }
        })
        historySensorDataViewModel.statisticMinValue.observe(this, Observer {
            it?.let {
                statistic_min_tv.text = it.toString()
            }
        })
        historySensorDataViewModel.statisticMaxValue.observe(this, Observer {
            it?.let {
                statistic_max_tv.text = it.toString()
            }
        })
        historySensorDataViewModel.statisticAverageValue.observe(this, Observer {
            it?.let {
                statistic_avg_tv.text = it.toString()
            }
        })
        initChart(sensorAdapterItem)
    }


    private fun initChart(sensorAdapterItem: SensorAdapterItem) {
        context?.let {
            val marker = CustomMarkerView(it, R.layout.custom_marker_view)
            marker.chartView = chart_lc
            chart_lc.marker = marker
        }
        chart_lc.setTouchEnabled(true)

        historySensorDataViewModel.liveData.observe(this, Observer {
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