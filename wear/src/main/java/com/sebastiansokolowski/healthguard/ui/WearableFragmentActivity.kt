package com.sebastiansokolowski.healthguard.ui

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.wearable.activity.WearableActivityDelegate
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * Created by Sebastian Sokołowski on 21.06.19.
 */
abstract class WearableFragmentActivity : FragmentActivity(), WearableActivityDelegate.AmbientCallback {
    private val wearableActivityDelegate = WearableActivityDelegate(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wearableActivityDelegate.onCreate(this)
    }

    override fun onResume() {
        super.onResume()
        wearableActivityDelegate.onResume()
    }

    override fun onPause() {
        wearableActivityDelegate.onPause()
        super.onPause()
    }

    override fun onStop() {
        wearableActivityDelegate.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        wearableActivityDelegate.onDestroy()
        super.onDestroy()
    }

    fun setAmbientEnabled() {
        wearableActivityDelegate.setAmbientEnabled()
    }

    fun setAutoResumeEnabled(enabled: Boolean) {
        wearableActivityDelegate.setAutoResumeEnabled(enabled)
    }

    fun setAmbientOffloadEnabled(enabled: Boolean) {
        wearableActivityDelegate.setAmbientOffloadEnabled(enabled)
    }

    fun isAmbient(): Boolean {
        return wearableActivityDelegate.isAmbient
    }

    override fun dump(prefix: String, fd: FileDescriptor, writer: PrintWriter, args: Array<String>) {
        wearableActivityDelegate.dump(prefix, fd, writer, args)
    }
}