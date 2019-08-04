package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import android.content.SharedPreferences
import android.provider.ContactsContract
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
import com.selastiansokolowski.healthcarewatch.ui.sensorData.SensorAdapterItem
import com.selastiansokolowski.healthcarewatch.view.preference.CustomMultiSelectListPreference
import com.selastiansokolowski.shared.SettingsSharedPreferences
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SettingsViewModel
@Inject constructor(private val sharedPreferences: SharedPreferences, private val wearableDataClient: WearableDataClient, private val contentResolver: ContentResolver) : ViewModel() {
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

    fun setupPreference(preference: CustomMultiSelectListPreference) {
        when (preference.key) {
            SettingsSharedPreferences.CONTACTS -> {
                setContacts(preference)
            }
            SettingsSharedPreferences.HEALTH_CARE_ENGINES -> {
                setSupportedEngines(preference)
            }
        }
    }

    private fun setContacts(preference: CustomMultiSelectListPreference) {
        val contactName = mutableListOf<String>()
        val contactNumber = mutableListOf<String>()

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

                if (!contactName.contains(name)) {
                    contactName.add("$name\n\t$number")
                    contactNumber.add(number)
                }
            } while (cursor.moveToNext())

            cursor.close()
        }

        preference.setValues(contactName.toTypedArray(), contactNumber.toTypedArray())
    }

    private fun setSupportedEngines(preference: CustomMultiSelectListPreference) {
        val engineName = mutableListOf<String>()
        val engineValue = mutableListOf<String>()

        HealthCareEventType.values().forEach {
            engineName.add(it.title)
            engineValue.add(it.name)
        }

        preference.setValues(engineName.toTypedArray(), engineValue.toTypedArray())
    }
}