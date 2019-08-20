package com.selastiansokolowski.healthcarewatch.ui

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.ui.sensorData.LiveSensorDataPageAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.watch_data_fragment.*
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class LiveDataFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.watch_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sensor_vp.adapter = LiveSensorDataPageAdapter(context, childFragmentManager)
        sensor_data_tl.setupWithViewPager(sensor_vp)
    }

}