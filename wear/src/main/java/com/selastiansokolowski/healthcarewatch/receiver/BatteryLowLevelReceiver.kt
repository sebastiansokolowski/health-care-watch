package com.selastiansokolowski.healthcarewatch.receiver

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.selastiansokolowski.healthcarewatch.service.SensorService
import dagger.android.DaggerBroadcastReceiver


/**
 * Created by Sebastian Sokołowski on 04.06.19.
 */
class BatteryLowLevelReceiver : DaggerBroadcastReceiver() {

    private var mShouldUnbind: Boolean = false

    private var mBoundService: SensorService? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mBoundService = (service as SensorService.LocalBinder).service
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mBoundService = null
        }
    }

    private fun doBindService(context: Context) {
        if (context.bindService(Intent(context, SensorService::class.java),
                        mConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true
        }
    }

    private fun doUnbindService(context: Context) {
        if (mShouldUnbind) {
            context.unbindService(mConnection)
            mShouldUnbind = false
        }
    }

    override fun peekService(myContext: Context?, service: Intent?): IBinder {
        return super.peekService(myContext, service)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        context?.let {
            doBindService(it)
            mShouldUnbind = true
            mBoundService?.let {
                if (!it.measurementRunning) {
                    it.stopMeasurement()
                }
            }
            doUnbindService(context)
        }
    }
}