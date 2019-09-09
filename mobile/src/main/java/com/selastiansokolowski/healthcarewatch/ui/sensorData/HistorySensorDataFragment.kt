package com.selastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.ui.adapter.HealthCareEventAdapter
import com.selastiansokolowski.healthcarewatch.util.SafeCall
import com.selastiansokolowski.healthcarewatch.util.SensorAdapterItemHelper
import com.selastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.selastiansokolowski.healthcarewatch.view.DateValueFormatter
import com.selastiansokolowski.healthcarewatch.viewModel.HistoryDataViewModel
import com.selastiansokolowski.healthcarewatch.viewModel.HistorySensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import java.util.concurrent.TimeUnit
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
        historySensorDataViewModel.setSensorType(sensorAdapterItem.sensorId)
        historySensorDataViewModel.refreshView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sensor_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        historyDataViewModel.currentDateLiveData.observe(this, Observer {
            it?.let {
                historySensorDataViewModel.setCurrentDate(it)
            }
        })
        historyDataViewModel.healthCareEventToShow.observe(this, Observer {
            it?.let {
                if (it.sensorEventData.target.type == sensorAdapterItem.sensorId) {
                    historySensorDataViewModel.showHealthCareEvent(it)
                    historyDataViewModel.healthCareEventToShow.postValue(null)
                }
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
        historySensorDataViewModel.healthCareEvents.observe(this, Observer {
            SafeCall.safeLet(context, it) { context, list ->
                val adapter = HealthCareEventAdapter(context, list, historySensorDataViewModel)
                adapter.setEmptyView(health_care_events_empty_view)
                health_care_events_lv.adapter = adapter
            }
        })
        historySensorDataViewModel.healthCareEventToRestore.observe(this, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    showRestoreDeletedItemSnackBar(it)
                }
            }
        })
        historySensorDataViewModel.healthCareEventSelected.observe(this, Observer {
            it?.let {
                historySensorDataViewModel.showHealthCareEvent(it)
            }
        })
        historySensorDataViewModel.entryHighlighted.observe(this, Observer {
            it?.let {
                highlightValue(it)
            }
        })
        initChart(sensorAdapterItem)
    }

    private fun highlightValue(entry: Entry) {
        chart_lc.data?.let { lineData ->
            lineData.dataSets?.let {
                chart_lc.centerViewToAnimated(entry.x, entry.y, YAxis.AxisDependency.LEFT, TimeUnit.SECONDS.toMillis(1))
                chart_lc.highlightValue(entry.x, entry.y, 0)
            }
        }
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

            val title = SensorAdapterItemHelper.getTitle(context, sensorAdapterItem)
            val lineDataSet = LineDataSet(it, title)

            chart_lc.xAxis.valueFormatter = DateValueFormatter()
            chart_lc.data = LineData(lineDataSet)
            chart_lc.notifyDataSetChanged()
            chart_lc.invalidate()

            historySensorDataViewModel.entryHighlighted.value?.let {
                highlightValue(it)
            }
        })
    }

    private fun showRestoreDeletedItemSnackBar(healthCareEvent: HealthCareEvent) {
        view?.let {
            val snackbar = Snackbar.make(it, getString(R.string.restore_deleted_item_title), Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.action_undo)) {
                historySensorDataViewModel.restoreDeletedEvent(healthCareEvent)
            }
            snackbar.show()
        }
    }

}