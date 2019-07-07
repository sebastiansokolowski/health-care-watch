package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.ViewModel
import android.content.SharedPreferences
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.ui.sensorData.SensorAdapterItem
import com.selastiansokolowski.shared.SettingsSharedPreferences
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SettingsViewModel
@Inject constructor(private val sharedPreferences: SharedPreferences, private val wearableDataClient: WearableDataClient) : ViewModel() {
    fun onSharedPreferenceChanged(key: String) {
        when (key) {
            SettingsSharedPreferences.SAMPLING_US -> {
                updatedSettings()
            }
        }
    }

    private fun updatedSettings() {
        val refreshRate = sharedPreferences.getInt(SettingsSharedPreferences.SAMPLING_US, SettingsSharedPreferences.SAMPLING_US_DEFAULT)

        val sampleUs = TimeUnit.SECONDS.toMicros(refreshRate.toLong()).toInt()
        val sensors = mutableListOf<Int>()

        //todo:
        SensorAdapterItem.values().forEach {
            sensors.add(it.sensorId)
        }

        val settings = WearableDataClient.Settings(sampleUs, sensors)
        wearableDataClient.sendSettings(settings)
    }
}