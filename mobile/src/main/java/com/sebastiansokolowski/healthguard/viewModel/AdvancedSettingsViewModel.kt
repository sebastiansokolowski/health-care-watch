package com.sebastiansokolowski.healthguard.viewModel

import androidx.lifecycle.ViewModel
import com.sebastiansokolowski.healthguard.model.MeasurementModel
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class AdvancedSettingsViewModel
@Inject constructor(private val measurementModel: MeasurementModel) : ViewModel() {

    fun onSharedPreferenceChanged(key: String) {
        if (measurementModel.measurementRunning) {
            measurementModel.stopMeasurement()
            measurementModel.requestStartMeasurement()
        }
    }

}