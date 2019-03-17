package com.selastiansokolowski.healthcarewatch.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.selastiansokolowski.shared.DataClientPaths

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class MessageReceiverService : WearableListenerService() {

    private var mShouldUnbind: Boolean = false

    private var mBoundService: SensorService? = null

    override fun onCreate() {
        super.onCreate()
        doBindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mBoundService = (service as SensorService.LocalBinder).service
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mBoundService = null
        }
    }

    private fun doBindService() {
        if (application.bindService(Intent(application, SensorService::class.java),
                        mConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true
        }
    }

    private fun doUnbindService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            application.unbindService(mConnection)
            mShouldUnbind = false
        }
    }

    override fun onDataChanged(p0: DataEventBuffer?) {
        super.onDataChanged(p0)
    }

    override fun onMessageReceived(event: MessageEvent?) {
        when (event?.path) {
            DataClientPaths.START_MEASUREMENT -> {
                mBoundService?.let {
                    if (!it.measurementRunning) {
                        it.toggleMeasurementState()
                    }
                }
            }
            DataClientPaths.STOP_MEASUREMENT -> {
                mBoundService?.let {
                    if (it.measurementRunning) {
                        it.toggleMeasurementState()
                    }
                }
            }
            else -> super.onMessageReceived(event)
        }
    }
}