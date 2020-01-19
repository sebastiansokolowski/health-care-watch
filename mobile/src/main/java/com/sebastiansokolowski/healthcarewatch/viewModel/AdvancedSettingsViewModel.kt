package com.sebastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.sebastiansokolowski.healthcarewatch.model.SensorDataModel
import com.sebastiansokolowski.healthcarewatch.model.SettingsModel
import com.sebastiansokolowski.healthcarewatch.model.SetupModel
import com.sebastiansokolowski.healthcarewatch.util.HealthCareEventHelper
import com.sebastiansokolowski.healthcarewatch.view.preference.CustomMultiSelectListPreference
import com.sebastiansokolowski.shared.SettingsSharedPreferences
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class AdvancedSettingsViewModel
@Inject constructor(context: Context, private val settingsModel: SettingsModel, private val sensorDataModel: SensorDataModel, private val contentResolver: ContentResolver, val setupModel: SetupModel) : ViewModel() {

    private val healthCareEventHelper = HealthCareEventHelper(context)

    fun onSharedPreferenceChanged(key: String) {
        when (key) {
            SettingsSharedPreferences.SAMPLING_US,
            SettingsSharedPreferences.HEALTH_CARE_EVENTS -> {
                if (sensorDataModel.measurementRunning) {
                    sensorDataModel.stopMeasurement()
                    sensorDataModel.requestStartMeasurement()
                }
            }
        }
    }

}