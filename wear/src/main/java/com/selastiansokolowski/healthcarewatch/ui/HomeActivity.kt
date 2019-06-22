package com.selastiansokolowski.healthcarewatch.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.service.MessageReceiverService
import com.selastiansokolowski.healthcarewatch.service.SensorService
import com.selastiansokolowski.healthcarewatch.viewModel.HomeViewModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 18.06.19.
 */
class HomeActivity : WearableFragmentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        setContentView(R.layout.activity_main)
        setAmbientEnabled()

        startService(Intent(this, SensorService::class.java))
        startService(Intent(this, MessageReceiverService::class.java))

        homeViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HomeViewModel::class.java)
        btn_measurement_start.setOnClickListener {
            homeViewModel.toggleMeasurementState()
        }

        homeViewModel.measurementState.observe(this, Observer {
            it?.let {
                btn_measurement_start.apply {
                    text = if (it) {
                        getString(R.string.measurement_button_stop_label)
                    } else {
                        tv_heart_rate.text = "---"
                        getString(R.string.measurement_button_start_label)
                    }
                }
            }
        })
        homeViewModel.heartRate.observe(this, Observer {
            it?.let {
                tv_heart_rate.text = it
            }
        })
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        updateDisplay()
    }

    override fun onUpdateAmbient() {
        updateDisplay()
    }

    override fun onExitAmbient() {
        updateDisplay()
    }

    private fun updateDisplay() {
        if (isAmbient()) {
            container.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            tv_heart_rate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            container.background = null
            tv_heart_rate.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
    }
}