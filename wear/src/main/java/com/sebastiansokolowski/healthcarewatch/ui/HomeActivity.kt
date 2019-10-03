package com.sebastiansokolowski.healthcarewatch.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.sebastiansokolowski.healthcarewatch.R
import com.sebastiansokolowski.healthcarewatch.service.MessageReceiverService
import com.sebastiansokolowski.healthcarewatch.service.SensorService
import com.sebastiansokolowski.healthcarewatch.viewModel.HomeViewModel
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
        homeViewModel.requestPermissions(this)

        btn_measurement_start.setOnClickListener {
            homeViewModel.toggleMeasurementState()
        }

        homeViewModel.measurementState.observe(this, Observer {
            it?.let {
                setMeasurementButtonView(it)
            }
        })
        homeViewModel.heartRate.observe(this, Observer {
            it?.let {
                setHeartRateView(it)
            }
        })
    }

    private fun setMeasurementButtonView(running: Boolean) {
        btn_measurement_start.apply {
            text = if (running) {
                getString(R.string.measurement_button_stop_label)
            } else {
                setHeartRateView("---")
                getString(R.string.measurement_button_start_label)
            }
        }
    }

    private fun setHeartRateView(text: String) {
        tv_heart_rate.text = text
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        updateDisplay()
    }

    override fun onUpdateAmbient() {
        homeViewModel.measurementState.value?.let {
            setMeasurementButtonView(it)
        }
        homeViewModel.heartRate.value?.let {
            setHeartRateView(it)
        }
    }

    override fun onExitAmbient() {
        updateDisplay()
    }

    private fun updateDisplay() {
        if (isAmbient()) {
            container.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            tv_heart_rate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            tv_heart_rate.paint.isAntiAlias = false
        } else {
            container.background = null
            tv_heart_rate.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            tv_heart_rate.paint.isAntiAlias = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        homeViewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}