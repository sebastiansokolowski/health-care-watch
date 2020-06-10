package com.sebastiansokolowski.healthguard.ui.sensorData

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sebastiansokolowski.healthguard.viewModel.HistoryDataViewModel
import com.sebastiansokolowski.healthguard.viewModel.sensorData.HistorySensorDataViewModel
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

        historySensorDataViewModel = ViewModelProvider(this, viewModelFactory)
                .get(HistorySensorDataViewModel::class.java)
        sensorEventViewModel = historySensorDataViewModel
        historyDataViewModel = ViewModelProvider(requireParentFragment(), viewModelFactory)
                .get(HistoryDataViewModel::class.java)
        historySensorDataViewModel.sensorType = sensorType.sensorId
        historySensorDataViewModel.initEventsView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyDataViewModel.currentDateLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                historySensorDataViewModel.changeCurrentDate(it)
            }
        })
        historyDataViewModel.healthEventEntityToShow.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.sensorEventEntity.target.type == sensorType.sensorId) {
                    historySensorDataViewModel.showHealthEvent(it)
                    historyDataViewModel.healthEventEntityToShow.postValue(null)
                }
            }
        })
        initChart(sensorType)
    }

    private fun initChart(sensorAdapterItem: SensorAdapterItem) {
        historySensorDataViewModel.chartLiveData.observe(viewLifecycleOwner, Observer {
            fillChart(sensorAdapterItem, it)

            historySensorDataViewModel.entryHighlighted.value?.let {
                highlightValue(it)
            }
        })
    }

}