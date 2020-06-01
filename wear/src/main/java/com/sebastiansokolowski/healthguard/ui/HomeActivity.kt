package com.sebastiansokolowski.healthguard.ui

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.service.MessageReceiverService
import com.sebastiansokolowski.healthguard.service.SensorService
import com.sebastiansokolowski.healthguard.view.DataValueFormatter
import com.sebastiansokolowski.healthguard.viewModel.HomeViewModel
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


        homeViewModel = ViewModelProvider(this, viewModelFactory)
                .get(HomeViewModel::class.java)
        homeViewModel.requestPermissions(this)

        btn_measurement_start.setOnClickListener {
            homeViewModel.toggleMeasurementState()
        }
        initHeartRateLineChart()

        homeViewModel.measurementState.observe(this, Observer {
            it?.let {
                setMeasurementButtonView(it)
            }
        })
        homeViewModel.heartRate.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                setHeartRateView("---")
            } else {
                setHeartRateView(it)
            }
        })
        homeViewModel.chartData.observe(this, Observer {
            it?.let {
                val lineDataSet = LineDataSet(it, "")
                lineDataSet.setDrawCircles(true)
                lineDataSet.color = ContextCompat.getColor(this, R.color.colorPrimaryLight)
                lineDataSet.valueTextColor = ContextCompat.getColor(this, R.color.white)
                lineDataSet.valueFormatter = DataValueFormatter()

                lc_heart_rate.data = LineData(lineDataSet)
                lc_heart_rate.notifyDataSetChanged()
                lc_heart_rate.invalidate()
            }
        })
    }

    private fun initHeartRateLineChart() {
        lc_heart_rate.setNoDataText("")
        lc_heart_rate.isHighlightPerDragEnabled = false
        lc_heart_rate.isHighlightPerTapEnabled = false
        lc_heart_rate.isClickable = false
        lc_heart_rate.description.isEnabled = false
        lc_heart_rate.legend.isEnabled = false
        lc_heart_rate.axisLeft.isEnabled = false
        lc_heart_rate.axisRight.isEnabled = false
        lc_heart_rate.xAxis.isEnabled = false
    }

    private fun setMeasurementButtonView(running: Boolean) {
        btn_measurement_start.apply {
            text = if (running) {
                getString(R.string.measurement_button_stop_label)
            } else {
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
            tv_heart_rate.paint.isAntiAlias = false
            lc_heart_rate.visibility = View.INVISIBLE
        } else {
            container.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            tv_heart_rate.paint.isAntiAlias = true
            lc_heart_rate.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        homeViewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}