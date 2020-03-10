package com.sebastiansokolowski.healthcarewatch.ui.sensorData

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
import com.sebastiansokolowski.healthcarewatch.MainActivity
import com.sebastiansokolowski.healthcarewatch.R
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthcarewatch.ui.adapter.HealthCareEventAdapter
import com.sebastiansokolowski.healthcarewatch.ui.dialog.HealthCareEventDetailsDialogFragment
import com.sebastiansokolowski.healthcarewatch.util.SafeCall
import com.sebastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.sebastiansokolowski.healthcarewatch.viewModel.HistoryDataViewModel
import com.sebastiansokolowski.healthcarewatch.viewModel.HistorySensorDataViewModel
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class HistorySensorDataFragment : SensorDataFragmentBase() {

    companion object {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historySensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HistorySensorDataViewModel::class.java)
        historyDataViewModel = ViewModelProviders.of(parentFragment!!, viewModelFactory)
                .get(HistoryDataViewModel::class.java)
        historySensorDataViewModel.setSensorType(sensorType.sensorId)
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
        historyDataViewModel.healthCareEventEntityToShow.observe(this, Observer {
            it?.let {
                if (it.sensorEventEntity.target.type == sensorType.sensorId) {
                    historySensorDataViewModel.showHealthCareEvent(it)
                    historyDataViewModel.healthCareEventEntityToShow.postValue(null)
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
        historySensorDataViewModel.healthCareEventsEntity.observe(this, Observer {
            SafeCall.safeLet(context, it) { context, list ->
                val adapter = HealthCareEventAdapter(context, list, historySensorDataViewModel)
                adapter.setEmptyView(health_care_events_empty_view)
                health_care_events_lv.adapter = adapter
            }
        })
        historySensorDataViewModel.healthCareEventEntityDetails.observe(this, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    val mainActivity: MainActivity = activity as MainActivity
                    mainActivity.showDialog(HealthCareEventDetailsDialogFragment.newInstance(it))
                }
            }
        })
        historySensorDataViewModel.healthCareEventEntityToRestore.observe(this, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    showRestoreDeletedItemSnackBar(it)
                }
            }
        })
        historySensorDataViewModel.healthCareEventEntitySelected.observe(this, Observer {
            it?.let {
                historySensorDataViewModel.showHealthCareEvent(it)
            }
        })
        historySensorDataViewModel.entryHighlighted.observe(this, Observer {
            it?.let {
                highlightValue(it)
            }
        })
        initChart(sensorType)
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

        historySensorDataViewModel.chartLiveData.observe(this, Observer {
            fillChart(sensorAdapterItem, it)

            historySensorDataViewModel.entryHighlighted.value?.let {
                highlightValue(it)
            }
        })
    }

    private fun showRestoreDeletedItemSnackBar(healthCareEventEntity: HealthCareEventEntity) {
        view?.let {
            val snackbar = Snackbar.make(it, getString(R.string.restore_deleted_item_title), Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.action_undo)) {
                historySensorDataViewModel.restoreDeletedEvent(healthCareEventEntity)
            }
            snackbar.show()
        }
    }

}