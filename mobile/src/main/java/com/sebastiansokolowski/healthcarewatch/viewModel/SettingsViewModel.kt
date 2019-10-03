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
class SettingsViewModel
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

    fun setupPreference(preference: CustomMultiSelectListPreference) {
        when (preference.key) {
            SettingsSharedPreferences.CONTACTS -> {
                setContacts(preference)
            }
            SettingsSharedPreferences.HEALTH_CARE_EVENTS -> {
                setSupportedHealthCareEvents(preference)
            }
        }
    }

    private fun setContacts(preference: CustomMultiSelectListPreference) {
        val names = mutableListOf<String>()
        val values = mutableListOf<String>()

        val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")

        if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
            do {
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                number = number.replace(" ", "")
                number = number.replace("-", "")

                if (!names.contains(name)) {
                    names.add("$name\n\t$number")
                    values.add(number)
                }
            } while (cursor.moveToNext())

            cursor.close()
        }

        preference.setValues(names.toTypedArray(), values.toTypedArray())
    }

    private fun setSupportedHealthCareEvents(preference: CustomMultiSelectListPreference) {
        val names = mutableListOf<String>()
        val values = mutableListOf<String>()

        val healthCareEventTypesName = settingsModel.getSupportedHealthCareEventTypes()

        healthCareEventTypesName.forEach {
            names.add(healthCareEventHelper.getTitle(it))
            values.add(it.name)
        }

        preference.setValues(names.toTypedArray(), values.toTypedArray())
    }

}