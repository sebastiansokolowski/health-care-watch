package com.selastiansokolowski.healthcarewatch.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.selastiansokolowski.healthcarewatch.BasePresenter
import com.selastiansokolowski.healthcarewatch.service.SensorService

/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
class MainPresenter(private val context: Context, private val mainView: MainView) : BasePresenter, SensorService.HearthRateChangeListener {

    private var mShouldUnbind: Boolean = false

    private var mBoundService: SensorService? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mBoundService = (service as SensorService.LocalBinder).service
            mBoundService?.hearthRateChangeListener = this@MainPresenter
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mBoundService = null
            mBoundService?.hearthRateChangeListener = null
        }
    }

    private fun doBindService() {
        if (context.bindService(Intent(context, SensorService::class.java),
                        mConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true
        }
    }

    private fun doUnbindService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            context.unbindService(mConnection)
            mShouldUnbind = false
        }
    }

    override fun onResume() {
        doBindService()
    }

    override fun onPause() {
        doUnbindService()
        mBoundService?.hearthRateChangeListener = null
    }

    fun toggleMeasurementState() {
        mBoundService?.apply {
            toggleMeasurementState()
            mainView.setMeasurementState(measurementRunning)
            if (!measurementRunning) {
                mainView.setHearthRate("")
            }
        }
    }

    override fun hearthRateChangeListener(hearthRate: String) {
        mainView.setHearthRate(hearthRate)
    }
}