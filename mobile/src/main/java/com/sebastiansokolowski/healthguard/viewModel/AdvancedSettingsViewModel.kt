package com.sebastiansokolowski.healthguard.viewModel

import android.arch.lifecycle.ViewModel
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class AdvancedSettingsViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel) : ViewModel() {

    fun onSharedPreferenceChanged(key: String) {
        if (sensorDataModel.measurementRunning) {
            sensorDataModel.stopMeasurement()
            sensorDataModel.requestStartMeasurement()
        }
    }

}