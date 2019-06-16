package com.selastiansokolowski.healthcarewatch.ui

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.ui.sensorData.SensorDataPageAdapter
import com.selastiansokolowski.healthcarewatch.viewModel.SensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.watch_data_fragment.*
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class WatchDataFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var sensorDataViewModel: SensorDataViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.watch_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SensorDataViewModel::class.java)

        sensor_vp.adapter = SensorDataPageAdapter(childFragmentManager)
        sensor_data_tl.setupWithViewPager(sensor_vp)

        sensorDataViewModel.initLiveData()
    }

}