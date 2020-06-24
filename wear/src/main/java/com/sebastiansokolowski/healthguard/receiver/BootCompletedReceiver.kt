package com.sebastiansokolowski.healthguard.receiver

import android.content.Context
import android.content.Intent
import com.sebastiansokolowski.healthguard.service.WearableService
import dagger.android.DaggerBroadcastReceiver
import timber.log.Timber


/**
 * Created by Sebastian Sokołowski on 04.06.19.
 */
class BootCompletedReceiver : DaggerBroadcastReceiver() {
    private val TAG = javaClass.canonicalName

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("onReceive action=${intent?.action}")
        super.onReceive(context, intent)

        context?.apply {
            startService(Intent(this, WearableService::class.java))
        }
    }
}