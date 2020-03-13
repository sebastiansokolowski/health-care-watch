package com.sebastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.sebastiansokolowski.healthcarewatch.viewModel.HistoryDataViewModel
import com.sebastiansokolowski.healthcarewatch.viewModel.sensorData.HistorySensorDataViewModel
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class HistorySensorDataFragment : SensorDataFragment() {

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
        sensorEventViewModel = historySensorDataViewModel
        historyDataViewModel = ViewModelProviders.of(parentFragment!!, viewModelFactory)
                .get(HistoryDataViewModel::class.java)
        historySensorDataViewModel.sensorType = sensorType.sensorId
        historySensorDataViewModel.refreshView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyDataViewModel.currentDateLiveData.observe(this, Observer {
            it?.let {
                historySensorDataViewModel.changeCurrentDate(it)
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
        initChart(sensorType)
    }

    private fun initChart(sensorAdapterItem: SensorAdapterItem) {
        historySensorDataViewModel.chartLiveData.observe(this, Observer {
            fillChart(sensorAdapterItem, it)

            historySensorDataViewModel.entryHighlighted.value?.let {
                highlightValue(it)
            }
        })
    }

}