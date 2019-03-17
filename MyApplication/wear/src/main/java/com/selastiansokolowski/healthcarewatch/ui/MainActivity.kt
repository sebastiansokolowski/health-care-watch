package com.selastiansokolowski.healthcarewatch.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.wearable.activity.WearableActivity
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.service.MessageReceiverService
import com.selastiansokolowski.healthcarewatch.service.SensorService
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : WearableActivity(), MainView {

    @Inject
    lateinit var mainPresenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)
        setAmbientEnabled()

        startService(Intent(this, SensorService::class.java))
        startService(Intent(this, MessageReceiverService::class.java))


        btn_measurement_start.setOnClickListener {
            mainPresenter.toggleMeasurementState()
        }
    }

    override fun onResume() {
        super.onResume()
        mainPresenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        mainPresenter.onPause()
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
        updateDisplay()
    }

    override fun onUpdateAmbient() {
        super.onUpdateAmbient()
        updateDisplay()
    }

    override fun onExitAmbient() {
        super.onExitAmbient()
        updateDisplay()
    }

    private fun updateDisplay() {
        if (isAmbient) {
            container.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            tv_heart_rate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            container.background = null
            tv_heart_rate.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
    }

    override fun setMeasurementState(running: Boolean) {
        btn_measurement_start.apply {
            text = if (running) {
                getString(R.string.measurement_button_stop_label)
            } else {
                getString(R.string.measurement_button_start_label)
            }
        }
    }

    override fun setHearthRate(heartRate: String) {
        tv_heart_rate.text = heartRate
    }

}
