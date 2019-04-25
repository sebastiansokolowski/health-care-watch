package com.selastiansokolowski.healthcarewatch.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.viewModel.HomeViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.home_fragment.*
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HomeFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homeViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HomeViewModel::class.java)

        homeViewModel.heartRate.observe(this, Observer {
            heart_rate_tv.text = it
        })

        homeViewModel.measurementState.observe(this, Observer {
            heart_rate_tv.text = "---"
            it?.let {
                if (it) {
                    measurement_btn.text = "Stop measurement"
                } else {
                    measurement_btn.text = "Start measurement"
                }
            }
        })

        measurement_btn.setOnClickListener {
            homeViewModel.toggleMeasurementState()
        }
    }
}