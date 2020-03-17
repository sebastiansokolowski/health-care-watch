package com.sebastiansokolowski.healthguard.viewModel

import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import com.sebastiansokolowski.healthguard.model.SettingsModel
import com.sebastiansokolowski.healthguard.model.SetupModel
import com.sebastiansokolowski.healthguard.util.HealthCareEventHelper
import com.sebastiansokolowski.healthguard.view.preference.CustomMultiSelectListPreference
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