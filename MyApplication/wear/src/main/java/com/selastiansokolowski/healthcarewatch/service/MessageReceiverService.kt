package com.selastiansokolowski.healthcarewatch.service

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.selastiansokolowski.shared.DataClientPaths

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class MessageReceiverService : WearableListenerService() {

    override fun onDataChanged(p0: DataEventBuffer?) {
        super.onDataChanged(p0)
    }

    override fun onMessageReceived(event: MessageEvent?) {
        when (event?.path) {
            DataClientPaths.START_MEASUREMENT -> {

            }
            DataClientPaths.STOP_MEASUREMENT -> {

            }
            else -> super.onMessageReceived(event)
        }
    }
}